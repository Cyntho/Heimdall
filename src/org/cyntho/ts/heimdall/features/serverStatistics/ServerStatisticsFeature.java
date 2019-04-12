package org.cyntho.ts.heimdall.features.serverStatistics;


import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.database.DatabaseConnector;
import org.cyntho.ts.heimdall.features.BaseFeature;

/**
 * This feature creates certain statistics about the server, like:
 *
 * 1) Online/Offline/Idle statistics about users //todo: export to globalListener
 * 2) Workload of channels
 * 3) Nickname history
 * 4) Description history
 * 5) Avatar history
 * 6) User meta data (IP, Version, OS, Country, ConnectionCounter)
 *
 * @author  Xida
 * @version 1.0
 */
public class ServerStatisticsFeature extends BaseFeature {

    private DatabaseConnector db;
    private volatile boolean stopRequest;

    private Thread thread;
    private ServerStatisticsRunner runner;
    private int updateInterval;


    public ServerStatisticsFeature(int updateInterval){
        super("ServerStatisticsFeature");
        this.updateInterval = updateInterval;
    }


    @Override
    public Long getId() {return 0x005L;}

    @Override
    public void activate(){
        if (super.tryToggleStatus(true)){
            db = new DatabaseConnector(Bot.heimdall.getBotConfig());
            super.active = true;
            stopRequest = false;

            runner = new ServerStatisticsRunner(updateInterval);
            thread = new Thread(runner);
            thread.start();
        }
    }

    @Override
    public void deactivate(){
        if (super.tryToggleStatus(false)){
            stopRequest = true;
            if (thread != null){
                try {
                    thread.join(2001);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            db = null;
            runner = null;
            thread = null;
            super.active = false;

        }
    }


    /* ------------------------ */
    private class ServerStatisticsRunner implements Runnable {

        private long lastCheck;
        private int  cooldown;

        ServerStatisticsRunner(int cd){
            cooldown = cd;
            lastCheck = 0;
        }

        @Override
        public void run(){
            check();
            do {
                // Sleep until cooldown is due but wake up every 2 seconds
                // to check for the stopRequest
                while (System.currentTimeMillis() - lastCheck < cooldown){
                    try {
                        Thread.sleep(2000);

                        if (stopRequest) return;

                    } catch (InterruptedException e){
                        return;
                    }
                }
                check();
            } while (!stopRequest);
        }

        void check(){
            lastCheck = System.currentTimeMillis();
        }
    }



}
