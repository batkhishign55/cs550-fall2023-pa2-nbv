package src.peer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import src.peer.PeerMain;
import src.peer.entity.PeerEntity;
import src.peer.entity.SearchMessageEntity;

public class PeerClientHandler extends Thread {

    private DataInputStream in = null;
    private DataOutputStream out = null;
    private Socket socket;
    private PeerMain peerMain;

    // Constructor
    public PeerClientHandler(PeerMain peerMain, Socket socket, DataInputStream in, DataOutputStream out) {
        this.peerMain = peerMain;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        // first message is request type
        try {
            String reqType = in.readUTF();

            switch (reqType) {

                case "search":
                    this.handleSearch();
                    break;
                case "searchRev":
                    this.handleSearchResult();
                    break;
                case "obtain":
                    this.handleObtain();
                    break;
                case "replicate":
                    this.handleReplicate();
                    break;

                default:
                    out.writeUTF("Invalid request");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
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

    private void handleSearch() throws Exception {

        // read file to search
        String msgId = in.readUTF();
        int ttl = Integer.valueOf(in.readUTF());
        String fileName = in.readUTF();
        String upstreamPeerId = in.readUTF();
        System.out.println(upstreamPeerId);

        socket.close();
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }

        // check if search msg is already received
        try {
            peerMain.findMsg(msgId);
            return;
        } catch (Exception e) {
        }

        // extracting peerId from msgId
        System.out.println(
                String.format("[Server]: Incoming search request:\n\t%s, %s, %d", msgId, fileName, ttl));

        PeerEntity upstreamPeer = peerMain.findPeer(upstreamPeerId);
        // register new msg
        SearchMessageEntity msg = new SearchMessageEntity(msgId);
        msg.setUpstreamId(upstreamPeerId);
        msg.setUpstreamIp(upstreamPeer.getIp());
        msg.setUpstreamPort(upstreamPeer.getPort());
        msg.setFileName(fileName);
        peerMain.addMsg(msg);

        // search the file in local directory
        File folder = new File("./files");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.getName().equals(fileName)) {
                System.out
                        .println(
                                String.format("[Server]: Found here. Sending the search result to %s", upstreamPeerId));
                sendSearchResult(msg, peerMain.getProp().getProperty("ip"), peerMain.getProp().getProperty("port"));
                break;
            }
        }

        // if ttl reached 0 stop the search here
        if (ttl == 0) {
            return;
        }

        // searching in peers
        for (PeerEntity peer : peerMain.getPeers()) {

            // don't send it to the upstream
            if (peer.getId().equals(upstreamPeerId)) {
                continue;
            }

            System.out.println(
                    String.format("[Server]: Searching in %s %s:%d", peer.getId(), peer.getIp(), peer.getPort()));

            // establish a connection
            Socket socket = new Socket(peer.getIp(), peer.getPort());

            // DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // send request type
            out.writeUTF("search");
            // send msgId
            out.writeUTF(msg.getMsgId());
            // send ttl
            out.writeUTF(String.valueOf(ttl - 1));
            // send fileName
            out.writeUTF(fileName);
            // send current peerId
            out.writeUTF(peerMain.getProp().getProperty("id"));
        }
    }

    private void sendSearchResult(SearchMessageEntity msg, String peerIP, String peerPort) throws IOException {

        // establish a connection
        Socket socket = new Socket(msg.getUpstreamIp(), msg.getUpstreamPort());

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // send request type
        out.writeUTF("searchRev");
        out.writeUTF(msg.getMsgId());
        out.writeUTF("0");
        out.writeUTF(msg.getFileName());
        out.writeUTF(peerIP);
        out.writeUTF(peerPort);

        socket.close();
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    private void handleSearchResult() throws Exception {

        // read file to search
        String msgId = in.readUTF();
        // ttl
        Integer.valueOf(in.readUTF());
        // filename
        String fileName = in.readUTF();
        String peerIp = in.readUTF();
        String peerPort = in.readUTF();

        socket.close();
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }

        SearchMessageEntity msg = peerMain.findMsg(msgId);

        // if the message is originated here
        if (msg.getIsOrigin()) {
            System.out.println(String.format("[Server]: %s found in: %s:%s", fileName, peerIp, peerPort));
            peerMain.addHolder(fileName, peerIp, Integer.valueOf(peerPort));
            if (!msg.getDidRespond()) {
                msg.setDidRespond(true);
                peerMain.incrementTestReceived();
            }
        } else {
            System.out.println(String.format("[Server]: Forwarding search result: %s.", msg.getMsgId()));
            this.sendSearchResult(msg, peerIp, peerPort);
        }
    }

    private void handleObtain() throws IOException {

        // read file to search
        String fileName = in.readUTF();
        System.out.println(String.format("[Server]: Incoming download request: %s", fileName));

        byte[] bytes = Files.readAllBytes(Paths.get("./files/", fileName));

        out.flush();
        out.write(bytes);

        socket.close();
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    private void handleReplicate() throws IOException {

        // read file to search
        System.out.println("[Server]: Incoming replication request.");

        String msg = in.readUTF();
        while (!msg.equals("end")) {

            int byteSize = Integer.parseInt(in.readUTF());
            int received = 0;
            byte[] buffer = new byte[1024];
            try (FileOutputStream fos = new FileOutputStream(String.format("./files/%s", msg))) {
                while (byteSize > received) {
                    fos.write(buffer, 0, in.read(buffer));
                    received += 1024;
                }
            }
            msg = in.readUTF();
        }
        System.out.println("[Client]: Replication successful!");

    }
}
