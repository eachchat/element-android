package im.vector.app.eachchat.search.contactsearch.data;

import im.vector.app.BuildConfig;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class AppConstant {

    public final static String SUCCESS = "success";

    public final static String EMAIL_FORMAT = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";

    public final static int SEARCH_MESSAGE_TYPE = 0;
    public final static int SEARCH_GROUP_MESSAGE_TYPE = 2;
    public final static int SEARCH_GROUP_CONTACTS_TYPE = 3;
    public final static int SEARCH_DEPARTMENT_CONTACTS_TYPE = 4;

    public final static int SEARCH_CONTACTS_NOT_DEL_TYPE = 6;
    public final static int SEARCH_GROUP = 7;
    public final static int SEARCH_FILE = 8;
    public final static int SEARCH_TEAM_MEMBER_TYPE = 9;
    public final static int SEARCH_TOPIC_MENTION_MEMBER_TYPE = 10;
    public final static int SEARCH_CHANNEL_MEMBER_TYPE = 11;


    public final static int SEARCH_MULTI_TYPE = 100;

    public final static int SEARCH_SERVICE_ALL = 200;
    public final static int SEARCH_SERVICE_CONTACTS = 201;
    public final static int SEARCH_SERVICE_FILE = 202;
    public final static int SEARCH_SERVICE_GROUP = 203;
    public final static int SEARCH_SERVICE_MESSAGE = 204;
    public final static int SEARCH_TEAM_CHAT_MESSAGE = 205;

    public final static int SEARCH_PRE_COUNT = 30;

    public final static int CONTACT_TYPE = 2;

    // Request codes here
    public static final int REQUEST_CODE_TAKE_PHOTO = 10000;
    public static final int REQUEST_CODE_PICK_PHOTO = 10001;
    public static final int REQUEST_CODE_CROP_PHOTO = 10002;
    public static final int REQUEST_CODE_APP_SET = 10004;
    public static final int REQUEST_CODE_ENCRYPTED_CHAT_CHOOSE = 10007;
    public static final int REQUEST_CODE_ENCRYPTED_CHAT_MORE = 10008;

    public static final String FILE_PROVIDER = BuildConfig.APPLICATION_ID;

    public static final int GROUP_STATUS_TOP = 1;

    public static final int GROUP_STATUS_DISTURBFLAG = 2;

    public static final String KEY_DEPARTMENT_ID = "key_department_id";

    public final static int ERROR_FORBID_UPDATE_AVATAR = 505;
    public final static int ERROR_GROUP_FORBID_CHAT = 508;
    public final static int ERROR_MANAGER_CAN_DISSOLVE_GROUP = 509;
    public final static int ERROR_FORBID_CHANGE_GROUP_NAME = 510;
    public final static int ERROR_FORBID_PUBLISH_ANNOUNCEMENT = 511;
    public final static int ERROR_GROUP_MEMBER_LIMIT = 512;
    public final static int ERROR_COLLECTION_SEND_FAIL_MESSAGE = 513;

}
