package Patterns.RWL;

import java.net.*;
import java.io.*;

public class ServerNode {

    private String nodeId;
    private String status;
    private String address;
    private int porta;
    private boolean lider;

    public ServerNode() {
    }

    public ServerNode(String nodeId, String status, String address, int porta, boolean lider) {
        this.nodeId = nodeId;
        this.status = status;
        this.address = address;
        this.porta = porta;
        this.lider = lider;
    }



    public ServerNode(String address, int port) {
        this.address = address;
        this.porta = port;
    }


    public void setLider(Boolean lider) {
        this.lider = lider;
    }

    public Boolean getLider() {
        return lider;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getAddress() {
        return address;
    }
    public String getNodeId() {
        return nodeId;
    }
    public int getPorta() {
        return porta;
    }
    public String getStatus() {
        return status;
    }



    public static void main(String[] args) {
        var address = "localhost";
        try {
            ServerNode node1 = new ServerNode();
            node1.setAddress(address);

            var socket = new Socket(InetAddress.getByName(address),8080);

            Connection.send("cliente:criar;1".getBytes(), socket);

            var mensagem = Connection.receiveData(socket);


            System.out.println(new String(mensagem));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
