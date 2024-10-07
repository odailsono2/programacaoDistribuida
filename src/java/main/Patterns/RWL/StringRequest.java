package Patterns.RWL;

import java.io.Serializable;

// Classe com a requisição do cliente: id da requisição o dados enviados
public class StringRequest implements Serializable{
    // private static final long serialVersionUID = 1L;
    private int requestId;
    private byte[] data;

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
    // public byte[] toBytes() {
    //     byte[] requestIdBytes = Integer.toString(requestId).getBytes();
    //     byte[] result = new byte[requestIdBytes.length + data.length];
    //     System.arraycopy(requestIdBytes, 0, result, 0, requestIdBytes.length);
    //     System.arraycopy(data, 0, result, requestIdBytes.length, data.length);
    //     return result;
    // }
}