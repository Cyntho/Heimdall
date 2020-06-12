package org.cyntho.ts.heimdall.app;

import java.util.HashMap;
import java.util.Map;

public class Tester {

    public static void main(String[] args) {

        Map<Integer, Boolean>  map = new HashMap<>();

        map.put(1, true);
        map.put(2, true);


        System.out.println("before: " + map.toString());

        int i = 1;

        map.remove(i);

        for (Map.Entry<Integer, Boolean> entry : map.entrySet()){
            System.out.println("key: " + entry.getKey() + "\tvalue: " + entry.getValue());

        }


    }
}
