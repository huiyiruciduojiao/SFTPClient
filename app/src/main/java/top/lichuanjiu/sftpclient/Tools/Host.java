package top.lichuanjiu.sftpclient.Tools;

public class Host {
    private String name;
    private String address;
    private int port = 22;
    private String userName;
    private String password;
    private String characterSet = "UTF-8";
    private int hostId = -1;

    public Host(String name, String address, int port, String userName, String password, String characterSet,int hostId) {
        this.setName(name);
        this.setAddress(address);
        this.setPort(port);
        this.setUserName(userName);
        this.setPassword(password);
        this.setCharacterSet(characterSet);
        this.setHostId(hostId);
    }

    public Host(String address, int port, String userName, String password) {
        this.setAddress(address);
        this.setPort(port);
        this.setUserName(userName);
        this.setPassword(password);
    }

    public Host(String address, String userName, String password) {
        this.setAddress(address);
        this.setUserName(address);
        this.setPassword(password);
    }

    /**
     * 获得基于当前主机的sftp配置对象
     *
     * @return 返回一个sftp对象
     */
    public SFTPConfigModel getSftpConfig() {
        if (getAddress() != null && getUserName() != null && getPassword() != null) {
            return new SFTPConfigModel(getUserName(), getPassword(), getAddress(), getPort(), getCharacterSet());
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }
}
