static class WriteQuorumCallback
        implements RequestCallback<RequestOrResponse> {

    private final int quorum;
    private volatile int expectedNumberOfResponses;
    private volatile int receivedResponses;
    private volatile int receivedErrors;
    private volatile boolean done;
    private final RequestOrResponse request;
    private final ClientConnection clientConnection;

    public WriteQuorumCallback(int totalExpectedResponses,
            RequestOrResponse clientRequest,
            ClientConnection clientConnection) {
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
        clientConnection.write(new RequestOrResponse(
                new StringRequest(RequestId.SetValueResponse,
                        response.getBytes()),
                request.getCorrelationId()));
    }

}

class RequestOrResponse {

}

class clientRequest {

}

class clientConnection {

}