package org.cyntho.ts.heimdall.manager.permissions;

/**
 * Created by Xida on 15.07.2017.
 */
public abstract class BasePermission {

    private final int id;
    private final int requiredPower;
    private final String name;

    public BasePermission(final int i, final int p, final String n){
        id = i;
        requiredPower = p;
        name = n;
    }


    // Forced transformation to BasePermission instance,
    // to replace the '(BasePermission) xx' parsing
    public abstract BasePermission toBase();








}
