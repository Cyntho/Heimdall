package org.cyntho.ts.heimdall.net;

import org.cyntho.ts.heimdall.net.streaming.IDataStream;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.cyntho.ts.heimdall.app.Bot.DEBUG_MODE;
import static org.cyntho.ts.heimdall.net.NetSendObjectType.*;

public final class NetSendObject<T extends Serializable> implements IDataStream {

    // Wrapper of the actual content
    private Map<String, NetSendObjectContent> content;

    // Constructor
    public NetSendObject() {
        content = new HashMap<>();
    }

    // Public SET
    public void set(String ref, NetSendObjectContent value){
        content.put(ref, value);
    }


    public void setInteger(String ref, int value){
        content.put(ref, new NetSendObjectContent(INTEGER, value));
    }

    public void setLong(String ref, long value){
        content.put(ref, new NetSendObjectContent(LONG, value));
    }

    public void setFloat(String ref, float value){
        content.put(ref, new NetSendObjectContent(FLOAT, value));
    }

    public void setDouble(String ref, double value){
        content.put(ref, new NetSendObjectContent(DOUBLE, value));
    }

    public void setChar(String ref, char value){
        content.put(ref, new NetSendObjectContent(CHAR, value));
    }

    public void setString(String ref, String value){
        content.put(ref, new NetSendObjectContent(STRING, value));
    }

    public void setBoolean(String ref, boolean value){
        content.put(ref, new NetSendObjectContent(BOOLEAN, value));
    }

    public void setObject(String ref, NetSendObject value){ content.put(ref, new NetSendObjectContent(NET_SEND_OBJECT, value)); }

    public void setSerialized(String ref, T value)  {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
            content.put(ref, new NetSendObjectContent(SERIALIZED, Base64.getEncoder().encodeToString(baos.toByteArray())));
        } catch (IOException e){
            // ignored for now
        }

    }




    /* ------------------------------------------------------------------ */

    // Public GET methods
    public int getInt(String ref){

        NetSendObjectContent tmp = content.get(ref);

        if (tmp != null){
            if (tmp.getType() == INTEGER){
                try {
                    return (Integer) tmp.getValue();
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        throw new NullPointerException();
    }

    public long getLong(String ref){
        NetSendObjectContent tmp = content.get(ref);

        if (tmp != null){
            if (tmp.getType() == LONG){
                try {
                    return (Long) tmp.getValue();
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        throw new NullPointerException();
    }

    public float getFloat(String ref){
        NetSendObjectContent tmp = content.get(ref);
        if (tmp != null){
            if (tmp.getType() == FLOAT){
                try {
                    return (float) tmp.getValue();
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        throw new NullPointerException();
    }

    public double getDouble(String ref){

        NetSendObjectContent tmp = content.get(ref);
        if (tmp != null){
            if (tmp.getType() == DOUBLE){
                try {
                    return (Double) tmp.getValue();
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        throw new NullPointerException();
    }

    public float getChar(String ref){
        NetSendObjectContent tmp = content.get(ref);
        if (tmp != null){
            if (tmp.getType() == CHAR){
                try {
                    return (char) tmp.getValue();
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        throw new NullPointerException();
    }

    public String getString(String ref){
        NetSendObjectContent tmp = content.get(ref);
        if (tmp != null){
            if (tmp.getType() == STRING){
                return tmp.getValue().toString();
            } else {
                throw new IllegalArgumentException();
            }
        }
        throw new NullPointerException();
    }

    public boolean getBoolean(String ref){
        NetSendObjectContent tmp = content.get(ref);
        if (tmp != null){
            if (tmp.getType() == BOOLEAN){
                try {
                    return (Boolean) tmp.getValue();
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        throw new NullPointerException();
    }

    public NetSendObject getObject(String ref){
        NetSendObjectContent tmp = content.get(ref);
        if (tmp != null){
            if (tmp.getType() == NET_SEND_OBJECT){
                try {
                    return (NetSendObject) tmp.getValue();
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
        throw new NullPointerException();
    }

    public Object getSerialized(String ref){
        NetSendObjectContent tmp = content.get(ref);
        if (tmp != null){
            if (tmp.getType() == SERIALIZED){
                try {
                    byte[] data = Base64.getDecoder().decode(tmp.getValue().toString());
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                    Object o = ois.readObject();
                    ois.close();
                    if (o != null){
                        return o;
                    }
                } catch (Exception e){
                    if (DEBUG_MODE){
                        System.out.println("Error casting object: ");
                        e.printStackTrace();
                    }
                    throw new IllegalArgumentException();
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
        throw new NullPointerException();
    }

    /* ---------------------------------------------------------------- */



    public Map<String, NetSendObjectContent> getContent() {
        return content;
    }

    /* IDataStream implementations */

    public String generateDataStream(){

        StringBuilder sb = new StringBuilder();

        // Start of NetSendObject
        sb.append(OBJ_START);
        int counter = content.size();

        for (Map.Entry<String, NetSendObjectContent> current : content.entrySet()){

            if (current.getValue().getType() == NET_SEND_OBJECT){
                NetSendObject child = (NetSendObject) current.getValue().getValue();
                sb.append(current.getKey()).append(SPLITTER);
                sb.append(current.getValue().getType()).append(SPLITTER);
                sb.append(child.generateDataStream());
            } else {
                sb.append(current.getKey()).append(SPLITTER);
                sb.append(current.getValue().getType()).append(SPLITTER);
                sb.append(current.getValue().getValue());
            }

            if (counter > 0){
                sb.append(SEPARATOR);
                counter--;
            }
        }

        // End of NetSendObject
        sb.append(OBJ_END);
        return sb.toString();
    }

    public void buildFromDataStream(String stream){
        content = new HashMap<>();


        while (stream.length() != 0){
            int startIndex = stream.indexOf(OBJ_START);

            stream = stream.replaceFirst(OBJ_START, "");

            String key = stream.substring(0, stream.indexOf(SPLITTER));
            stream = stream.substring(stream.indexOf(SPLITTER));

            String typeRaw = stream.substring(0, stream.indexOf(SPLITTER));
            stream = stream.substring(stream.indexOf(SPLITTER));

            System.out.println("key: " + key);
            System.out.println("type: " + typeRaw);

            NetSendObjectType type = NetSendObjectType.parseFromString(typeRaw);
            System.out.println(type);

            if (type != NET_SEND_OBJECT){
                content.put(key, new NetSendObjectContent(type, stream.substring(0, stream.indexOf(SEPARATOR))));
            } else {
                NetSendObject child = new NetSendObject();
                child.buildFromDataStream(stream);
                content.put(key, new NetSendObjectContent(type,  child));
            }







        }


    }

























}
