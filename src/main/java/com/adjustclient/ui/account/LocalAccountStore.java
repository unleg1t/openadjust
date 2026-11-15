package com.adjustclient.ui.account;

import com.adjustclient.service.storage.LocalStorage;
import com.adjustclient.ui.account.repository.AccountRepository;
import com.adjustclient.ui.account.login.auth.MicrosoftCookieAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftCredentialAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftRefreshTokenAccount;
import com.adjustclient.ui.account.login.auth.SessionTokenAccount;
import com.adjustclient.server.ServerType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class LocalAccountStore {
    public static final LocalAccountStore INSTANCE = new LocalAccountStore();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<AccountRepository> repositories = new ArrayList<>();
    private final Path file = LocalStorage.INSTANCE.directory.toPath().resolve("accounts-local-v1.json");
    private boolean loaded;
    private boolean readFailed;
    private int nextRepositoryId = 1;
    private int nextAccountId = 1;
    private int selectedRepository;

    private LocalAccountStore() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { save(); } catch (RuntimeException error) { error.printStackTrace(); }
        }, "Adjust-local-accounts-save"));
    }

    public synchronized void open(AccountManagerScreen manager) {
        if (!loaded) load();
        if (readFailed) throw new IllegalStateException("Local account file could not be read; original retained: " + file);
        manager.repositories = repositories;
        if (manager.selection == null) manager.selection = new ArrayList<>();
        if (!repositories.contains(manager.currentRepository)) {
            manager.currentRepository = repositories.stream().filter(r -> r.id == selectedRepository)
                    .findFirst().orElse(repositories.isEmpty() ? null : repositories.get(0));
        }
    }

    private void load() {
        loaded = true;
        if (!Files.exists(file)) return;
        try {
            State state = gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), State.class);
            if (state == null || state.version != 1) throw new IOException("Unsupported local account format");
            List<AccountRepository> restored = new ArrayList<>();
            Set<Integer> repositoryIds = new HashSet<>(), accountIds = new HashSet<>();
            nextRepositoryId = Math.max(1, state.nextRepositoryId);
            nextAccountId = Math.max(1, state.nextAccountId);
            for (RepositoryData data : state.repositories) {
                if (data.id <= 0 || !repositoryIds.add(data.id)) throw new IOException("Duplicate repository id");
                AccountRepository repository = new AccountRepository(data.name, data.id);
                repository.lastAccessed = data.lastAccessed;
                repository.addMember(new com.adjustclient.user.ClientUser("Local owner"), com.adjustclient.server.ServerRegion.OWNER);
                repository.refreshMemberNames();
                repository.loading = false;
                repository.loaded = true;
                for (AccountData item : data.accounts) {
                    if (item.id <= 0 || !accountIds.add(item.id)) throw new IOException("Duplicate account id");
                    AccountEntry account = switch (AccountType.valueOf(item.type)) {
                        case COOKIE -> new MicrosoftCookieAccount();
                        case CREDENTIALS -> new MicrosoftCredentialAccount();
                        case BROWSER -> new MicrosoftRefreshTokenAccount();
                        case SESSION -> new SessionTokenAccount();
                    };
                    account.deserialize(Base64.getDecoder().decode(item.data));
                    account.id = item.id;
                    account.repository = repository;
                    account.username = item.username;
                    account.token = item.token;
                    account.setUuid(item.uuid);
                    if (item.lastLoginUser != null) account.lastLogin = new LastLogin(item.lastLoginUser, item.lastLoginTime, null);
                    for (Map.Entry<String, Long> ban : item.bans.entrySet())
                        account.bans.setExpiry(ServerType.valueOf(ban.getKey()), ban.getValue(), false);
                    repository.accounts.add(account);
                    nextAccountId = Math.max(nextAccountId, Math.addExact(item.id, 1));
                }
                repository.accountCount = repository.accounts.size();
                restored.add(repository);
                nextRepositoryId = Math.max(nextRepositoryId, Math.addExact(data.id, 1));
            }
            repositories.addAll(restored);
            selectedRepository = state.selectedRepository;
        } catch (Exception error) {
            // Never overwrite a damaged/unsupported file with a partial or empty load.
            readFailed = true;
            throw new IllegalStateException("Unable to load local accounts; original file retained: " + file, error);
        }
    }

    public synchronized void save() {
        if (!loaded || readFailed) return;
        State state = new State();
        state.nextRepositoryId = nextRepositoryId;
        state.nextAccountId = nextAccountId;
        state.selectedRepository = selectedRepository;
        try {
            for (AccountRepository repository : repositories) {
                RepositoryData data = new RepositoryData();
                data.id = repository.id;
                data.name = repository.name;
                data.lastAccessed = repository.lastAccessed;
                repository.accountCount = repository.accounts.size();
                for (AccountEntry account : repository.accounts) {
                    AccountData item = new AccountData();
                    item.id = account.id;
                    item.type = account.getType().name();
                    item.username = account.username;
                    item.uuid = account.uuid;
                    item.token = account.token;
                    item.data = Base64.getEncoder().encodeToString(account.serialize());
                    if (account.lastLogin != null) {
                        item.lastLoginUser = account.lastLogin.user;
                        item.lastLoginTime = account.lastLogin.time;
                    }
                    account.bans.bans.forEach((server, expiry) -> item.bans.put(((ServerType) server).name(), expiry));
                    data.accounts.add(item);
                }
                state.repositories.add(data);
            }
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, gson.toJson(state), StandardCharsets.UTF_8);
            try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException error) { throw new UncheckedIOException("Unable to save local accounts", error); }
    }

    public synchronized AccountRepository create(String name) {
        if (name == null || name.length() < 3 || name.length() > 26 || !name.matches("^[a-zA-Z0-9-\\s]*$"))
            throw new IllegalArgumentException("Invalid repository name");
        AccountRepository repository = new AccountRepository(name, nextRepositoryId);
        nextRepositoryId = Math.addExact(nextRepositoryId, 1);
        repository.addMember(new com.adjustclient.user.ClientUser("Local owner"), com.adjustclient.server.ServerRegion.OWNER);
        repository.refreshMemberNames();
        repository.loading = false;
        repository.loaded = true;
        repositories.add(repository);
        try { save(); }
        catch (RuntimeException error) { repositories.remove(repository); throw error; }
        return repository;
    }

    public synchronized void rename(AccountRepository repository, String name) {
        if (!repositories.contains(repository)) throw new IllegalArgumentException("Repository no longer exists");
        String previous = repository.name;
        repository.name = name;
        try { save(); }
        catch (RuntimeException error) { repository.name = previous; throw error; }
    }

    public synchronized void delete(AccountManagerScreen manager, AccountRepository repository) {
        repositories.remove(repository);
        if (manager.currentRepository == repository) manager.currentRepository = null;
        manager.select(null);
        manager.selection.clear();
        open(manager);
        save();
    }

    public synchronized void add(AccountRepository repository, AccountEntry account) {
        if (!repositories.contains(repository)) throw new IllegalArgumentException("Repository no longer exists");
        account.id = nextAccountId;
        nextAccountId = Math.addExact(nextAccountId, 1);
        account.repository = repository;
        repository.accounts.add(account);
        repository.accountCount = repository.accounts.size();
        try { save(); }
        catch (RuntimeException error) {
            repository.accounts.remove(account);
            repository.accountCount = repository.accounts.size();
            account.repository = null;
            throw error;
        }
    }

    public synchronized void remove(AccountManagerScreen manager, AccountRepository repository, AccountEntry[] accounts) {
        for (AccountEntry account : accounts) {
            if (repository.accounts.contains(account)) manager.accountDeleted(repository.id, account.id);
        }
        repository.accountCount = repository.accounts.size();
        save();
    }

    public synchronized void move(AccountManagerScreen manager, AccountRepository source, AccountRepository target, AccountEntry[] accounts) {
        if (source == target || !repositories.contains(target)) return;
        for (AccountEntry account : accounts) if (source.accounts.remove(account)) {
            account.repository = target;
            target.accounts.add(account);
        }
        source.accountCount = source.accounts.size();
        target.accountCount = target.accounts.size();
        manager.select(null);
        save();
    }

    public synchronized void selected(AccountRepository repository) {
        selectedRepository = repository == null ? 0 : repository.id;
        if (repository != null) repository.lastAccessed = System.currentTimeMillis();
        save();
    }

    private static final class State {
        int version = 1, nextRepositoryId = 1, nextAccountId = 1, selectedRepository;
        List<RepositoryData> repositories = new ArrayList<>();
    }
    private static final class RepositoryData {
        int id;
        String name;
        long lastAccessed;
        List<AccountData> accounts = new ArrayList<>();
    }
    private static final class AccountData {
        int id;
        String type, username, uuid, token, data, lastLoginUser;
        long lastLoginTime;
        Map<String, Long> bans = new LinkedHashMap<>();
    }
}
