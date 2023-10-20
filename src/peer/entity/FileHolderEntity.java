package src.peer.entity;

import java.util.ArrayList;

public class FileHolderEntity {

    private String fileName;
    private ArrayList<AddressEntity> addresses = new ArrayList<>();

    public FileHolderEntity(String fileName, String ip, int port) {
        this.fileName = fileName;
        this.addresses.add(new AddressEntity(ip, port));
    }

    public FileHolderEntity(String fileName, AddressEntity address) {
        this.fileName = fileName;
        this.addresses.add(address);
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<AddressEntity> getAddresses() {
        return this.addresses;
    }

    public void setAddresses(ArrayList<AddressEntity> addresses) {
        this.addresses = addresses;
    }

}
