import java.net.DatagramPacket;
import java.net.InetAddress;

public class Client{

	private int port;
	private InetAddress adress;
    private String request;

	Client(DatagramPacket clientReceivedPacket){
		port = clientReceivedPacket.getPort();
		adress = clientReceivedPacket.getAddress();
        request = clientReceivedPacket.getData().toString();
	}

    public String getRequest() {
        return request;
    }
	public int getPort() {
		return port;
	}

	public InetAddress getAdress() {
		return adress;
	}


	public DatagramPacket respond(RequestOrResponse reply){
		byte[] replymsg = reply.getRequest().getData();
		return new DatagramPacket(replymsg,replymsg.length,adress,port);
	}
}