package im.vector.app.eachchat.contact.event;

/**
 * Created by zhouguanjie on 2019/11/12.
 */
public class MQTTConnectEvent {

    public final static int CONNECT_SUCCESS = 0;
    public final static int CONNECT_LOST = 1;
    public final static int CONNECT_RETRY = 2;
    public final static int CONNECT_FETCH = 3;

    private int connectStatus;
    private boolean needFetchData;
    private boolean forceShowUI;

    public MQTTConnectEvent(int connectStatus, boolean needFetchData) {
        this.connectStatus = connectStatus;
        this.needFetchData = needFetchData;
    }

    public MQTTConnectEvent(int connectStatus, boolean forceShowUI, boolean needFetchData) {
        this.connectStatus = connectStatus;
        this.forceShowUI = forceShowUI;
        this.needFetchData = needFetchData;
    }


    public MQTTConnectEvent(int connectStatus) {
        this.connectStatus = connectStatus;
    }

    public int getConnectStatus() {
        return connectStatus;
    }

    public boolean isNeedFetchData() {
        return needFetchData;
    }

    public boolean isForceShowUI() {
        return forceShowUI;
    }
}
