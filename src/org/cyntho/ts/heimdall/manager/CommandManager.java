package org.cyntho.ts.heimdall.manager;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.commands.BaseCommand;
import org.cyntho.ts.heimdall.events.CommandListener;
import org.cyntho.ts.heimdall.features.BaseFeature;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * The CommandManager class, if activated, manages certain registered
 * commands that can be used by TS3User instances while being connected
 * to the Teamspeak Server
 *
 * @author  Xida
 * @version 1.0
 */
public class CommandManager extends BaseFeature{

    private List<BaseCommand> commands;
    private CommandListener listener;
    private boolean active = false;

    public Long getId() { return 0x002L; }

    public CommandManager(){
        super("CommandManager");
        commands = new ArrayList<>();
        listener = new CommandListener(this);
    }

    public void registerCommand(BaseCommand command){
        commands.add(command);
    }

    public void unregisterCommand(BaseCommand command){
        commands.remove(command);
    }

    public boolean isRegistered(String name){
        for (BaseCommand command : commands){
            if (command.getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }

    public BaseCommand getCommandByLabel(String name){
        for (BaseCommand command : commands){
            if (command.getLabel().equalsIgnoreCase(name)){
                return command;
            }
        }
        return null;
    }

    public Collection<BaseCommand> getRegistered(){
       return Collections.unmodifiableCollection(this.commands);
    }

    public void activate(){
        if (!active){
            active = true;
            Bot.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been activated.");
            this.listener = new CommandListener(this);
            Bot.heimdall.getApi().addTS3Listeners(listener);
        }
    }

    public void deactivate(){
        if (active){
            active = false;
            Bot.log(LogLevelType.BOT_EVENT, "Feature: " + super.getName() + " has been deactivated.");
            try {
                Bot.heimdall.getApi().removeTS3Listeners(this.listener);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }



}
