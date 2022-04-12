package im.vector.app.eachchat.search.contactsearch.data;

import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.Serializable;

import im.vector.app.eachchat.department.data.IDisplayBean;

/**
 * Created by zhouguanjie on 2019/9/11.
 */
public class SearchGroupMessageBean implements Serializable, IDisplayBean {

    private String groupId;

    private String groupName;

    private String messageContent;

    private long seqId;

    private int count;

    private String avatar;

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public long getSeqId() {
        return seqId;
    }

    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }

    @Override
    public String getMainContent() {
        if (groupName != null) {
            return groupName;
        }
        return "";
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public String getId() {
        return groupId;
    }

    @Override
    public int getItemType() {
        return 0;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getMinorContent() {
        return messageContent;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getType() {
        return AppConstant.SEARCH_GROUP_MESSAGE_TYPE;
    }

    @Override
    public Intent getExtra() {
        return null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SearchGroupMessageBean)) {
            return false;
        }
        SearchGroupMessageBean bean = (SearchGroupMessageBean) obj;
        return TextUtils.equals(bean.getId(), getId());
    }
}
