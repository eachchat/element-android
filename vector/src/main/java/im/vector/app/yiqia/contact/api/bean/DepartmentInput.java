package im.vector.app.yiqia.contact.api.bean;

import java.io.Serializable;

/**
 * Created by zhouguanjie on 2019/9/10.
 */
public class DepartmentInput implements Serializable {

    private int sortOrder;

    private int sequenceId;

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }
}
