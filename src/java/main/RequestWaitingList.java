
import java.util.concurrent.*;
import java.util.*;

public class RequestWaitingList<Key, Response> {
    private Map<Key, CallbackDetails> pendingRequests = new ConcurrentHashMap<>();

    public void add(Key key, RequestCallback<Response> callback) {

        pendingRequests.put(key, new CallbackDetails(callback, System.nanoTime()));
    }

    public void handleResponse(Key key, Response response) {

        if (!pendingRequests.containsKey(key)) {
            return;
        }

        CallbackDetails callbackDetails = pendingRequests.remove(key);

        ((RequestCallback) callbackDetails.getRequestCallback()).onResponse(response);

    }

    public void handleError(int requestId, Throwable e) {

        CallbackDetails callbackDetails = pendingRequests.remove(requestId);
        ((RequestCallback<Response>) callbackDetails.getRequestCallback()).onError(e);

    }

}

class CallbackDetails<RequestCallback> {
    RequestCallback requestCallback;
    long createTime;

    public CallbackDetails(RequestCallback requestCallback, long createTime) {
        this.requestCallback = requestCallback;
        this.createTime = createTime;
    }

    public RequestCallback getRequestCallback() {
        return requestCallback;
    }

    public long elapsedTime(long now) {
        return now - createTime;
    }

}

class Response {
    String response;

    public Response(String response) {
        this.response = response;
    }
}
