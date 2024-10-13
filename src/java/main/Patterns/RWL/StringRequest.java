package Patterns.RWL;

import java.io.*;
import java.util.Arrays;

// Classe com a requisição do cliente: id da requisição o dados enviados
public class StringRequest implements Serializable {
    // private static final long serialVersionUID = 1L;
    private int requestId;
    private byte[] data;
    private int correlationID;

    public StringRequest(int requestId, byte[] data) {
        this.requestId = requestId;
        this.data = data;
    }

    public int getRequestId() {
        return requestId;
    }

    public byte[] getData() {
        return data;
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

    public int getCorrelationId() {
        return correlationID;
    }

}