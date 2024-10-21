package Patterns.RWL;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * InnerHeartBeat
 */
public class Beating implements Serializable {

    private String nodeId;
    private long timestamp;
    private String status;
    private String address;
    private int porta;

    public Beating(String nodeId, long timestamp, String status, String address, int porta) {
        this.nodeId = nodeId;
        this.timestamp = timestamp;
        this.status = status;
        this.address = address;
        this.porta = porta;
    }

    public String getNodeId() {
        return nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }

    public int getPorta() {
        return porta;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Converte o StringRequest em um array de bytes (pode incluir requestId + data)
    public byte[] toBytes() {

        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            // Serializar o objeto para um array de bytes
            byteArrayOutputStream = new ByteArrayOutputStream();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    new BufferedOutputStream(byteArrayOutputStream));

            objectOutputStream.writeObject(this); // Serializa o objeto

            objectOutputStream.flush(); // Garante que todos os dados sejam gravados

            objectOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public String toString() {
        return "Beating [nodeId=" + nodeId + ", timestamp=" + timestamp + ", status=" + status + ", address=" + address
                + ", porta=" + porta + "]";
    }


}