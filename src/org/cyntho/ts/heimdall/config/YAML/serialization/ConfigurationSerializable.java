package org.cyntho.ts.heimdall.config.YAML.serialization;

import java.util.Map;

/**
 * Created by Xida on 12.07.2017.
 */
public interface ConfigurationSerializable {

    public Map<String, Object> serialize();
}
