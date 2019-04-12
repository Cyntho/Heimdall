package org.cyntho.ts.heimdall.net.streaming;

import org.cyntho.ts.heimdall.net.NetSendObject;

import java.io.Serializable;

public interface ISendAble<T> extends Serializable {

    T generateFromNetSendObject(NetSendObject sendObject);
    NetSendObject createNetSendObject();
}
