package Patterns.RWL;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Client{

	private int port;
	private InetAddress adress;
    private String request;

	public Client(DatagramPacket clientReceivedPacket){
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
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(byteArrayOutputStream))) {
			objectOutputStream.writeObject(reply);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] replymsg = byteArrayOutputStream.toByteArray();


		return new DatagramPacket(replymsg,replymsg.length,adress,port);
	}
}