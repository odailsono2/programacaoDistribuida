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
        StringRequest requestTest= new StringRequest(1,response.getBytes());
        RequestOrResponse reqRespTest = new RequestOrResponse(requestTest,1);
        clientConnection.respond(reqRespTest);   
    }

}

// classe usada para encapsular os dados da requisição do clinete com um ID de mensagens entre servidores
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

        // Converte o StringRequest em um array de bytes (pode incluir requestId + data)
        public byte[] toBytes() {
            byte[] requestIdBytes = Integer.toString(requestId).getBytes();
            byte[] result = new byte[requestIdBytes.length + data.length];
            System.arraycopy(requestIdBytes, 0, result, 0, requestIdBytes.length);
            System.arraycopy(data, 0, result, requestIdBytes.length, data.length);
            return result;
        }

}

// Classe com a requisição do cliente: id da requisição o dados enviados
class StringRequest {
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
    public byte[] toBytes() {
        byte[] requestIdBytes = Integer.toString(requestId).getBytes();
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
