package Patterns.RWL;

import java.io.Serializable;

public class RequestOrResponse implements  Serializable{
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
    // public byte[] toBytes() {
    //     byte[] requestIdBytes = Integer.toString(correlationId).getBytes();
    //     byte[] result = new byte[requestIdBytes.length + request.toBytes().length];
    //     System.arraycopy(requestIdBytes, 0, result, 0, requestIdBytes.length);
    //     System.arraycopy(request.toBytes(), 0, result, requestIdBytes.length, request.toBytes().length);
    //     return result;
    //     }
    public static void main(String[] args) {
        // var testStrReq = new StringRequest(1, "Oi".getBytes());
        // var testReqOrResp = new RequestOrResponse(testStrReq, 33);

        // System.out.println(testStrReq.getData() +" , " + testReqOrResp.getRequest().getData());

        // System.out.println(new String(testReqOrResp.toBytes()));

}

    @Override
    public String toString() {
        return "RequestOrResponse [request=" + new String(request.getData()) + ", correlationId=" + correlationId + "]";
    }
}