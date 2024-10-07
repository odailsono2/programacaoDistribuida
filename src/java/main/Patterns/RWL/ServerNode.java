package Patterns.RWL;
import java.net.InetAddress;
import java.net.UnknownHostException;

public interface ServerNode {

    public InetAddress getAddress();
    public void setAdress(String address) throws UnknownHostException;
    public int getPort();
    public void setPort(int port);



}
