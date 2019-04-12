package org.cyntho.ts.heimdall.app;


import java.util.Scanner;


public class Tester {

    private static boolean stopRequest = false;

    public static void main(String[] args) {

        System.out.println("Starting from Test env.");

        TestRunner runner = new TestRunner();
        runner.start("bot");


    }

    private static class TestRunner implements Runnable {

        Thread instance;
        Heimdall heimdall = new Heimdall();

        @Override
        public void run() {
            heimdall.start();
            while (!stopRequest){
                handleDirectInput();
            }
            heimdall.stop();
        }

        public void start(String name){
            if (instance == null){
                instance = new Thread(this, name);
                instance.start();
            }
        }
    }


    private static void handleDirectInput(){

        // Some spaghetti code just for debugging here
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("shutdown")){
            stopRequest = true;
        }
        scanner.close();
    }
}
