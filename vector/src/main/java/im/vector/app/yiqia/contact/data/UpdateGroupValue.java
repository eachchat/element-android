package im.vector.app.yiqia.contact.data;

import java.io.Serializable;

/**
 * Created by zhouguanjie on 2019/9/6.
 */
public class UpdateGroupValue implements Serializable {

    private long updateTime;

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
