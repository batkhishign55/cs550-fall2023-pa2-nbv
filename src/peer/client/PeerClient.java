package src.peer.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import src.peer.PeerMain;
import src.peer.entity.AddressEntity;
import src.peer.entity.FileHolderEntity;
import src.peer.entity.PeerEntity;
import src.peer.entity.SearchMessageEntity;

public class PeerClient extends Thread {

    private Socket socket = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;
    private Scanner input = null;
    private PeerMain peerMain;
    List<Long> timeList = new ArrayList<>();

    public PeerClient(PeerMain peerMain) {
        this.peerMain = peerMain;
    }

    @Override
    public void run() {

        int idx = 0;
        for (int i = 0; i < 16; i++) {
            String peerId = String.format("peer%d", i + 1);
            if (!peerMain.getProp().getProperty("id").equals(peerId)) {
                System.out.println(peerId);
                this.peers[idx] = peerId;
                idx += 1;
            }
        }

        input = new Scanner(System.in);
        while (true) {
            try {
                System.out.println(
                        "[Client]: What do you want to do?\n\t[0] - Search a file\n\t[1] - Obtain a file\n\t[2] - Replicate");
                String inp = input.nextLine();
                switch (inp) {
                    case "0":
                        search();
                        break;
                    case "1":
                        obtain();
                        break;
                    case "2":
                        replicate();
                    case "3":
                        benchmark();
                        break;
                    default:
                        System.out.println("[Client]: Unknown option!");
                        break;
                }
            } catch (IOException e) {
                System.out.println("[Client]: Encountered an error!");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("[Client]: Encountered an error!");
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void search() throws UnknownHostException, IOException {

        System.out.print("[Client]: File name:");
        String fileName = input.nextLine();
        if (fileName == null || fileName == "") {
            return;
        }

        UUID uuid = UUID.randomUUID();
        String msgId = String.format("%s:%s", peerMain.getProp().getProperty("id"), uuid.toString());

        SearchMessageEntity msg = new SearchMessageEntity(msgId);
        msg.setFileName(fileName);
        msg.setIsOrigin(true);
        peerMain.addMsg(msg);

        for (PeerEntity peer : peerMain.getPeers()) {
            System.out.println(
                    String.format("[Client]: Searching in %s %s:%d", peer.getId(), peer.getIp(), peer.getPort()));

            // establish a connection
            socket = new Socket(peer.getIp(), peer.getPort());
            System.out.println(String.format("[Client]: Connected to %s!", peer.getId()));

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // send request type
            out.writeUTF("search");
            // send msgId
            out.writeUTF(msgId);
            // send ttl
            int ttl = Integer.parseInt(peerMain.getProp().getProperty("ttl"));
            out.writeUTF(String.valueOf(ttl - 1));
            // send fileName
            out.writeUTF(fileName);
            // send current peerId
            out.writeUTF(peerMain.getProp().getProperty("id"));
        }
    }

    private void obtain() throws Exception {

        System.out.print("[Client]: Enter fileName to obtain:");
        String fileName = input.nextLine();

        FileHolderEntity holder = peerMain.findHolder(fileName);
        System.out.println("[Client]: Enter index of the server");

        for (int i = 0; i < holder.getAddresses().size(); i++) {
            AddressEntity addr = holder.getAddresses().get(i);
            System.out.println(String.format("\t[%d]: %s:%d", i, addr.getIp(), addr.getPort()));
        }
        String inp = input.nextLine();

        AddressEntity addr = holder.getAddresses().get(Integer.parseInt(inp));
        // establish a connection
        socket = new Socket(addr.getIp(), addr.getPort());

        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        // send fileName
        out.writeUTF("obtain");
        out.writeUTF(fileName);

        int bytesRead;
        byte[] buffer = new byte[1024];
        try (FileOutputStream fos = new FileOutputStream(String.format("./files/%s", fileName))) {
            while ((bytesRead = in.read(buffer)) > 0) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("[Client]: Download successful!");
    }

    private void replicate() throws Exception {
        String repls = peerMain.getProp().getProperty("repl");
        String[] peerIds = repls.split(",");

        for (String peerId : peerIds) {

            PeerEntity peer = peerMain.findPeer(peerId);
            System.out.println(
                    String.format("[Client]:Replicating in %s %s:%d", peer.getId(), peer.getIp(), peer.getPort()));
            // establish a connection
            socket = new Socket(peer.getIp(), peer.getPort());

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("replicate");

            // search the file in local directory
            File folder = new File("./files");
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                out.writeUTF(file.getName());
                byte[] bytes = Files.readAllBytes(Paths.get("./files/", file.getName()));
                out.writeUTF(String.valueOf(bytes.length));
                out.write(bytes);
                out.flush();
            }

            out.writeUTF("end");

            socket.close();
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            System.out.println("[Client]:Replication sucessful");

        }
    }

    private void benchmark() throws UnknownHostException, IOException {

        System.out.println("[Client]: Which benchmark do you want to run:\n\t[0] - Search\n\t[1] - Download");
        this.testType = Integer.parseInt(input.nextLine());
        System.out.println("[Client]: Small dataset or large dataset:\n\t[0] - Small\n\t[1] - Large");
        this.fileSize = Integer.parseInt(input.nextLine());

        if (this.testType == 1) {

        }

        peerMain.startTest();
        // establish a connection
        for (int i = 0; i < peerMain.getTestSize(); i++) {
            this.sendRequest();
        }
        System.out.println("Time Taken Per Request: " + this.timeList);
        peerMain.endTest();
    }

    private void sendRequest() throws UnknownHostException, IOException {
        // record start time
        long startTime = System.nanoTime();

        Socket socket = null;
        DataOutputStream out = null;

        UUID uuid = UUID.randomUUID();

        String msgId = String.format("%s:%s", peerMain.getProp().getProperty("id"), uuid.toString());
        String fileName = getRandomFilename();

        SearchMessageEntity msg = new SearchMessageEntity(msgId);
        msg.setFileName(fileName);
        msg.setIsOrigin(true);
        peerMain.addMsg(msg);
        for (PeerEntity peer : peerMain.getPeers()) {
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
            int ttl = Integer.parseInt(peerMain.getProp().getProperty("ttl"));
            out.writeUTF(String.valueOf(ttl - 1));
            // send fileName
            out.writeUTF(fileName);
            // send current peerId
            out.writeUTF(peerMain.getProp().getProperty("id"));
        }

        // calculating the time in milliseconds
        long elapsedTimeMillis = (System.nanoTime() - startTime) / 1000000;
        this.timeList.add(elapsedTimeMillis);

    }

    private static final String[] sizes = { "10KB", "100MB" };
    private static final int[] sizeLimits = { 100000, 10 };
    private static final Random random = new Random();
    private int testType;
    private int fileSize;
    private String[] peers = new String[15];

    private String getRandomFilename() {
        String size = sizes[this.fileSize];
        int limit = sizeLimits[this.fileSize];
        String value = String.format("%06d", random.nextInt(limit));
        String extension = ".txt";
        String peer = this.peers[random.nextInt(this.peers.length)];
        return size + "_" + value + "_" + peer + extension;
    }
}
