package im.vector.app.eachchat.net;

import java.io.Serializable;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class Response<V, T> implements Serializable {

    private int code;

    private String message;

    private V obj;

    private T results;

    private int total;

    private boolean hasNext;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public V getObj() {
        return obj;
    }

    public void setObj(V obj) {
        this.obj = obj;
    }

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isSuccess() {
        return code == NetConstant.NET_SUCCESS || code == NetConstant.NET_NO_NEWS;
    }
}