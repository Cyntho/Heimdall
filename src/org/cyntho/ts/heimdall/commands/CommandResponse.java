package org.cyntho.ts.heimdall.commands;

public enum CommandResponse {

    COMMAND_SUCCESS ("Success", 0x000),
    FAILURE_PERMISSION ("FailurePermission", 0x001);


    private final String name;
    private final long value;

    CommandResponse(final String n, final long value){
        this.name = n;
        this.value = value;
    }


}
