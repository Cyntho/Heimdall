package org.cyntho.ts.heimdall.features.userSurveillance;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.config.CustomConfig;

import java.io.IOException;

/**
 * Created by Xida on 04.04.2019
 */
@SuppressWarnings("FieldCanBeLocal")
public class UserSurveillanceConfig extends CustomConfig {




    public UserSurveillanceConfig() throws IOException{
        super("Surveillance.yml", "Surveillance.yml");
        if (Bot.DEBUG_MODE){
            super.createExternal();
        }
    }

}
