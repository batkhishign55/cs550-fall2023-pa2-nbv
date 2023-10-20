package src.peer.entity;

public class AddressEntity {

    private String ip;
    private int port;

    public AddressEntity(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Boolean equals(AddressEntity another) {
        if (this.ip.equals(another.getIp()) && this.port == another.getPort()) {
            return true;
        }
        return false;
    }

}