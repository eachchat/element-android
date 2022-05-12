package im.vector.app.eachchat.search.contactsearch.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;


import java.util.List;

import im.vector.app.R;
import im.vector.app.eachchat.contact.api.BaseConstant;


/**
 * 启动搜索传参
 * Created by zhouguanjie on 2019/9/24.
 */
public class SearchParam implements Parcelable {

    //搜索类型
    //搜索消息 SEARCH_MESSAGE_TYPE = 0 入口聊天TAB搜索
    //搜索联系人 SEARCH_CONTACTS_TYPE = 1 入口联系人TAB搜索
    //搜索某群组相关聊天记录 SEARCH_GROUP_MESSAGE_TYPE = 2 入口聊天搜索TAB搜出多条消息显示聊天记录
    //搜索某群组参与人 SEARCH_GROUP_CONTACTS_TYPE = 3 入口群组内添加 移除人员搜索
    private int mSearchType = AppConstant.SEARCH_MESSAGE_TYPE;
    //群组名
    private String groupName;
    //条数
    private int count;
    //群组id
    private String groupId;
    //搜索关键词
    private String keyWord;
    //是否支持选中
    private boolean isChooseMode;
    //部门id
    private String departmentId;

    private int[] multiType;

    private boolean singleChooseMode;

    private List<String> groupIds;

    private List<String> userIds;

    private long startTime;

    private int teamId;

    private int conversationId;

    //点击仅返回结果不做任何跳转处理
    private boolean onlyReturnData;

    private String channelId;

    private boolean showDelIcon;

    private SearchParam() {

    }

    protected SearchParam(Parcel in) {
        mSearchType = in.readInt();
        groupName = in.readString();
        count = in.readInt();
        groupId = in.readString();
        keyWord = in.readString();
        isChooseMode = in.readByte() != 0;
        departmentId = in.readString();
        multiType = in.createIntArray();
        singleChooseMode = in.readByte() != 0;
        onlyReturnData = in.readByte() != 0;
        if (groupIds != null) {
            in.readStringList(groupIds);
        }
        if (userIds != null) {
            in.readStringList(userIds);
        }
        startTime = in.readLong();
        teamId = in.readInt();
        conversationId = in.readInt();
        channelId = in.readString();
        showDelIcon = in.readByte() != 0;
    }


    public static final Creator<SearchParam> CREATOR = new Creator<SearchParam>() {
        @Override
        public SearchParam createFromParcel(Parcel in) {
            return new SearchParam(in);
        }

        @Override
        public SearchParam[] newArray(int size) {
            return new SearchParam[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSearchType);
        dest.writeString(groupName);
        dest.writeInt(count);
        dest.writeString(groupId);
        dest.writeString(keyWord);
        dest.writeByte((byte) (isChooseMode ? 1 : 0));
        dest.writeString(departmentId);
        dest.writeIntArray(multiType);
        dest.writeInt((byte) (singleChooseMode ? 1 : 0));
        dest.writeInt((byte) (onlyReturnData ? 1 : 0));
        if (groupIds != null) {
            dest.writeList(groupIds);
        }
        if (userIds != null) {
            dest.writeList(userIds);
        }
        dest.writeLong(startTime);
        dest.writeInt(teamId);
        dest.writeInt(conversationId);
        dest.writeString(channelId);
        dest.writeInt((byte) (showDelIcon ? 1 : 0));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        //搜索消息
        public SearchParam buildSearchMessage(int count) {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_MESSAGE_TYPE;
            param.count = count;
            return param;
        }

        public SearchParam buildSearchMessage(int count, String groupId, String keyWord) {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_GROUP_MESSAGE_TYPE;
            param.groupId = groupId;
            param.count = count;
            param.keyWord = keyWord;
            return param;
        }

        //搜索群组中的消息
        public SearchParam buildSearchMessageInGroup(@NonNull String groupId, String groupName, String keyWord, int count) {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_GROUP_MESSAGE_TYPE;
            param.groupName = groupName;
            param.groupId = groupId;
            param.keyWord = keyWord;
            param.count = count;
            return param;
        }

        public SearchParam buildSearchTeamMessage(int teamId, int count) {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_TEAM_CHAT_MESSAGE;
            param.teamId = teamId;
            param.count = count;
            return param;
        }

        //搜索联系人
        public SearchParam buildSearchContracts(int count) {
            SearchParam param = new SearchParam();
            param.mSearchType = BaseConstant.SEARCH_USER_TYPE;
            param.count = count;
            return param;
        }

        public SearchParam buildSearchContracts(int count, boolean isChooseMode) {
            SearchParam param = new SearchParam();
            param.mSearchType = BaseConstant.SEARCH_USER_TYPE;
            param.count = count;
            param.isChooseMode = isChooseMode;
            return param;
        }

        public SearchParam buildSearchNotDelContracts(int count, boolean isChooseMode) {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_CONTACTS_NOT_DEL_TYPE;
            param.count = count;
            param.isChooseMode = isChooseMode;
            return param;
        }

        //搜索群组中的人
        public SearchParam buildSearchGroupMember(@NonNull String groupId, int count, boolean isChooseMode) {
            return buildSearchGroupMember(groupId, count, isChooseMode, false, false);
        }

        public SearchParam buildSearchGroupMember(@NonNull String groupId, int count, boolean isChooseMode, boolean singleChooseMode, boolean showDelIcon) {
            SearchParam param = new SearchParam();
            param.groupId = groupId;
            param.mSearchType = AppConstant.SEARCH_GROUP_CONTACTS_TYPE;
            param.count = count;
            param.isChooseMode = isChooseMode;
            param.singleChooseMode = singleChooseMode;
            param.showDelIcon = showDelIcon;
            return param;
        }

        //搜索部门中的人
        public SearchParam buildSearchDepartmentMember(@NonNull String departmentId, int count, boolean isChooseMode) {
            SearchParam param = new SearchParam();
            param.departmentId = departmentId;
            param.count = count;
            param.isChooseMode = isChooseMode;
            param.mSearchType = AppConstant.SEARCH_DEPARTMENT_CONTACTS_TYPE;
            return param;
        }

        //搜索团队中的人
        public SearchParam buildSearchTeamMember(int teamId, boolean isChooseMode) {
            SearchParam param = new SearchParam();
            param.teamId = teamId;
            param.isChooseMode = isChooseMode;
            param.mSearchType = AppConstant.SEARCH_TEAM_MEMBER_TYPE;
            return param;
        }

        public SearchParam buildSearchChannelMember(String channelId, boolean isChooseMode) {
            SearchParam param = new SearchParam();
            param.channelId = channelId;
            param.isChooseMode = isChooseMode;
            param.mSearchType = AppConstant.SEARCH_CHANNEL_MEMBER_TYPE;
            return param;
        }

        //搜索讨论里面的人
        public SearchParam buildSearchConversationMember(int teamId, int conversationId, boolean isChooseMode) {
            SearchParam param = new SearchParam();
            param.teamId = teamId;
            param.conversationId = conversationId;
            param.isChooseMode = isChooseMode;
            param.mSearchType = AppConstant.SEARCH_TOPIC_MENTION_MEMBER_TYPE;
            return param;
        }

        /**
         * 综合搜索
         *
         * @param count     条数
         * @param multiType 搜索哪些内容 不可包含 AppConstant.SEARCH_MULTI_TYPE
         * @return 搜索参数
         */
        public SearchParam buildSearchMulti(int count, int[] multiType) {
            SearchParam param = new SearchParam();
            param.count = count;
            param.mSearchType = AppConstant.SEARCH_MULTI_TYPE;
            param.multiType = multiType;
            return param;
        }

        public SearchParam buildSearchMulti(int count, int[] multiType, boolean onlyReturnData) {
            SearchParam param = new SearchParam();
            param.count = count;
            param.mSearchType = AppConstant.SEARCH_MULTI_TYPE;
            param.multiType = multiType;
            param.onlyReturnData = onlyReturnData;
            return param;
        }

        public SearchParam buildSearchMulti(int count, int[] multiType, String departmentId) {
            SearchParam param = new SearchParam();
            param.count = count;
            param.mSearchType = AppConstant.SEARCH_MULTI_TYPE;
            param.multiType = multiType;
            param.departmentId = departmentId;
            return param;
        }

        public SearchParam buildNewOne(SearchParam param, int type) {
            SearchParam p = new SearchParam();
            p.mSearchType = type;
            p.groupName = param.getGroupName();
            p.count = param.getCount();
            p.groupId = param.getGroupId();
            p.keyWord = param.getKeyWord();
            p.isChooseMode = param.isChooseMode;
            p.departmentId = param.getDepartmentId();
            return p;
        }

        public SearchParam buildSearchDepartments(int count) {
            SearchParam param = new SearchParam();
            param.mSearchType = BaseConstant.SEARCH_DEPARTMENT_TYPE;
            param.count = count;
            return param;
        }

        public SearchParam getParamByType(Context context, String type, String groupId, String keyWord) {
                if (TextUtils.equals(type, context.getString(R.string.contacts))) {
                return buildServiceSearchContacts();
            } else if (TextUtils.equals(type, context.getString(R.string.message_record))) {
                return buildServiceSearchGroup();
            } else if (TextUtils.equals(type, context.getString(R.string.all))) {
                return buildServiceSearchAll();
            } else if (TextUtils.equals(type, context.getString(R.string.file))) {
                return buildServiceSearchFiles();
            } else if (TextUtils.equals(type, context.getString(R.string.message))) {
                return buildServiceSearchMessage();
            }
            return null;
        }

        public SearchParam buildServiceSearchAll() {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_SERVICE_ALL;
            return param;
        }

        public SearchParam buildServiceSearchContacts() {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_SERVICE_CONTACTS;
            return param;
        }

        public SearchParam buildServiceSearchFiles() {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_SERVICE_FILE;
            return param;
        }

        public SearchParam buildServiceSearchGroup() {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_SERVICE_GROUP;
            return param;
        }

        public SearchParam buildServiceSearchMessage() {
            SearchParam param = new SearchParam();
            param.mSearchType = AppConstant.SEARCH_SERVICE_MESSAGE;
            return param;
        }

    }

    public int[] getMultiType() {
        return multiType;
    }

    public void setMultiType(int[] multiType) {
        this.multiType = multiType;
    }

    public int getSearchType() {
        return mSearchType;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public boolean isChooseMode() {
        return isChooseMode;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isSingleChooseMode() {
        return singleChooseMode;
    }

    public void setSingleChooseMode(boolean singleChooseMode) {
        this.singleChooseMode = singleChooseMode;
    }

    public boolean isOnlyReturnData() {
        return onlyReturnData;
    }

    public void setOnlyReturnData(boolean onlyReturnData) {
        this.onlyReturnData = onlyReturnData;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public String getChannelId() {
        return channelId;
    }

    public boolean isShowDelIcon() {
        return showDelIcon;
    }
}
