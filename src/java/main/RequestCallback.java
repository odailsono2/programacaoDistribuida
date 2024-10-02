
public interface RequestCallback<T>{
    void onResponse(T r);
    void onError(Throwable e);
}