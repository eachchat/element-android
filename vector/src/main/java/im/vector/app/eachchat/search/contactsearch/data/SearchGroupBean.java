package im.vector.app.eachchat.search.contactsearch.data;

import android.content.Intent;

import java.io.Serializable;

import im.vector.app.eachchat.department.data.IDisplayBean;

/**
 * Created by zhouguanjie on 2020/2/18.
 */
public class SearchGroupBean implements Serializable, IDisplayBean {

    private String groupAvatar;

    private String groupName;

    private String groupId;

    public SearchGroupBean(String groupAvatar, String groupName, String groupId) {
        this.groupAvatar = groupAvatar;
        this.groupName = groupName;
        this.groupId = groupId;
    }

    @Override
    public String getAvatar() {
        return groupAvatar;
    }

    @Override
    public String getMainContent() {
        return groupName;
    }

    @Override
    public String getMinorContent() {
        return null;
    }

    @Override
    public String getId() {
        return groupId;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getType() {
        return AppConstant.SEARCH_GROUP;
    }

    @Override
    public Intent getExtra() {
        return null;
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
