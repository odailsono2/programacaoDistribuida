
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node implements ServerNode {
    private InetAddress adress;
    private int port;

    public Node(InetAddress adress, int port) {
        this.adress = adress;
        this.port = port;
    }

    @Override
    public InetAddress getAddress() {
        return adress;
    }

    public int getPort() {
        return port;
    }
    @Override
    public void setAdress(String adress) {

        try {
            this.adress = InetAddress.getByName(adress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
        
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    

}
