package com.adjustclient.ui.account.login;

import com.adjustclient.ui.account.repository.AccountRepository;
import com.adjustclient.user.ClientUser;
import com.adjustclient.server.ServerRegion;

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
