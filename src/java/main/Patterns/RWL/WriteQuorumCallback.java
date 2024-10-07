package Patterns.RWL;
public class WriteQuorumCallback
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
        //StringRequest requestTest= new StringRequest(1,response.getBytes());
        //RequestOrResponse reqRespTest = new RequestOrResponse(requestTest,1);
        clientConnection.respond(request);   
    }

}

enum RequestId {
    SetValueRequest,
    SetValueResponse,
    GetValueRequest,
    GetValueResponse;
}

