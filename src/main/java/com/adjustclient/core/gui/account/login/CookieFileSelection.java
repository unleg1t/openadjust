package com.adjustclient.core.gui.account.login;

import com.adjustclient.core.gui.account.repository.AccountRepository;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.server.ServerRegion;

public class CookieFileSelection {
    public AccountRepository repository;
    public ClientUser user;
    public ServerRegion role;
    public Object field16;

    public CookieFileSelection(AccountRepository repository, ClientUser user, ServerRegion role) {
        this.repository = repository;
        this.user = user;
        this.role = role;
    }
}
