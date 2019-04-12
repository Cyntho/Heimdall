package org.cyntho.ts.heimdall.util;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The (static) ChannelManagement class provides
 * some basic functionality for channels, like
 * resolving the absolute path to a channel  from the root
 *
 * @author  Xida
 * @version 1.0
 */
public class ChannelManagement {


    public static String getPathToChannelById(final int id, boolean skipSpacer){

        ChannelInfo info = Bot.heimdall.getApi().getChannelInfo(id);

        if (info == null){
            Bot.heimdall.log(LogLevelType.COMMAND_ERROR, "Could not get path to channel '" + id + "' - Channel not found");
            return null;
        }

        StringBuilder path = new StringBuilder();

        try {
            Channel current;
            List<Channel> channelList = Bot.heimdall.getApi().getChannels();
            Map<Integer, Channel> channelMap = new HashMap<>();

            // Generate Map<Integer, Channel> to easily find channels based on it's ID
            // Instead of looping over TS3Api.getChannels() over and over again
            for (Channel channel : channelList){
                channelMap.put(channel.getId(), channel);
            }


            // Add the requested channel
            current = channelMap.get(id);
            path = new StringBuilder("/" + current.getName());

            // Recursively iterate over channels, until root reached
            while (current.getParentChannelId() != 0){

                current = channelMap.get(current.getParentChannelId());

                if (!skipSpacer || !current.getName().contains("spacer")){
                    path.insert(0, "/" + current.getName());
                }
            }

            path = new StringBuilder("{" + path + "}");
        } catch (Exception ex){
            Bot.heimdall.log(LogLevelType.BOT_ERROR, "Could not resolve path to channel '" + id + "'");
        }

        return path.toString();
    }


    public static Map<ChannelProperty, String> mapOf(ChannelProperty property, Object value){
        return Collections.singletonMap(property, String.valueOf(value));
    }

}
