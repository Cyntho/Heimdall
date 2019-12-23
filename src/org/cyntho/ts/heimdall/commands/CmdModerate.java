package org.cyntho.ts.heimdall.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import org.cyntho.ts.heimdall.manager.user.TS3User;


/**
 * Command to moderate a channel
 *
 * Setting a channel to 'moderate' temporarily removes permissions to
 * write and talk within the given channel to allow a more clean conversation.
 *
 * Parameters are as follows:
 *
 *      -c {id}           Target channel by it's ID
 *      -u {'name'|uuid}  Target user by name|uuid
 *      -g {'name'|id}    Target users by server group
 *      -r                Reset to defaults (if used alone, ALL settings will be reset)
 *      -i ['name']       Ignore specific server group (if blank, invoker's group will  be used)
 *
 * Usage:
 *      !mod [-c {id}] | [-u {'name' | 'uuid'}] | [-g {'name' | 'id'}] | [-r] | [-i {'name'}
 *
 * Examples:
 *      Set invoker's current channel to moderated:
 *      !mod
 *
 *      Set channel with id '65' to moderated:
 *      !mod -c 65
 *
 *      Set channel with id '65' to default
 *      !mod -r -c 65
 *
 *      Set all users of the group 'guest' to moderated in Lobby
 *      !mod -c 'Lobby' -g 'guest'
 *
 *      Set all  users of group 'admin' to un-moderated in Lobby
 *      !mod -c 'Lobby' -g 'admin' -r
 *
 *      Set Lobby to moderated but allow members to talk (2 commands)
 *      !mod -c 'Lobby'
 *      !mod -c 'Lobby' -r -g 'member'
 *
 */
public class CmdModerate extends BaseCommand {

    public CmdModerate(){
        super(4, "mod", "!mod");
    }

    @Override
    public boolean execute(TS3User invoker, String[] args){
        if (invoker.hasPermission("moderate")) {

            int targetChannnel = invoker.getCurrentChannelId();
            int targetGroup = invoker.getPermissionGroupId();




        }
        return false;
    }

    @Override
    public void sendUsage(TS3User invoker){
        invoker.sendPrivateMessage("!mod [-c {id}] | [-u {'name' | 'uuid'}] | [-g {'name' | 'id'}] | [-r] | [-i {'name'}");
    }

    @Override
    public void sendHelp(TS3User invoker){
        StringBuilder sb = new StringBuilder();

        sb.append("\nHelp for !mod command: \n");
        sb.append("\t-c {id}           Target channel by it's ID\n");
        sb.append("\t-u {'name'|uuid}  Target user by name|uuid\n");
        sb.append("\t-g {'name'|id}    Target users by server group\n");
        sb.append("\t-r                Reset to defaults (if used alone, ALL settings will be reset)\n");
        sb.append("\t-i ['name']       Ignore specific server group (if blank, invoker's group will  be used)");

        invoker.sendPrivateMessage(sb.toString());
    }
}
