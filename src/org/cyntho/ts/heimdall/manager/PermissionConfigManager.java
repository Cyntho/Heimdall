package org.cyntho.ts.heimdall.manager;

import org.cyntho.ts.heimdall.config.CustomConfig;
import org.cyntho.ts.heimdall.config.PermissionConfig;
import org.cyntho.ts.heimdall.config.YAML.file.FileConfiguration;
import org.cyntho.ts.heimdall.config.YAML.file.YamlConfiguration;
import org.cyntho.ts.heimdall.perm.PermissionGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PermissionConfigManager {

    private List<PermissionGroup> groups;
    private PermissionConfig config;
    private boolean initialized = false;

    public PermissionConfigManager(){
        groups = new ArrayList<>();
    }

    private void init(){
        try {
            config = new PermissionConfig();

            FileConfiguration f = config.getFileConfiguration();

            if (f != null && f.getRoot() != null){
                for (String s : f.getRoot().getKeys(false)){
                    groups.add(new PermissionGroup(s));
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        initialized = true;
    }

    public boolean isInitialized() { return initialized; }

}
