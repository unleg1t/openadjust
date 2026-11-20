package com.adjustclient.utilities.backend.hwid;

import oshi.SystemInfo;

public class UserEnvironmentCollector extends HwidCollector {
    private int runCount;

    public UserEnvironmentCollector(SystemInfo systemInfo, HwidSink sink) {
        super(systemInfo, sink);
    }

    public UserEnvironmentCollector(SystemInfo systemInfo) {
        super(systemInfo);
    }

    private String a(String... names) {
        try {
            for (String name : names) {
                for (String key : System.getenv().keySet()) {
                    if (key.equalsIgnoreCase(name)) {
                        return System.getenv(key);
                    }
                }
            }
            return "(!)";
        } catch (Throwable ignored) {
            for (String name : names) {
                String value = System.getenv(name);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
    }

    private void c() {
        this.sink.a("User: " + a("USERNAME", "LOGNAME", "WINEUSERNAME"));
        this.sink.a("Host: " + a("COMPUTERNAME", "USERDOMAIN", "LOGONSERVER"));
        this.sink.a("Appdata: " + a("APPDATA", "LOCALAPPDATA", "ROAMINGAPPDATA"));
        this.sink.a("Home: " + System.getProperty("user.home"));
    }

    @Override
    public void run() {
        Runnable task = this::c;
        task.run();
        this.runCount++;
    }
}
