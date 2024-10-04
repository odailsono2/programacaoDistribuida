
public class CallbackDetails<Response> {
    private final RequestCallback<Response> requestCallback;
    private final long createTime;

    public CallbackDetails(RequestCallback<Response> requestCallback, long createTime) {
        this.requestCallback = requestCallback;
        this.createTime = createTime;
    }

    public RequestCallback<Response> getRequestCallback() {
        return requestCallback;
    }

    public long elapsedTime(long now) {
        return now - createTime;
    }

}