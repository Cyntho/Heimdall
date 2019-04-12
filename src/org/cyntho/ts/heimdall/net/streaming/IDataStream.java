package org.cyntho.ts.heimdall.net.streaming;

public interface IDataStream {

    long serialVersionUID = 2L;

    String OBJ_START      = "|#~OBJ_START~#|";
    String OBJ_END        = "|#~OBJ_END~#|";
    String SEPARATOR      = "|#~SEPARATOR~#|";
    String SPLITTER       = "|#~SPLITTER~#|";

    /*
    String OBJ_START      = "|#~1d0da1fa3cc62359c3b0cc0fb1a8e3c2~#|";
    String OBJ_END        = "|#~c04e03444268e7af60a13a9684732e50~#|";
    String SEPARATOR      = "|#~70bb8dc90aa233a66b91ae2e9dc1a850~#|";
    String SPLITTER       = "|#~d5c1bfb5bb6a461871b68c0c0d465b84~#|";
     */

    String generateDataStream();
    void buildFromDataStream(String stream) throws IndexOutOfBoundsException, NumberFormatException;


}
