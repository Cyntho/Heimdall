package org.cyntho.ts.heimdall.features.userSurveillance;


import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.features.userSurveillance.runners.ClientRunner;
import org.cyntho.ts.heimdall.features.userSurveillance.runners.UserSurveillanceBaseRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  Xida
 * @version 1.0
 *
 * A TS-Bot feature to observe special users and their activities.
 * Configuration takes place in the 'Surveillance.yml' config file
 *
 */
public class UserSurveillanceFeature extends BaseFeature {

    /* Constructor */
    public UserSurveillanceFeature() {
        super("UserSurveillanceFeature");
        runners = new ArrayList<>();
    }

    /* Additionally needed variables */
    private List<UserSurveillanceBaseRunner> runners;










    /* Overriding base class */

    @Override
    public Long getId() {
        return 0x004L;
    }

    @Override
    public void activate(){

    }

    @Override
    public void deactivate(){

    }

}
