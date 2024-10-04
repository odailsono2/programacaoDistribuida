import java.io.IOException;

class WriteQuorumCallback
        implements RequestCallback<RequestOrResponse> {

    private final int quorum;
    private volatile int expectedNumberOfResponses;
    private volatile int receivedResponses;
    private volatile int receivedErrors;
    private volatile boolean done;
    private final RequestOrResponse request;
    private final Client clientConnection;

    public WriteQuorumCallback(int totalExpectedResponses,
            RequestOrResponse clientRequest,
            Client clientConnection) {
        this.expectedNumberOfResponses = totalExpectedResponses;
        this.quorum = expectedNumberOfResponses / 2 + 1;
        this.request = clientRequest;
        this.clientConnection = clientConnection;

    }

    @Override
    public void onResponse(RequestOrResponse r) {
        receivedResponses++;

        if (receivedResponses == quorum && !done) {
            respondToClient("Success");
            done = true;
        }
    }

    @Override
    public void onError(Throwable e) {
        receivedErrors++;
        if (receivedErrors == quorum && !done) {
            respondToClient("Error");
            done = true;
        }
    }

    private void respondToClient(String response) {
        clientConnection.respond(new RequestOrResponse(
                                    new StringRequest(
                                        RequestId.SetValueResponse, 
                                        response.getBytes()),
                                    request.getCorrelationId()));   
    }

}


class RequestOrResponse {
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

    // Converte o RequestOrResponse em bytes para envio
    public byte[] toBytes() {
        byte[] requestBytes = request.toBytes();
        byte[] correlationIdBytes = Integer.toString(correlationId).getBytes();
        
        // Combina os dados da requisição com o ID de correlação
        byte[] result = new byte[requestBytes.length + correlationIdBytes.length];
        System.arraycopy(requestBytes, 0, result, 0, requestBytes.length);
        System.arraycopy(correlationIdBytes, 0, result, requestBytes.length, correlationIdBytes.length);
        
        return result;
    }
}

class StringRequest {
    private RequestId requestId;
    private byte[] data;

    public StringRequest(RequestId requestId, byte[] data) {
        this.requestId = requestId;
        this.data = data;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public byte[] getData() {
        return data;
    }

    // Converte o StringRequest em um array de bytes (pode incluir requestId + data)
    public byte[] toBytes() {
        // Exemplo simples, em um caso real, você pode querer serializar de forma mais elaborada
        byte[] requestIdBytes = requestId.name().getBytes();
        byte[] result = new byte[requestIdBytes.length + data.length];
        System.arraycopy(requestIdBytes, 0, result, 0, requestIdBytes.length);
        System.arraycopy(data, 0, result, requestIdBytes.length, data.length);
        return result;
    }
}

enum RequestId {
    SetValueRequest,
    SetValueResponse,
    GetValueRequest,
    GetValueResponse;
}
