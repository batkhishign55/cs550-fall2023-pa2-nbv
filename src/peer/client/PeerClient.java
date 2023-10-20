package src.peer.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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

    public PeerClient(PeerMain peerMain) {
        this.peerMain = peerMain;
    }

    @Override
    public void run() {

        input = new Scanner(System.in);
        while (true) {
            try {
                System.out.println(
                        "[Client]: What do you want to do?\n\t[0] - Search a file\n\t[1] - Obtain a file");
                String inp = input.nextLine();
                switch (inp) {
                    case "0":
                        search();
                        break;
                    case "1":
                        obtain();
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
}
