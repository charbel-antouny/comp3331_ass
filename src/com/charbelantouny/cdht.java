package com.charbelantouny;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

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

        while (true) {
            // Create a packet to receive a request.
            DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
            // Wait for a packet to be received.
            socket.receive(request);
            // Print a message when a packet is received.
            printInfo(request);

            // Implement some kind of delay here using Thread.sleep (ms).

            // Send a response.

            // Also need code for receiving a response.
            // A ping request message was received from Peer 5.
        }
    }

    private static void printInfo (DatagramPacket request) throws Exception {
        // A ping request message was received from Peer 5.

        // http://www.java2s.com/Code/Java/Network-Protocol/UseDatagramSockettosendoutandreceiveDatagramPacket.htm
    }
}
