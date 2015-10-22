import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright 2015 Charbel Antouny
 * This is a circular DHT network for the COMP3331 assignment.
 */
public class cdht {

    private static int pred1 = -1;
    private static int pred2 = -1;

    public static int getPred1 () {
        return pred1;
    }

    public static int getPred2 () {
        return pred2;
    }

    /**
     * The main function sends ping requests and contains a second thread to listen for responses.
     * @param args contains the identity of the peer and its two successors.
     * @throws Exception
     */
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

        // Create a separate thread to continuously listen for incoming packets
        final DatagramSocket sock = socket;
        final int refPeer = peer;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // Create a packet to receive a request.
                    DatagramPacket dgp = new DatagramPacket(new byte[1024], 1024);
                    // Wait for a packet to be received.
                    try {
                        sock.receive(dgp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Print a message when a packet is received.
                    try {
                        printInfo(dgp,sock, refPeer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        // Pause execution before begin sending and receiving (for visual benefit)
        Thread.sleep(5000); // TODO

        while (true) {
            // Send a ping request to successors
            sendRequest(socket, peer, suc1);
            sendRequest(socket, peer, suc2);

            // Start threads to manage file requests
            FileManager fm = new FileManager(peer, suc1);
            fm.sendReceive();

            // Delay before sending another ping request.
            Thread.sleep(10000);
        }
    }

    /**
     * A helper function that prints requests and responses, and calls the method to send a response if a request is received.
     * @param dgp the packet that contains a response (if applicable).
     * @param s the socket to send a response (if applicable).
     * @param peer identity of this peer.
     * @throws Exception
     */
    private static void printInfo (DatagramPacket dgp, DatagramSocket s, int peer) throws Exception {
        String line = new String(dgp.getData(), 0, dgp.getLength());
        System.out.println(line);
        if (line.contains("request")) {
            Pattern p = Pattern.compile(".*?(\\d+).*");
            Matcher m = p.matcher(line);
            m.matches();
            int id = Integer.parseInt(m.group(1));
            if (pred1 == -1) {
                pred1 = id;
            } else if (pred2 == -1) {
                pred2 = id;
            } // TODO needs to be fixed to cater for case after peer quits
            sendResponse(s, dgp, id, peer);
        }
    }

    /**
     * Helper method that sends a request message to a successor.
     * @param s the socket that sends the request.
     * @param peer identity of this peer.
     * @param suc identity of the successor.
     * @throws Exception
     */
    private static void sendRequest (DatagramSocket s, int peer, int suc) throws Exception {
        byte[] buf;
        String message = "A ping request message was received from Peer " + peer + ".";
        buf = message.getBytes();
        DatagramPacket req = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 50000+suc);
        s.send(req);
    }

    /**
     * A helper method to send a response to a request.
     * @param s socket used to send response.
     * @param dgp packet used to send response.
     * @param id identity of the peer that sent the request.
     * @param peer identity of this peer.
     * @throws Exception
     */
    private static void sendResponse (DatagramSocket s, DatagramPacket dgp, int id, int peer) throws Exception {
        byte[] buf;
        String message = "A ping response message was received from Peer " + peer + ".";
        buf = message.getBytes();
        DatagramPacket res = new DatagramPacket(buf, buf.length, dgp.getAddress(), 50000+id);
        s.send(res);
    }

    /**
     * A helper function that checks to make sure the predecessors of this peer are in the correct order.
     * pred2 should be closest anticlockwise, with pred1 preceding pred2.
     */
    private final void checkPred () {
        if (pred1 > pred2) {
            int temp = pred2;
            pred2 = pred1;
            pred1 = temp;
        }
    }
}