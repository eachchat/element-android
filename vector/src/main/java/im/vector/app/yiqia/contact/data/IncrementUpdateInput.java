package im.vector.app.yiqia.contact.data;

import java.io.Serializable;

/**
 * Created by zhouguanjie on 2019/10/31.
 */
public class IncrementUpdateInput implements Serializable {

    private String name;

    private long updateTime;

    private int sequenceId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }
}
