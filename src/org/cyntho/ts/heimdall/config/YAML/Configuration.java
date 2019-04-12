package org.cyntho.ts.heimdall.config.YAML;

import java.util.Map;

/**
 * Created by Xida on 12.07.2017.
 */
public interface Configuration extends ConfigurationSection{

    void addDefault(String path, Object value);
    void addDefaults(Map<String, Object> defaults);
    void addDefaults(Configuration defaults);
    void setDefaults(Configuration defaults);

    Configuration getDefaults();
    ConfigurationOptions options();
}