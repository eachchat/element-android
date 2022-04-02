package im.vector.app.eachchat.mqtt;

import android.content.Context;
import android.text.TextUtils;

import org.matrix.android.sdk.api.session.Session;

import im.vector.app.eachchat.base.BaseModule;
import im.vector.app.eachchat.utils.SPUtils;
import im.vector.app.eachchat.utils.AppCache;
import im.vector.app.eachchat.contact.data.User;
import im.vector.app.eachchat.contact.data.UserEnterpriseBean;

/**
 * Created by zhouguanjie on 2019/8/27.
 */
public class UserCache {

    private static final String KEY_USER = "key_user";
    private static final String KEY_INIT_CONTACTS = "key_init_contacts";
    private static final String KEY_INIT_GROUPS = "key_init_groups";
    private static final String KEY_INIT_ENCRYPTION_GROUPS = "key_init_encryption_groups";
    private static final String KEY_INIT_DEPARTMENTS = "key_init_departments";
    private static final String KEY_SEQ_ID = "key_seq_id";
    private static final String KEY_VOICE_MODE = "key_voice_tel_mode";
    private static final String KEY_VOICE_TIME_LIMIT = "key_voice_time_limit";
    private static final String KEY_RECENT = "key_recent";
    private static final String KEY_UPDATE_ENTERPRISE_TIME = "key_update_enterprise_time";
    private static final String KEY_LOST_CONNECTION_RED_TIPS = "key_lost_connection_red_tips";
    private static final String KEY_UPDATE_TEAM_TIME = "key_update_team_time";
    private static final String KEY_UPDATE_CONVERSATION_TIME = "key_update_conversation_time";
    private static final String KEY_UPDATE_TOPIC_TIME = "key_update_topic_time";
    private static final String KEY_UPDATE_TOPIC_COUNT_TIME = "key_update_topic_count_time";
    private static final String KEY_UPDATE_TOPIC_REPLY_TIME = "key_update_topic_reply_time";
    private static final String KEY_UPDATE_TOPIC_FILES = "key_update_topic_files";
    private static final String KEY_UPDATE_TEAM_NOTIFY = "key_update_team_notify";
    private static final String KEY_UPDATE_TOPIC_FOLDER = "key_update_topic_folder";
    private static final String KEY_TEAM_MESSAGE_SEQID = "key_team_message_seqid";
    private static final String KEY_CHANNEL_UPDATE_TIME = "key_channel_update_time";
    private static final String KEY_CHANNEL_UPDATE_MESSAGE_TIME = "key_channel_update_message_time";
    private static String currentGroupId;
    private static User user = new User();


    /**
     * 保存当前用户信息
     *
     * @param user 用户信息
     */
//    public static void saveMe(User user) {
//        UserStoreHelper.insertOrUpdate(user);
//        SPUtils.put(KEY_USER, user);
//    }

     public static void saveMeSp(User user) {
        SPUtils.put(KEY_USER, user);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户
     */
    public static User getUser() {
        return SPUtils.get(KEY_USER, UserCache.user);
    }

    public static String getUserId() {
        if (TextUtils.isEmpty(user.getId())) {
            user.setId(getUser().getId());
        }
        return user.getId();
    }

    public static void clear() {
        SPUtils.remove(KEY_USER);
        user.setId(null);
    }

    public static void clearAll(Context context) {
        SPUtils.clear(context);
    }

    /**
     * 是否初始化联系人
     *
     * @return 是/否
     */
    public static boolean isInitContacts() {
        return SPUtils.get(getUserId() + KEY_INIT_CONTACTS, false);
    }

    /**
     * 设置是否成功初始化联系人
     *
     * @param hasInit 是/否
     */
    public static void setInitContacts(boolean hasInit) {
        SPUtils.put(getUserId() + KEY_INIT_CONTACTS, hasInit);
    }

    /**
     * 设置是否成功初始化群组
     *
     * @param hasInit 是/否
     */
    public static void setInitGroups(boolean hasInit) {
        SPUtils.put(getUserId() + KEY_INIT_GROUPS, hasInit);
    }

    /**
     * 是否初始化群组
     *
     * @return 是/否
     */
    public static boolean isInitGroups() {
        return SPUtils.get(getUserId() + KEY_INIT_GROUPS, false);
    }

    public static void setInitEncryptionGroups(boolean hasInit) {
        SPUtils.put(getUserId() + KEY_INIT_ENCRYPTION_GROUPS, hasInit);
    }

    public static boolean isInitEncryptionGroups() {
        return SPUtils.get(getUserId() + KEY_INIT_ENCRYPTION_GROUPS, false);
    }

    public static boolean isInitDepartments() {
        return SPUtils.get(getUserId() + KEY_INIT_DEPARTMENTS, false);
    }

    public static void setInitDepartments(boolean hasInit) {
        SPUtils.put(getUserId() + KEY_INIT_DEPARTMENTS, hasInit);
    }

    /**
     * 注销操作 清空用户数据
     */
//    public static void logout() {
//        AppCache.saveImportRecoveryKey(false);
//        PushUtils.clearNotification();
//        BadgeUtils.setCount(0, YQLApplication.getContext());
//        PushUtils.logout();
////        MessageStoreHelper.clearAll();
////        setInitGroups(false);
//        TokenStore.removeToken();
//        UserCache.clear();
//        NotificationUtils.getInstance().clearAllNotifications();
//        IMManager.getClient().disconnect();
//        StoreManager.reset();
//
//        // Clear all sp config
//        SPUtils.clear(YQLApplication.getContext());
//        NetConstant.clearHost();
//        Session session = MatrixHolder.getInstance(YQLApplication.getContext()).getSession();
//        if (session != null) {
//            session.close();
//        }
//    }

    public static boolean isLogin() {
        Session session = BaseModule.getSession();
        return session != null && session.isOpenable();
    }

    public static void saveSequenceId(long seqId, boolean isEncryption) {
        if (getUser() == null || TextUtils.isEmpty(getUserId())) {
            return;
        }
        SPUtils.put(getKeySeqId(isEncryption), seqId);
    }

    public static long getSeqId(boolean isEncryption) {
        if (getUser() == null || TextUtils.isEmpty(getUserId())) {
            return 0;
        }
        return SPUtils.get(getKeySeqId(isEncryption), 0L);
    }

    private static String getKeySeqId(boolean isEncryption) {
        String key;
        if (isEncryption) {
            key = getUserId() + KEY_SEQ_ID + "_encryption";
        } else {
            key = getUserId() + KEY_SEQ_ID;
        }
        return key;
    }

    public static void setCurrentGroupId(String groupId) {
        currentGroupId = groupId;
    }

    public static String getCurrentGroupId() {
        return currentGroupId;
    }

    public static String getUpdateGroupTime(boolean isEncryption) {
        return SPUtils.get(getUserId() + (isEncryption ? "update_encryption_group_time" : "update_group_time"), "0");
    }

    public static void setUpdateGroupTime(String time, boolean isEncryption) {
        SPUtils.put(getUserId() + (isEncryption ? "update_encryption_group_time" : "update_group_time"), time);
    }

    public static String getCacheUpdateGroupTime(boolean isEncryption) {
        return SPUtils.get(getUserId() + (isEncryption ? "cache_update_encryption_group_time" : "cache_update_group_time"), "0");
    }

    public static void setCacheUpdateGroupTime(String time, boolean isEncryption) {
        SPUtils.put(getUserId() + (isEncryption ? "cache_update_encryption_group_time" : "cache_update_group_time"), time);
    }

    public static String getUpdateUserTime() {
        return SPUtils.get(getUserId() + "update_user_time", "0");
    }

    public static void setUpdateUserTime(String time) {
        SPUtils.put(getUserId() + "update_user_time", time);
    }

    public static String getUpdateDepartmentTime() {
        return SPUtils.get(getUserId() + "update_department_time", "0");
    }

    public static void setUpdateDepartmentTime(String time) {
        SPUtils.put(getUserId() + "update_department_time", time);
    }

    public static void setAppCustom(String custom) {
        SPUtils.put(getUserId() + "app_custom", custom);
    }

    public static String getAppCustom() {
        return SPUtils.get(getUserId() + "app_custom", "");
    }

    public static void setSpeakerMode(boolean b) {
        SPUtils.put(getUserId() + KEY_VOICE_MODE, b);
    }

    public static boolean getSpeakerMode() {
        return SPUtils.get(getUserId() + KEY_VOICE_MODE, false);
    }

    public static void setVoiceLengthLimit(long lengthLimit) {
        SPUtils.put(getUserId() + KEY_VOICE_TIME_LIMIT, lengthLimit);
    }

    public static long getVoiceLengthLimit() {
        return SPUtils.get(getUserId() + KEY_VOICE_TIME_LIMIT, 600);
    }

    public static void setRecentLimit(int rencentLimit) {
        SPUtils.put(getUserId() + KEY_RECENT, rencentLimit);
    }

    public static int getRecentLimit() {
        return SPUtils.get(getUserId() + KEY_RECENT, 30);
    }

    public static void setEnterpriseUpdateTime(long time) {
        SPUtils.put(getUserId() + KEY_UPDATE_ENTERPRISE_TIME, time);
    }

    public static long getEnterpriseUpdateTime() {
        return SPUtils.get(getUserId() + KEY_RECENT, 0);
    }

    public static void setShowLostTipsTime(long time) {
        SPUtils.put(getUserId() + KEY_LOST_CONNECTION_RED_TIPS, time);
    }

    public static long getShowLostTipsTime() {
        return SPUtils.get(getUserId() + KEY_LOST_CONNECTION_RED_TIPS, 0);
    }

    public static void saveServiceParam(UserEnterpriseBean bean) {
        if (bean == null) {
            return;
        }
        UserCache.setEnterpriseUpdateTime(System.currentTimeMillis());
        if (bean.getVoiceMaxDuration() >= 0) {
            UserCache.setVoiceLengthLimit(bean.getVoiceMaxDuration());
            AppCache.setVoiceLengthLimit(bean.getVoiceMaxDuration());
        }
        if (bean.getRecentContactsNumber() >= 0) {
            UserCache.setRecentLimit(bean.getRecentContactsNumber());
        }
        UserCache.setShowLostTipsTime(bean.getLostConnDuration());
    }

    public static void saveUpdateTeamTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_TEAM_TIME, updateTime);
    }

    public static long getUpdateTeamTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_TEAM_TIME, 0L);
    }

    public static void saveUpdateConversationTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_CONVERSATION_TIME, updateTime);
    }

    public static long getUpdateConversationTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_CONVERSATION_TIME, 0L);
    }

    public static void saveUpdateTopicTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_TOPIC_TIME, updateTime);
    }

    public static long getUpdateTopicTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_TOPIC_TIME, -1L);
    }

    public static void saveUpdateTopicCountTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_TOPIC_COUNT_TIME, updateTime);
    }

    public static long getUpdateTopicCountTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_TOPIC_COUNT_TIME, -1L);
    }

    public static void saveUpdateTopicReplyTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_TOPIC_REPLY_TIME, updateTime);
    }

    public static long getUpdateTopicReplyTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_TOPIC_REPLY_TIME, -1L);
    }

    public static void saveUpdateTopicFileTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_TOPIC_FILES, updateTime);
    }

    public static long getUpdateTopicFileTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_TOPIC_FILES, -1L);
    }

    public static void saveUpdateTopicFolderTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_TOPIC_FOLDER, updateTime);
    }

    public static long getUpdateTopicFolderTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_TOPIC_FOLDER, -1L);
    }

    public static void saveUpdateTeamNotifyTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_UPDATE_TEAM_NOTIFY, updateTime);
    }

    public static long getUpdateTeamNotifyTime() {
        return SPUtils.get(getUserId() + KEY_UPDATE_TEAM_NOTIFY, -1L);
    }

    public static void saveUpdateTeamMessageSeqId(long updateTime) {
        SPUtils.put(getUserId() + KEY_TEAM_MESSAGE_SEQID, updateTime);
    }

    public static long getUpdateTeamMessageSeqId() {
        return SPUtils.get(getUserId() + KEY_TEAM_MESSAGE_SEQID, -1L);
    }

    public static void saveUpdateChannelTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_CHANNEL_UPDATE_TIME, updateTime);
    }

    public static long getUpdateChannelTime() {
        return SPUtils.get(getUserId() + KEY_CHANNEL_UPDATE_TIME, 0L);
    }

    public static void saveUpdateChannelMessageTime(long updateTime) {
        SPUtils.put(getUserId() + KEY_CHANNEL_UPDATE_MESSAGE_TIME, updateTime);
    }

    public static long getUpdateChannelMessageTime() {
        return SPUtils.get(getUserId() + KEY_CHANNEL_UPDATE_MESSAGE_TIME, -1L);
    }

}
