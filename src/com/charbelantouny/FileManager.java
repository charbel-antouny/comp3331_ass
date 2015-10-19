package com.charbelantouny;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Charbel Antouny.
 * This class manages file requests and responses.
 */
public class FileManager {
    public FileManager (int peer, int suc1) {
        this.peer = peer;
        this.suc1 = suc1;
    }

    private final String lhost = "localhost";
    private int peer;
    private int suc1;

    public void sendReceive () throws Exception {

        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket server = new ServerSocket(50000+peer);
                    while (true) {
                        Socket connection = server.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String message = in.readLine();

                        // TODO NEED TO MODIFY TO CHECK IF RESPONSE

                        Pattern p = Pattern.compile(".*?(\\d+).*?(\\d+).*");
                        Matcher m = p.matcher(message);
                        m.matches();
                        int file = Integer.parseInt(m.group(1));
                        int origin = Integer.parseInt(m.group(2));
                        //

                        if (hash(file) < suc1) {
                            System.out.println("    File "+file+" is here.\n" +
                                    "    A response message, destined for peer "+origin+", has been sent.");
                            // TODO
                        } else {
                            System.out.println("    File "+file+" is not stored here.\n" +
                                    "    File request message has been forwarded to my successor.");
                            // TODO
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listenThread.start();

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // Create a scanner to read from STDIN
                    Scanner scanner = new Scanner(System.in);
                    String line = scanner.nextLine();

                    if (line.contains("request")) {
                        Pattern p = Pattern.compile(".*?(\\d+).*");
                        Matcher m = p.matcher(line);
                        m.matches();
                        int file = Integer.parseInt(m.group(1));
                        try {
                            Socket s = new Socket(lhost, 50000+suc1);
                            String message = line.trim() + " original " + peer;
                            DataOutputStream out = new DataOutputStream(s.getOutputStream());
                            out.writeBytes(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("    File request message for " + file + " has been sent to my successor.");
                    }
                }
            }
        });
        sendThread.start();

    }

    private int hash (int file) {
        file += 1;
        file = file % 256;
        return file;
    }
}
