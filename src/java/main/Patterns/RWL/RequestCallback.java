package Patterns.RWL;

public interface RequestCallback<T>{
    void onResponse(T r);
    void onError(Throwable e);
}