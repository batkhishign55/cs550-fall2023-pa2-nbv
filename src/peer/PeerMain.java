package src.peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import src.peer.client.PeerClient;
import src.peer.entity.AddressEntity;
import src.peer.entity.FileHolderEntity;
import src.peer.entity.PeerEntity;
import src.peer.entity.SearchMessageEntity;
import src.peer.server.PeerServer;

public class PeerMain {

    ArrayList<SearchMessageEntity> msgs = new ArrayList<>();
    public static final Object lock1 = new Object();
    public static final Object lock2 = new Object();
    public static final Object lock3 = new Object();
    ArrayList<PeerEntity> peers = new ArrayList<>();
    ArrayList<FileHolderEntity> holders = new ArrayList<>();
    Properties prop;
    static int testSize = 10000;
    int testReceived;
    long startTime;

    public int getTestSize() {
        return PeerMain.testSize;
    }

    public void incrementTestReceived() {
        synchronized (lock1) {
            this.testReceived += 1;
            if (this.testReceived > PeerMain.testSize * 0.95) {
                this.endTest();
            }
        }
    }

    public void startTest() {
        synchronized (lock1) {
            this.testReceived = 0;
            startTime = System.nanoTime();
        }
    }

    public void endTest() {
        synchronized (lock1) {
            this.testReceived = 0;
            long elapsedTimeMillis = (System.nanoTime() - startTime) / 1000000;

            String res = "Total Time Taken: " + elapsedTimeMillis + "ms";
            System.out.println(res);
            try (FileOutputStream outputStream = new FileOutputStream("./test_res.txt")) {
                byte[] strToBytes = res.getBytes();
                outputStream.write(strToBytes);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<SearchMessageEntity> getMsgs() {
        synchronized (lock2) {
            return this.msgs;
        }
    }

    public SearchMessageEntity findMsg(String msgId) throws Exception {
        synchronized (lock2) {
            for (SearchMessageEntity msg : this.msgs) {
                if (msgId.equals(msg.getMsgId())) {
                    return msg;
                }
            }
            throw new Exception("msg not found!");
        }
    }

    public void addMsg(SearchMessageEntity msg) {
        synchronized (lock2) {
            // clear the messages if it's over 1k
            if (this.msgs.size() > 10001) {
                System.out.println("clearing msgs.");
                this.msgs.clear();
            }
            this.msgs.add(msg);
        }
    }

    public ArrayList<FileHolderEntity> getHolders() {
        synchronized (lock3) {
            return this.holders;
        }
    }

    public FileHolderEntity findHolder(String fileName) throws Exception {
        synchronized (lock3) {
            for (FileHolderEntity holder : this.holders) {
                if (holder.getFileName().equals(fileName)) {
                    return holder;
                }
            }
            throw new Exception("File holder not found!");
        }
    }

    public void addHolder(String fileName, String ip, int port) {
        synchronized (lock3) {
            if (this.holders.size() > 50) {
                this.holders.clear();
            }

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
        return this.peers;
    }

    public PeerEntity findPeer(String peerId) throws Exception {
        for (PeerEntity peer : this.peers) {
            if (peer.getId().equals(peerId)) {
                return peer;
            }
        }
        throw new Exception("Peer not found!");
    }

    public Properties getProp() {
        return this.prop;
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

        ExecutorService executor = Executors.newFixedThreadPool(15);
        Runnable clientThread = new PeerClient(this);
        executor.execute(clientThread);
        Runnable serverThread = new PeerServer(this);
        executor.execute(serverThread);
    }

    public static void main(String args[]) {
        new PeerMain();
    }
}