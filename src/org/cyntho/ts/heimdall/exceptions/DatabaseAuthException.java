package org.cyntho.ts.heimdall.exceptions;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;

/**
 * Created by Xida on 14.07.2017.
 */
public class DatabaseAuthException extends Throwable {

    private static final long serialVersionUID = 0x06;
    private String conString, user, pass;

    public DatabaseAuthException(String con){
        conString = con;
    }
    public DatabaseAuthException(String con, String u, String p){
        conString = con;
        user = u;
        pass = p;
    }

    public final String getMessage(){
        if (user == null || pass == null || !DEBUG_MODE){
            return ("Could not establish connection to " + conString);
        } else {
            return ("Could not establish connection to " + conString + " using credentials user=[" + user + " ] pass=[" + pass + "]");
        }
    }
}
