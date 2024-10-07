package Patterns.RWL;

import java.io.*;

public class RequestOrResponse implements Serializable {
    // private static final long serialVersionUID = 1L;
    private StringRequest request;
    private int correlationId;

    public RequestOrResponse(StringRequest request, int correlationId) {
        this.request = request;
        this.correlationId = correlationId;
    }

    public StringRequest getRequest() {
        return request;
    }

    public int getCorrelationId() {
        return correlationId;
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

    public static void main(String[] args) {
        // var testStrReq = new StringRequest(1, "Oi".getBytes());
        // var testReqOrResp = new RequestOrResponse(testStrReq, 33);

        // System.out.println(testStrReq.getData() +" , " +
        // testReqOrResp.getRequest().getData());

        // System.out.println(new String(testReqOrResp.toBytes()));

    }

    @Override
    public String toString() {
        return "RequestOrResponse [request=" + new String(request.getData()) + ", correlationId=" + correlationId + "]";
    }
}