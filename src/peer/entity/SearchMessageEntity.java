package src.peer.entity;

public class SearchMessageEntity {

    private final String msgId;
    private String upstreamId;
    private String upstreamIp;
    private int upstreamPort;
    private String fileName;
    private boolean isOrigin;

    public String getUpstreamId() {
        return this.upstreamId;
    }

    public void setUpstreamId(String upstreamId) {
        this.upstreamId = upstreamId;
    }

    public void setIsOrigin(boolean isOrigin) {
        this.isOrigin = isOrigin;
    }

    public SearchMessageEntity(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public String getUpstreamIp() {
        return this.upstreamIp;
    }

    public void setUpstreamIp(String upstreamIp) {
        this.upstreamIp = upstreamIp;
    }

    public int getUpstreamPort() {
        return this.upstreamPort;
    }

    public void setUpstreamPort(int upstreamPort) {
        this.upstreamPort = upstreamPort;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isIsOrigin() {
        return this.isOrigin;
    }

    public boolean getIsOrigin() {
        return this.isOrigin;
    }
}