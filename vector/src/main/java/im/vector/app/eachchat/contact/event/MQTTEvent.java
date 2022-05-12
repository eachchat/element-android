package im.vector.app.eachchat.contact.event;

/**
 * Created by zhouguanjie on 2019/10/28.
 */
public class MQTTEvent {

    public String cmd, updateTime;

    public long noticeId;

    public MQTTEvent(String cmd, String updateTime, long noticeId) {
        this.cmd = cmd;
        this.updateTime = updateTime;
        this.noticeId = noticeId;
    }
}
