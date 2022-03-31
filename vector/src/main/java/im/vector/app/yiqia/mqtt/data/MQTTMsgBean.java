package im.vector.app.yiqia.mqtt.data;

import java.io.Serializable;

/**
 * Created by zhouguanjie on 2019/9/3.
 */
public class MQTTMsgBean implements Serializable {

    private String clientId;

    private long noticeId;

    private String name;

    private Object value;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(long noticeId) {
        this.noticeId = noticeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
