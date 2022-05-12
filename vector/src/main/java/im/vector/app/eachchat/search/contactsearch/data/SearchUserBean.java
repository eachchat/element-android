package im.vector.app.eachchat.search.contactsearch.data;

import android.content.Intent;

import androidx.annotation.Nullable;

import im.vector.app.eachchat.contact.api.BaseConstant;
import im.vector.app.eachchat.contact.data.User;
import im.vector.app.eachchat.department.data.DepartmentUserBean;
import im.vector.app.eachchat.department.data.IDisplayBean;

/**
 * Created by zhouguanjie on 2019/9/11.
 */
public class SearchUserBean extends DepartmentUserBean implements IDisplayBean {

    private String departmentName;

    private String keyWord;

    private Intent intent;

    private String contactUrlAvatar;
    private String contactBase64Avatar;

    public SearchUserBean(User user, String keyWord) {
        super(user);
        setId(user.getId());
        setDisplayName(user.getDisplayName());
        setNickName(user.getNickName());
        setUserName(user.getUserName());
        setDepartmentId(user.getDepartmentId());
        setEmails(user.getEmails());
        setPhoneNumbers(user.getPhoneNumbers());
        setAvatarOUrl(user.getAvatarOUrl());
        setAvatarTUrl(user.getAvatarTUrl());
        setMatrixId(user.getMatrixId());
        this.keyWord = keyWord;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getContactUrlAvatar() {
        return contactUrlAvatar;
    }

    public void setContactUrlAvatar(String contactUrlAvatar) {
        this.contactUrlAvatar = contactUrlAvatar;
    }

    public String getContactBase64Avatar() {
        return contactBase64Avatar;
    }

    public void setContactBase64Avatar(String contactBase64Avatar) {
        this.contactBase64Avatar = contactBase64Avatar;
    }

    @Override
    public String getMainContent() {
//        String content = "";
//        if (!TextUtils.isEmpty(getDisplayName())) {
//            if (getDisplayName().contains(keyWord)) {
//                return getDisplayName();
//            }
//        }
//        content = getDisplayName();
//        if (!TextUtils.isEmpty(getNickName())) {
//            if (getNickName().contains(keyWord)) {
//                return content + "(" + getNickName() + ")";
//            }
//        }
//
//        if (!TextUtils.isEmpty(getUserName())) {
//            if (getUserName().contains(keyWord)) {
//                return content + "(" + getUserName() + ")";
//            }
//        }
        return getDisplayName();
    }

    @Override
    public String getMinorContent() {
//        if (getEmails() != null) {
//            for (Email email : getEmails()) {
//                if (email.getValue().contains(keyWord)) {
//                    return email.getValue();
//                }
//            }
//        }

//        if (getPhoneNumbers() != null) {
//            for (Phone phone : getPhoneNumbers()) {
//                if (phone.getValue().contains(keyWord)) {
//                    return phone.getValue();
//                }
//            }
//        }

        return getUserTitle();
    }

    @Override
    public String getAvatar() {
        return getAvatarTUrl();
    }

    @Override
    public int getItemType() {
        return BaseConstant.USER_TYPE;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getType() {
        return BaseConstant.SEARCH_USER_TYPE;
    }

    @Override
    public Intent getExtra() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DepartmentUserBean) {
            return super.equals(obj);
        }
        return false;
//        if (!(obj instanceof SearchUserBean)) {
//            return false;
//        }
//        SearchUserBean bean = (SearchUserBean) obj;
//        return TextUtils.equals(bean.getId(), getId());
    }
}
