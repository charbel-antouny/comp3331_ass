package com.charbelantouny;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Copyright 2015 Charbel Antouny
 * This is a circular DHT network for the COMP3331 assignment.
 */
public class cdht {

    public static void main(String[] args) throws Exception {
	    if (args.length != 3) {
            System.out.println("3 arguments required.");
            return;
        }
        int peer = Integer.parseInt(args[0]);
        int suc1 = Integer.parseInt(args[1]);
        int suc2 = Integer.parseInt(args[2]);

        // Create a socket for sending and receiving UDP packets.
        DatagramSocket socket = new DatagramSocket(50000+peer);
        // Pause execution before begin sending and receiving (for visual benefit)
        Thread.sleep(10000);

        while (true) {
            // Send a ping request to successors
            sendRequest(socket, suc1);
            Thread.sleep(5000);
            sendRequest(socket, suc2);

            // Create a packet to receive a request.
            DatagramPacket dgp = new DatagramPacket(new byte[1024], 1024);
            // Wait for a packet to be received.
            socket.receive(dgp);
            // Print a message when a packet is received.
            printInfo(dgp,socket);

            // Wait 15 seconds before each ping request.
            Thread.sleep(15000);
        }
    }

    private static void printInfo (DatagramPacket dgp, DatagramSocket s) throws Exception {
        String line = new String(dgp.getData(), 0, dgp.getLength());
        System.out.println(line);
        if (line.contains("request")) {
            sendResponse(s, dgp);
        }
    }

    // method for sending request
    private static void sendRequest (DatagramSocket s, int suc) throws Exception {
        byte[] buf;
        String message = "A ping request message was received from Peer " + suc + ".";
        buf = message.getBytes();
        DatagramPacket req = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 50000+suc);
        s.send(req);
    }

    // method for sending response
    private static void sendResponse (DatagramSocket s, DatagramPacket dgp) throws Exception {
        byte[] buf;
        int peer = dgp.getPort() - 50000;
        String message = "A ping response message was received from Peer " + peer + ".";
        buf = message.getBytes();
        DatagramPacket res = new DatagramPacket(buf, buf.length, dgp.getAddress(), dgp.getPort());
        s.send(res);
    }
}
