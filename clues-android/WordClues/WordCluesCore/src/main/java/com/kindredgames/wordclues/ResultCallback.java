package com.kindredgames.wordclues;

/**
 * Use to handle completion of a request with a result
 * @param <T>
 */
public interface ResultCallback<T> {

    void onResult(T result);

    void onFault(String reason);
}
