package src.peer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import src.peer.PeerMain;

public class PeerServer extends Thread {

    private ServerSocket server = null;
    private PeerMain peerMain;

    public PeerServer(PeerMain peerMain) {
        this.peerMain = peerMain;
    }

    @Override
    public void run() {

        try {
            System.out.println(peerMain.getProp().getProperty("port"));
            server = new ServerSocket(Integer.parseInt(peerMain.getProp().getProperty("port")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // starts server and waits for a connection
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("[Server]: Peer accepted");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                Thread clientThread = new PeerClientHandler(peerMain, socket, in, out);
                clientThread.start();

            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }
}
