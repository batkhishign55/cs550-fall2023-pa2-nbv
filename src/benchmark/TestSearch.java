package src.benchmark;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import src.peer.entity.PeerEntity;

public class TestSearch {

    private static final String[] sizes = { "10KB", "100MB" };
    private static final int[] sizeLimits = { 100000, 10 };
    private static final Random random = new Random();
    List<Long> timeList = new ArrayList<>();
    Properties prop;
    private int testType;
    ArrayList<PeerEntity> peers = new ArrayList<>();

    private String getRandomFilename() {
        String size = sizes[this.testType];
        int limit = sizeLimits[this.testType];
        String value = String.format("%06d", random.nextInt(limit));
        String extension = ".txt";
        PeerEntity peer = peers.get(random.nextInt(peers.size()));
        return size + "_" + value + "_" + peer.getId() + extension;
    }

    public TestSearch(String testType) {
        this.testType = Integer.parseInt(testType);

        prop = new Properties();
        try (FileInputStream fis = new FileInputStream("app.config")) {
            prop.load(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String peersString = prop.getProperty("peers");
        if (peersString != null) {
            String[] peerStrings = peersString.split(",");
            for (String peerString : peerStrings) {
                String[] parts = peerString.split(":");
                if (parts.length == 3) {
                    peers.add(new PeerEntity(parts[0], parts[1], Integer.parseInt(parts[2])));
                }
            }
        } else {
            System.out.println("No peers found in the configuration file.");
        }

        // record start time
        long startTime = System.nanoTime();

        // establish a connection
        for (int i = 0; i < 1; i++) {
            this.sendRequest();
        }
        // record end time
        long endTime = System.nanoTime();

        // calculating the time in milliseconds
        long elapsedTimeMillis = (endTime - startTime) / 1000000;

        System.out.println("Total Time Taken: " + elapsedTimeMillis + "ms");
        System.out.println("Time Taken Per Request: " + this.timeList);

    }

    private void sendRequest() {
        // record start time
        long startTime = System.nanoTime();

        Socket socket = null;
        DataOutputStream out = null;

        try {

            UUID uuid = UUID.randomUUID();
            String msgId = String.format("%s:%s", this.prop.getProperty("id"), uuid.toString());

            for (PeerEntity peer : peers) {
                System.out.println(
                        String.format("[Client]: Searching in %s %s:%d", peer.getId(), peer.getIp(), peer.getPort()));

                // establish a connection
                socket = new Socket(peer.getIp(), peer.getPort());
                System.out.println(String.format("[Client]: Connected to %s!", peer.getId()));

                out = new DataOutputStream(socket.getOutputStream());

                // send request type
                out.writeUTF("search");
                // send msgId
                out.writeUTF(msgId);
                // send ttl
                int ttl = Integer.parseInt(prop.getProperty("ttl"));
                out.writeUTF(String.valueOf(ttl - 1));
                // send fileName
                out.writeUTF(getRandomFilename());
                // send current peerId
                out.writeUTF(prop.getProperty("id"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // calculating the time in milliseconds
        long elapsedTimeMillis = (System.nanoTime() - startTime) / 1000000;
        this.timeList.add(elapsedTimeMillis);

    }

    public static void main(String args[]) {
        new TestSearch(args[0]);
    }
}
