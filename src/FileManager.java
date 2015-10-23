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
    public FileManager (int peer, int suc1, int suc2) {
        this.peer = peer;
        this.suc1 = suc1;
        this.suc2 = suc2;
    }

    private final String lhost = "localhost";
    private int peer;
    private int suc1;
    private int suc2;

    /**
     * This is the main method, which contains two threads constantly waiting to send and receive file requests.
     * The send thread listens for user input for a file, while the listen thread waits to receive a file request.
     * @throws Exception
     */
    public void sendReceive () throws Exception {

        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Wait to accept an incoming connection
                    ServerSocket server = new ServerSocket(50000+peer);
                    while (true) {
                        Socket connection = server.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String message = in.readLine();

                        // Process the received message
                        if (message.contains("response")) {
                            System.out.println(message);
                        } else if (message.contains("depart")) {
                            Pattern p = Pattern.compile(".*?(\\d+).*?(\\d+).*?(\\d+)");
                            Matcher m = p.matcher(message);
                            m.matches();
                            int depart = Integer.parseInt(m.group(1));
                            int departSuc1 = Integer.parseInt(m.group(2));
                            int departSuc2 = Integer.parseInt(m.group(3));
                            if (suc1 == depart) {
                                cdht.setSuc1(departSuc1);
                                cdht.setSuc2(departSuc2);
                                suc1 = departSuc1;
                                suc2 = departSuc2;
                            } else {
                                cdht.setSuc2(departSuc1);
                                suc2 = departSuc1;
                            }
                            System.out.println("    Peer "+depart+" will depart from the network.\n" +
                                    "    My first successor is now peer "+suc1+".\n" +
                                    "    My second successor is now peer "+suc2+".");
                        } else {
                            Pattern p = Pattern.compile(".*?(\\d+).*?(\\d+).*");
                            Matcher m = p.matcher(message);
                            m.matches();
                            int file = Integer.parseInt(m.group(1));
                            int origin = Integer.parseInt(m.group(2));

                            if ((hash(file) < suc1) || peer > suc1) {
                                System.out.println("    File " + file + " is here.\n" +
                                        "    A response message, destined for peer " + origin + ", has been sent.");
                                Socket s = new Socket(lhost, 50000 + origin);
                                String reply = "    Received a response message from peer " + peer + ", " +
                                        "which has the file " + file + ".";
                                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                                out.writeBytes(reply);
                                s.close();
                            } else {
                                System.out.println("    File " + file + " is not stored here.\n" +
                                        "    File request message has been forwarded to my successor.");
                                Socket s = new Socket(lhost, 50000 + suc1);
                                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                                out.writeBytes(message);
                                s.close();
                            }
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
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("    File request message for " + file + " has been sent to my successor.");

                    } else if (line.contains("quit")) {
                        int pred1 = cdht.getPred1();
                        int pred2 = cdht.getPred2();
                        // Check if predecessors are in correct order
                        if (pred1 > pred2) {
                            int temp = pred2;
                            pred2 = pred1;
                            pred1 = temp;
                            cdht.setPred1(pred1);
                            cdht.setPred2(pred2);
                        }
                        try {
                            Socket s = new Socket(lhost, 50000+cdht.getPred1());
                            String message = "depart "+peer+" successorOne "+suc1+" successorTwo "+suc2;
                            DataOutputStream out = new DataOutputStream(s.getOutputStream());
                            out.writeBytes(message);
                            s.close();

                            s = new Socket(lhost, 50000+cdht.getPred2());
                            out = new DataOutputStream(s.getOutputStream());
                            out.writeBytes(message);
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        sendThread.start();
    }

    /**
     * This is a helper method used simply to compute the hash of a file name.
     * @param file the file name to be hashed.
     * @return the hashed file name.
     */
    private int hash (int file) {
        file += 1;
        file = file % 256;
        return file;
    }
}
