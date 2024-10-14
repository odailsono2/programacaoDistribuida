package Patterns.RWL;

import java.io.*;
import java.util.Arrays;

// Classe com a requisição do cliente: id da requisição o dados enviados
public class StringRequest implements Serializable {
    // private static final long serialVersionUID = 1L;
    private String origemAddress;
    private int origemPort;
    private int requestId;
    private byte[] data;
    private long timeStamp;

    public StringRequest(){}

    public StringRequest(int requestId, byte[] data) {
        this.requestId = requestId;
        this.data = data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setOrigemAddress(String origemAddress) {
        this.origemAddress = origemAddress;
    }

    public void setOrigemPort(int origemPort) {
        this.origemPort = origemPort;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getRequestId() {
        return requestId;
    }

    public byte[] getData() {
        return data;
    }

    public String getOrigemAddress() {
        return origemAddress;
    }
    public int getOrigemPort() {
        return origemPort;
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
        return "StringRequest [requestId=" + requestId + ", data=" + Arrays.toString(data) + "]";
    }

}
