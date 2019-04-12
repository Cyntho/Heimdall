package org.cyntho.ts.heimdall.util;

import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.VirtualServerInfo;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.manager.user.TS3User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides functionality to handleUserPlaceholder a String (e.g. messages)
 * into a 'translated' version by replacing placeholders with
 * their actual values, either taken from the database,
 * the user's TS3User object or the server itself.
 *
 * @author  Xida
 * @version 1.0
 */
public class PlaceHolder {

    public static String handleUserPlaceholder(TS3User user, String raw){

        String tmp = raw;
        try {
            Map<String, String> base = new HashMap<>();

            System.out.println("converting: " + raw);

            if (user != null){
                base.put("%USER_LINK%", "[URL=client://" + user.getOfflineCopy().getRuntimeId() + "/" + user.getClientUUID() + "~=" + user.getOfflineCopy().getNickname() + "]" + user.getOfflineCopy().getNickname() + "[/URL]");
                base.put("%USER_NAME%", user.getOfflineCopy().getNickname());

                ClientInfo clientInfo = user.getClientInfo();

                if (clientInfo != null){
                    int con = clientInfo.getTotalConnections();

                    if (con == 1) {
                        base.put("%CONNECTION_COUNT_NAME%", "st");
                    } else if (con == 2){
                        base.put("%CONNECTION_COUNT_NAME%", "nd");
                    } else if (con == 3){
                        base.put("%CONNECTION_COUNT_NAME%", "rd");
                    } else {
                        base.put("%CONNECTION_COUNT_NAME%", "th");
                    }


                    base.put("%CONNECTION_COUNT_TOTAL%", String.valueOf(con));
                }
            }

            for (String key : base.keySet()){
                tmp = tmp.replaceAll(key, base.get(key));
            }

            System.out.println("finished converting to: " + tmp);
        } catch (Exception e){
            if (Bot.DEBUG_MODE){
                e.printStackTrace();
            }
            return raw;
        }

        return tmp;
    }

    public static String handleBotPlaceholder(String raw){
        String tmp = raw;

        System.out.println("Raw: " + raw);

        try {
            Map<String, String> base = new HashMap<>();

            VirtualServerInfo serverInfo = Bot.heimdall.getApi().getServerInfo();

            if (serverInfo != null){
                base.put("%SERVER_NAME%", serverInfo.getName());
                base.put("%SERVER_UPTIME%", String.valueOf(serverInfo.getUptime()));
                base.put("%SERVER_VERSION%", serverInfo.getVersion());
            }

            base.put("%BOT_NICKNAME%", Bot.heimdall.getNickname());
            base.put("%BOT_VERSION%", Bot.VERSION);
            base.put("%BOT_AUTHOR%", Bot.AUTHOR);
            base.put("%BOT_CONTACT%", Bot.CONTACT);
            base.put("%BOT_UPTIME%", String.valueOf(Bot.heimdall.getUpTimeFormatted(new SimpleDateFormat("dd:HH:mm"))));

            for (String key : base.keySet()){
                tmp = tmp.replaceAll(key, base.get(key));
            }

            System.out.println("Converted to: " + tmp);
        } catch (Exception e){
            if (Bot.DEBUG_MODE){
                e.printStackTrace();
            }
            return raw;
        }
        return tmp;
    }






    public static int getDayOfMonth(){
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

}
