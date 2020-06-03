package org.cyntho.ts.heimdall.commands;

public enum CommandResponse {

    SUCCESS(0x000),
    HIDDEN(0x001),
    FAILURE(0x002);


    private final long value;

    CommandResponse(final long value){
        this.value = value;
    }

    public long getValue() { return value; }
}
