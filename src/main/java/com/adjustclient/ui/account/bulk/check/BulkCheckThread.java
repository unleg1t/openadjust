package com.adjustclient.ui.account.bulk.check;

import com.adjustclient.Adjust;
import com.adjustclient.auth.LoginResult;
import com.adjustclient.auth.LoginStatus;
import com.adjustclient.backend.api.NullableValue;
import com.adjustclient.backend.api.request.repository.ChangePermissionsRequest;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountType;
import com.adjustclient.ui.account.LocalAccountStore;
import com.adjustclient.ui.account.importer.BulkImportScreen;
import com.adjustclient.ui.account.importer.ImportMode;
import com.adjustclient.ui.account.login.auth.MicrosoftCookieAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftCredentialAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftRefreshTokenAccount;
import com.adjustclient.ui.account.repository.AccountRepository;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class BulkCheckThread extends Thread {
    public boolean failedStop;
    public volatile boolean stopped;
    private final BulkImportScreen screen;

    public BulkCheckThread(BulkImportScreen screen) {
        super("Bulk Check Thread");
        this.screen = screen;
    }

    public void stopChecking() {
        if (!this.stopped) {
            this.stopped = true;
            this.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            int checkedCount = 0;
            int rateLimits = 0;
            while (!this.stopped && !this.screen.uncheckedList.entries.isEmpty()) {
                if (Adjust.hydraConnection != null) {
                    if (!vendorIsBackendReady()) {
                        break;
                    }
                } else if (Adjust.INSTANCE.accountManager == null || Adjust.INSTANCE.accountManager.currentRepository == null) {
                    break;
                }

                if (checkedCount > 0) {
                    try {
                        Thread.sleep(2000L * (long) rateLimits + 1000L);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                    if (this.stopped || this.screen.uncheckedList.entries.isEmpty()) {
                        break;
                    }
                }
                checkedCount++;

                CheckEntry entry = this.screen.uncheckedList.entries.get(0);
                AccountEntry account;
                LoginResult result;
                AccountType type;
                byte[] serializedData;

                if (this.screen.mode == ImportMode.MICROSOFT) {
                    result = MicrosoftCredentialAccount.login(entry.name, entry.data);
                    type = AccountType.CREDENTIALS;
                    MicrosoftCredentialAccount credentialAccount = new MicrosoftCredentialAccount(entry.name, entry.data, false);
                    account = credentialAccount;
                    try {
                        serializedData = credentialAccount.serialize();
                    } catch (Exception e) {
                        Logger.log(LogLevel.ERROR, true, "Could not save account '{}' type data due to: {}", entry.name, e);
                        serializedData = new byte[0];
                    }
                } else if (this.screen.mode == ImportMode.REFRESH) {
                    MicrosoftRefreshTokenAccount refreshAccount = new MicrosoftRefreshTokenAccount(entry.data, true, false);
                    result = refreshAccount.login();
                    type = AccountType.BROWSER;
                    account = refreshAccount;
                    try {
                        serializedData = refreshAccount.serialize();
                    } catch (Exception e) {
                        Logger.log(LogLevel.ERROR, true, "Could not save account '{}' type data due to: {}", entry.name, e);
                        serializedData = new byte[0];
                    }
                } else {
                    MicrosoftCookieAccount cookieAccount = new MicrosoftCookieAccount(entry.data, false);
                    result = cookieAccount.login();
                    type = AccountType.COOKIE;
                    account = cookieAccount;
                    try {
                        serializedData = cookieAccount.serialize();
                    } catch (Exception e) {
                        Logger.log(LogLevel.ERROR, true, "Could not save cookie account '{}' type data due to: {}", entry.name, e);
                        serializedData = new byte[0];
                    }
                }

                if (result.status == LoginStatus.VALID_RATE_LIMITED) {
                    rateLimits++;
                    entry.status = "Ratelimited #" + rateLimits;
                    entry.success = false;
                    continue;
                }

                this.screen.uncheckedList.entries.remove(entry);
                entry.status = result.status.message;
                entry.success = result.status == LoginStatus.SUCCESS;
                this.screen.checkedList.entries.add(entry);
                rateLimits = 0;

                if (result.status == LoginStatus.SUCCESS && result.session != null) {
                    if (Adjust.hydraConnection != null) {
                        vendorSaveCheckedAccount(entry, result.session, type, serializedData);
                    } else {
                        saveCheckedAccountOffline(entry, account, result.session);
                    }
                }
            }
        } finally {
            if (this.screen.checkThread != null) {
                this.screen.checkThread.stopChecking();
                this.screen.checkThread = null;
            }
        }
    }

    private void saveCheckedAccountOffline(CheckEntry entry, AccountEntry account, Session session) {
        AccountRepository repository = Adjust.INSTANCE.accountManager.currentRepository;
        if (repository == null) {
            onAccountSaveFailed(entry, "No repository selected");
            return;
        }
        account.username = session.getUsername();
        account.token = session.getToken();
        account.setUuid(session.getPlayerID());
        Minecraft.getMinecraft().addScheduledTask(() -> {
            try {
                LocalAccountStore.INSTANCE.add(repository, account);
            } catch (RuntimeException error) {
                onAccountSaveFailed(entry, error);
            }
        });
    }

    private void onAccountSaveFailed(CheckEntry entry, Object reason) {
        entry.status = "Successful, Account Add Failure";
        entry.success = false;
        Logger.log(LogLevel.WARN, true, "Unable to save account '{}'. Stopping check! {}", entry.name, reason);
        this.stopped = true;
        this.failedStop = true;
        this.interrupt();
    }

    private boolean vendorIsBackendReady() {
        return Adjust.hydraConnection != null
                && Adjust.hydraConnection.authenticated
                && Adjust.hydraConnection.ready;
    }

    private void vendorSaveCheckedAccount(CheckEntry entry, Session session, AccountType type, byte[] serializedData) {
        AccountRepository repository = Objects.requireNonNull(Adjust.INSTANCE.accountManager.currentRepository);
        String username = Objects.requireNonNull(session.getUsername());
        String token = Objects.requireNonNull(session.getToken());
        new ChangePermissionsRequest(
                repository,
                username,
                new NullableValue(session.getProfile().getId()),
                token,
                type,
                serializedData
        ).toPacket().sendAsync(response -> vendorOnSaveResponse(entry, response));
    }

    private void vendorOnSaveResponse(CheckEntry entry, BackendResponse response) {
        if (response.status != ResponseStatus.OK) {
            onAccountSaveFailed(entry, response);
        }
    }
}
