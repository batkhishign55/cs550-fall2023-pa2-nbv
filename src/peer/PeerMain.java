package src.peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import src.peer.client.PeerClient;
import src.peer.entity.AddressEntity;
import src.peer.entity.FileHolderEntity;
import src.peer.entity.PeerEntity;
import src.peer.entity.SearchMessageEntity;
import src.peer.server.PeerServer;

public class PeerMain {

    ArrayList<SearchMessageEntity> msgs = new ArrayList<>();
    public static final Object lock = new Object();
    ArrayList<PeerEntity> peers = new ArrayList<>();
    ArrayList<FileHolderEntity> holders = new ArrayList<>();
    Properties prop;

    public ArrayList<SearchMessageEntity> getMsgs() {
        synchronized (lock) {
            return this.msgs;
        }
    }

    public SearchMessageEntity findMsg(String msgId) throws Exception {
        synchronized (lock) {
            for (SearchMessageEntity msg : this.msgs) {
                if (msgId.equals(msg.getMsgId())) {
                    return msg;
                }
            }
            throw new Exception("msg not found!");
        }
    }

    public void addMsg(SearchMessageEntity msg) {
        synchronized (lock) {
            // clear the messages if it's over 1k
            if (this.msgs.size()> 1000) {
                this.msgs = new ArrayList<>();
            }
            this.msgs.add(msg);
        }
    }

    public ArrayList<FileHolderEntity> getHolders() {
        synchronized (lock) {
            return this.holders;
        }
    }

    public FileHolderEntity findHolder(String fileName) throws Exception {
        synchronized (lock) {
            for (FileHolderEntity holder : this.holders) {
                if (holder.getFileName().equals(fileName)) {
                    return holder;
                }
            }
            throw new Exception("File holder not found!");
        }
    }

    public void addHolder(String fileName, String ip, int port) {
        synchronized (lock) {

            int idx = -1;
            for (int i = 0; i < this.holders.size(); i++) {
                if (this.holders.get(i).getFileName().equals(fileName)) {
                    idx = i;
                }
            }

            if (idx == -1) {
                this.holders.add(new FileHolderEntity(fileName, ip, port));
                return;
            }

            FileHolderEntity holder = this.holders.get(idx);
            ArrayList<AddressEntity> addrs = holder.getAddresses();
            Boolean found = false;
            for (AddressEntity addr : addrs) {
                if (addr.getIp().equals(ip) && addr.getPort() == port) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                addrs.add(new AddressEntity(ip, port));
            }
            holder.setAddresses(addrs);
            this.holders.set(idx, holder);
        }
    }

    public ArrayList<PeerEntity> getPeers() {
        synchronized (lock) {
            return this.peers;
        }
    }

    public PeerEntity findPeer(String peerId) throws Exception {
        synchronized (lock) {
            for (PeerEntity peer : this.peers) {
                if (peer.getId().equals(peerId)) {
                    return peer;
                }
            }
            throw new Exception("Peer not found!");
        }
    }

    public Properties getProp() {
        synchronized (lock) {
            return this.prop;
        }
    }

    public PeerMain() {
        System.out.println("Peer started!");

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

        Thread clientThread = new PeerClient(this);
        clientThread.start();
        Thread serverThread = new PeerServer(this);
        serverThread.start();
    }

    public static void main(String args[]) {
        new PeerMain();
    }
}