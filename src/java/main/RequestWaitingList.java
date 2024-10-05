
import java.util.concurrent.*;
import java.util.*;

public class RequestWaitingList<TKey, TResponse> {
    
    private Map<TKey, CallbackDetails<TResponse>> pendingRequests = new ConcurrentHashMap<>();

    public void add(TKey key, RequestCallback<TResponse> callback) {

        pendingRequests.put(key, new CallbackDetails<>(callback, System.nanoTime()));
    }

    public void handleResponse(TKey key, TResponse response) throws Exception {

        if (!pendingRequests.containsKey(key)) {
            return;
        }

        CallbackDetails<TResponse> callbackDetails = pendingRequests.remove(key);

        if (callbackDetails != null) {
            callbackDetails.getRequestCallback().onResponse(response);
        } else {
            throw new Exception("Chave " + key + " não encontrada na lista de requisições pendentes.");
        }

    }

    public void handleError(TKey key, Throwable e) throws Exception {
        
        CallbackDetails<TResponse> callbackDetails = pendingRequests.remove(key);

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

