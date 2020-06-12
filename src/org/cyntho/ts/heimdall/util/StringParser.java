package org.cyntho.ts.heimdall.util;

import java.io.*;
import java.util.*;

/**
 * Some functionality to parse Strings into different formats
 *
 * @author  Xida
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class StringParser {

    public static String longToDateString(long val){

        long iYears, iDays, iHours, iMin;

        iYears = val / 31536000;
        val -= (iYears * 31536000);

        iDays = val / 86400;
        val -= (iDays * 86400);

        iHours = val / 3600;
        val -= (iHours * 3600);

        iMin = val / 60;
        val -= (iMin * 60);

        if (iYears > 1){
            return String.format("%02d:%03d:%02d:%02d:%02d", iYears, iDays, iHours, iMin, val);
        } else if (iDays > 1){
            return String.format("%03d:%02d:%02d:%02d", iDays, iHours, iMin, val);
        } else {
            return String.format("%02d:%02d:%02d", iHours, iMin, val);
        }
    }

    public static String convertToTeamspeakName(String raw){

         /*
            5C%5C%5B    == [
            5C%5C%5D    == ]
            20          == space
            7B          == {
            28          == (
            -           == -
            _           == _
            2B          == +
            3D          == =
            26          == &
            21          == !
            24          == $
            25          == %
            5E          == ^
            2A          == *
            \\          == \
         */

        Map<String, Character> map = new HashMap<>();
        map.put("5C%5C%5B", '[');
        map.put("5C%5C%5D", ']');
        map.put("%20", ' ');
        map.put("%7B", '{');
        map.put("%28", '(');
        map.put("%2B", '+');
        map.put("%3D", '=');
        map.put("%26", '&');
        map.put("%21", '!');
        map.put("%24", '$');
        map.put("%25", '%');
        map.put("%5E", '^');
        map.put("%2A", '*');
        map.put("\\\\", '\\');

        for (Map.Entry<String, Character> entry : map.entrySet()){
            raw = replace(raw, entry.getKey(), entry.getValue());
        }

        return raw;
    }

    public static String replace(String content, String replacement, char c){

        StringBuilder sb = new StringBuilder();

        // Find each occurrence of the search char 'c'
        for (int i = 0; i < content.length(); i++){
            char current = content.charAt(i);
            if (current == c){
                sb.append(replacement);
            } else {
                sb.append(current);
            }
        }

        return sb.toString();
    }


    public static String serializeMap(Serializable o){

        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);

            oos.writeObject(o);

            oos.close();

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch (IOException ioe) {
            //ignored
        }

        return null;
    }

    public static Map<String, Object> deserializeMap(String input) throws IOException, ClassNotFoundException{

        byte[] data = Base64.getDecoder().decode(input);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));

        Map<String, Object> map = null;

        try {
            map = (Map<String, Object>) ois.readObject();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            ois.close();
        }

        return (map);
    }


    public static String getRandomString(int len){
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }












}
