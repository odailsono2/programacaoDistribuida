package Patterns.RWL;
import java.io.IOException;

public class ClientConnectionMaker{

    private final ClientConnection clientConnection;

    
    ClientConnectionMaker(){
        clientConnection = new ClientConnection();
    }

    public ClientConnectionMaker setAdress(String address) throws Exception{
        clientConnection.setAddress(address);
        return this;
    }

    public ClientConnectionMaker setClientPort(final int porta){
        clientConnection.setPorta(porta);
        return this;
    }

    public ClientConnection getConection(){
        return clientConnection;
    }

    public static void main(final String[] args) {

        final var clientConnection = new ClientConnectionMaker();
        var conexao = new ClientConnection();


        try {
                
            conexao = clientConnection.setAdress("localhost").
                                            setClientPort(8080).
                                            getConection();

            conexao.send("Ola Vc");
            conexao.send("tudo bem");
            
            var serverResponse = conexao.receive();

            System.out.println("Resposta do servidor: " + serverResponse+" : porta "+ conexao.getPort());

        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            conexao.close();
        }

        
                  
            
    }


}