package src.benchmark;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDownload {

    private static final String[] peers = { "peer1", "peer2" };
    private static final String[] sizes = { "1KB", "1MB", "1GB" };
    private static final int[] sizeLimits = { 10000, 1000, 10 };
    private static final Random random = new Random();

    public static String getRandomFilename() {
        String size = sizes[random.nextInt(sizes.length)];
        int limit = sizeLimits[random.nextInt(sizeLimits.length)];
        String value = String.format("%06d", random.nextInt(limit));
        String extension = (size.equals("1KB") || size.equals("1MB")) ? ".txt" : ".bin";
        String peer = peers[random.nextInt(peers.length)];
        return size + "_" + value + "_" + peer + extension;
    }

    public static void main(String args[]) {
        // record start time
        long startTime = System.nanoTime();
        List<Long> TimeList = new ArrayList<>();

        // establish a connection

        for (int i = 0; i < 16000; i++) {
            // record start time
            long startTime1 = System.nanoTime();

            Socket socket = null;
            DataOutputStream out = null;
            DataInputStream in = null;
            try {

                socket = new Socket("127.0.0.1", 8080);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // send request type
                out.writeUTF("search");

                // send fileName
                String fileName = getRandomFilename();
                System.out.println(fileName);
                out.writeUTF(fileName);
                String peer_addr = in.readUTF();
                String msg = "";

                // reads peers from cis until "end" is sent
                while (!msg.equals("end")) {
                    System.out.println(msg);
                    msg = in.readUTF();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                socket.close();

                socket = new Socket(peer_addr.split("\\s+")[1], 6000);

                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // send fileName
                out.writeUTF(fileName);

                int bytesRead;
                byte[] buffer = new byte[1024];
                try (FileOutputStream fos = new FileOutputStream(String.format("./%s", fileName))) {
                    while ((bytesRead = in.read(buffer)) > 0) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                // record end time
                long endTime = System.nanoTime();

                // calculating the time in milliseconds
                long elapsedTimeMillis = (endTime - startTime) / 1000000;

                System.out.println("Time Taken: " + elapsedTimeMillis + "ms");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // record end time
            long endTime = System.nanoTime();

            // calculating the time in milliseconds
            long elapsedTimeMillis = (endTime - startTime1) / 1000000;
            TimeList.add(elapsedTimeMillis);
        }
        // record end time
        long endTime = System.nanoTime();

        // calculating the time in milliseconds
        long elapsedTimeMillis = (endTime - startTime) / 1000000;

        System.out.println("Total Time Taken: " + elapsedTimeMillis + "ms");
        System.out.println("Time Taken Per Request: " + TimeList);

    }
}
