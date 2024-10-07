package Patterns.RWL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class RequestWaitingList<TKey, TResponse> {
    
    private Map<TKey, CallbackDetails<TResponse>> pendingRequests = new ConcurrentHashMap<>();

    public void add(TKey key, RequestCallback<TResponse> callback) {

        pendingRequests.put(key, new CallbackDetails<>(callback, System.nanoTime()));
    }

    public void handleResponse(TKey key, TResponse response) {

        if (!pendingRequests.containsKey(key)) {
            return;
        }

        CallbackDetails<TResponse> callbackDetails = pendingRequests.remove(key);

            callbackDetails.getRequestCallback().onResponse(response);

    }

    public void handleError(TKey key, Throwable e) {
        
        CallbackDetails<TResponse> callbackDetails = pendingRequests.remove(key);

        callbackDetails.getRequestCallback().onError(e);
    }
 

}


