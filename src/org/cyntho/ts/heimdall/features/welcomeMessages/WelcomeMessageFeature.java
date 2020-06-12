package org.cyntho.ts.heimdall.features.welcomeMessages;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.events.WelcomeMessageListener;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;

/**
 * Created by Xida on 14.07.2017.
 */
public class WelcomeMessageFeature extends BaseFeature {

    public WelcomeMessageFeature(){
        super( "WelcomeMessageFeature");
    }

    public Long getId() { return 0x003L; }

    private WelcomeMessageListener listener;

    public void activate(){

        Bot.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been activated.");

        if (!super.active){
            super.active = true;

            this.listener = new WelcomeMessageListener(this);

            Bot.heimdall.getApi().addTS3Listeners(listener);
        }
    }

    public void deactivate(){

        Bot.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been deactivated.");

        if (super.active){
            super.active = false;

            try {
                Bot.heimdall.getApi().removeTS3Listeners(this.listener);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }




}
