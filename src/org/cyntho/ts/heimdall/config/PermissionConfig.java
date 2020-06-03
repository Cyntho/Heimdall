package org.cyntho.ts.heimdall.config;

import java.io.IOException;

public class PermissionConfig extends CustomConfig {

    public PermissionConfig() throws IOException {
        super("permissionsDefault.yml", "permissions.yml");

        if (!initialized){
            System.out.println("Error initializing config.");
        }
    }


}
