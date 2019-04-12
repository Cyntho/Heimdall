package org.cyntho.ts.heimdall.features.autoPromotion;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;

/**
 * This feature provides the functionality to promote users
 * to specific server-/ and channel groups, based on various
 * conditions like connection counter etc.
 * If enabled in the config file, web-permissions are taken into account.
 *
 * @see AutoPermissionCondition for a list of conditions
 *
 * @author  Xida
 * @version 1.0
 */
public class AutoPromotionFeature extends BaseFeature {

    // private fields
    private Thread updateThread;
    private volatile boolean stopRequest;


    // Constructor
    public AutoPromotionFeature(){ super("AutoPermissionFeature"); }

    @Override
    public Long getId() {return 0x004L;}

    @Override
    public void activate() {
        if (super.tryToggleStatus(true)){
            stopRequest = false;

            // Load countdown from config. Default: 10min
            long countdown = Bot.heimdall.getBotConfig().getLong("features.autoPromotion.UpdateFrequency", 10 * 1000 * 60);
            updateThread = new Thread(new UpdateThreadRunner(countdown));
            updateThread.start();

            super.active = true;
        }
    }

    @Override
    public void deactivate() {
        if (super.tryToggleStatus(false)){
            stopRequest = true;

            if (updateThread != null){
                try {
                    updateThread.join(1000);
                } catch (InterruptedException e) {
                    if (Bot.DEBUG_MODE) e.printStackTrace();
                }
            }
            super.active = false;
        }
    }

    private final class UpdateThreadRunner implements Runnable {

        private long lastCheck = 0;
        private long countdown;

        UpdateThreadRunner(long l){
            countdown = l;
        }

        @Override
        public void run(){
            // Run until stop is requested by the bot
            while (!stopRequest){

                // Sleep until the next check is due but wake up
                // every couple seconds (5) to check if a stop has been requested.
                // Todo: check if sleep is sufficient
                while (System.currentTimeMillis() - lastCheck < countdown){
                    try {
                        Thread.sleep(5 * 1000);

                        if (stopRequest) break;
                    } catch (InterruptedException e){
                        break;
                    }
                }

                if (stopRequest){
                    break;
                } else {
                    doCheck();

                    // reset countdown
                    lastCheck = System.currentTimeMillis();
                }
            }
        }


        public void doCheck(){

        }
    }



}
