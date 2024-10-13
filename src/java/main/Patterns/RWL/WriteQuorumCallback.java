package Patterns.RWL;

import java.io.IOException;

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
            respondToClient(new String(r.getRequest().getData()));
            done = true;
        }
    }

    @Override
    public void onError(Throwable e) {
        receivedErrors++;
        if (receivedErrors == quorum && !done) {
            respondToClient(e.getMessage());
            done = true;
        }
    }

    private void respondToClient(String response) {
        // System.out.println(response);
        StringRequest request= new StringRequest(RequestId.SetValueRequest.getValor(),response.getBytes());
        RequestOrResponse reqResp = new RequestOrResponse(request,request.getCorrelationId());
        try {
            clientConnection.respond(reqResp.getRequest().getData());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    }

}

enum RequestId {
    SetValueRequest(1),
    SetValueResponse(2),
    GetValueRequest(3),
    GetValueResponse(4);

    private final int valor;

    RequestId(int valor){
        this.valor = valor;

    }

    public int getValor(){
        return valor;
    }

        // Método para obter o enum pelo valor inteiro
        public static RequestId fromValor(int valor) {
            for (RequestId req : RequestId.values()) {
                if (req.getValor() == valor) {
                    return req;
                }
            }
            throw new IllegalArgumentException("Valor inválido: " + valor);
        }
}

