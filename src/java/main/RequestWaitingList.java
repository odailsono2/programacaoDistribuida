
import java.util.concurrent.*;
import java.util.*;

public class RequestWaitingList<Key, Response> {
    
    private Map<Key, CallbackDetails<Response>> pendingRequests = new ConcurrentHashMap<>();

    public void add(Key key, RequestCallback<Response> callback) {

        pendingRequests.put(key, new CallbackDetails<>(callback, System.nanoTime()));
    }

    public void handleResponse(Key key, Response response) throws Exception {

        if (!pendingRequests.containsKey(key)) {
            return;
        }

        CallbackDetails<Response> callbackDetails = pendingRequests.remove(key);

        if (callbackDetails != null) {
            callbackDetails.getRequestCallback().onResponse(response);
        } else {
            throw new Exception("Chave " + key + " não encontrada na lista de requisições pendentes.");
        }

    }

    public void handleError(Key key, Throwable e) throws Exception {
        CallbackDetails<Response> callbackDetails = pendingRequests.remove(key);

        // Verifica se existe um callback para essa chave
        if (callbackDetails != null) {
            callbackDetails.getRequestCallback().onError(e);
        } else {
            throw new Exception("Chave " + key + " não encontrada na lista de requisições pendentes.");
        }
    }
 

}

interface RequestCallback<TResponse>{
    void onResponse(TResponse r);
    void onError(Throwable e);
}

