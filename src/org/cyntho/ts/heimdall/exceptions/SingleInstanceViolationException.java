package org.cyntho.ts.heimdall.exceptions;

import org.cyntho.ts.heimdall.app.SimpleBotInstance;

public class SingleInstanceViolationException extends Throwable {

    private static final long serialVersionUID = 0x07;
    private SimpleBotInstance parent;

    public SingleInstanceViolationException(SimpleBotInstance parent){
        this.parent = parent;
    }

    @Override
    public final String getMessage(){
        return "SimpleBotInstance ' " + parent.getInstanceIdentifier() + "' forces Single-Instance.";
    }

}
