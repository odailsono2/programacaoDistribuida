package Patterns.RWL;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientConnection {
    private InetAddress address;
    private int porta;
    private DatagramPacket datagramPacket;
    private DatagramSocket socketClient;


    public void setPorta(final int porta) {
        this.porta = porta;
    }

    public int getPort() {
        return porta;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(final String address) throws UnknownHostException, SocketException {
        setSocketClient(new DatagramSocket());
        this.address = InetAddress.getByName(address);
    }

    /// não usar daqui pra baixo
    public void setDatagramPacket(final DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    public DatagramSocket getSocketClient() {
        return socketClient;
    }
    
    public void setSocketClient(final DatagramSocket socketClient) {
        this.socketClient = socketClient;
    }

    public void send(final String mensagem) throws IOException{
        
        setDatagramPacket(new DatagramPacket(mensagem.getBytes(),mensagem.getBytes().length,getAddress(),getPort()));
        setSocketClient(new DatagramSocket());
        socketClient.send(datagramPacket);
    }

    public void close(){
        if (getSocketClient() != null && !getSocketClient().isClosed()) {
            getSocketClient().close();
        }
    }

    public String receive() throws IOException{
        // Buffer para receber a resposta do servidor
        final byte[] receiveData = new byte[1024];

        // Pacote UDP para receber a resposta
        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        final String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
        return serverResponse;
        

    }


}
