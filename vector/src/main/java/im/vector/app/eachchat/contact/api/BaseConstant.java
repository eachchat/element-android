package im.vector.app.eachchat.contact.api;

import java.util.regex.Pattern;


/**
 * Created by zhouguanjie on 2019/11/21.
 */
public class BaseConstant {

    public final static String HOME_SERVER_URL = "home_server_url";
    public final static String ORG_NAME = "org_name";

    public final static String EACH_CHAT_HOME_SERVER = "https://chat.yunify.com";

    public final static Boolean E2EEnable = false; // 加密功能通过E2EEnable来控制

    //AppId
    public final static int MESSAGE = 0;
    public final static int CONTACT = 1;
    public final static int COLLECTION = 2;
    public final static int EMAIL = 3;
    public static final int ANYBOX = 4;
    public static final int TEAM = 5;
    public static final int NOTIFY = 6;
    public static final int CHANNEL = 7;
    public static final int MINE = 8;

    // Exit all activities
    public static final String EXIT_LISTENER_RECEIVER = ".ExitListenerReceiver";
    public static final String ALL_EXIT = "all_exit";

    // Available in SystemSettingsActivity
    public static final String SP_LANGUAGES = "sp_languages"; // 0 == system language; 1 == zh-CN; 2 == en-US
    public static final String SP_TEXT_SIZE = "sp_text_size";

    public static final char DEF_CHAR = '#';

    public static final int REQUEST_CODE_ADD_TEAM_MEMBER = 20000;

    public static final int TEAM_MEMBER_TYPE = 1;
    public static final int CONVERSATION_MEMBER_TYPE = 2;

    public static int ROOT_FOLDER_ID = 0;
    public static int FOLDER = 0;
    public static int FILE = 1;

    //纯文本主题帖
    public final static int TEXT_TOPIC = 1;
    //通知类型
    public final static int NOTIFY_TOPIC = 2;
    //富文本类型帖子
    public final static int RICH_TOPIC = 3;
    //不支持的
    public final static int UN_SUPPORT_TOPIC = 0;

    public static final String TOPIC_DETAIL_PATH = "com.workly.ai.team.android.conversation.topic.TopicDetailActivity";
    public final static String KEY_TOPIC_ID = "key_topic_id";
    public final static String KEY_TEAM_ID = "key_team_id";
    public final static String KEY_CONVERSATION_ID = "key_conversation_id";

    public final static String CMD_UPDATE_TOPIC = "updateTopic";
    public final static String CMD_UPDATE_TOPIC_COUNT = "updateTopicCount";
    public final static String CMD_UPDATE_TOPIC_REPLY = "updateReplyTopic";
    public final static String CMD_UPDATE_FILE = "updateFile";
    public final static String CMD_UPDATE_NOTIFY = "updateNotification";
    public final static String CMD_UPDATE_FOLDER = "updateFolder";
    public final static String CMD_NEW_TEAM_MESSAGE = "newTeamMessage";
    public final static String CMD_UPDATE_CHANNEL = "updateChannel";
    public final static String CMD_NEW_CHANNEL_MESSAGE = "newChannelMessage";

    public static final String CONVERSATION_HOME_PATH = "com.workly.ai.team.android.conversation.home.ConversationHomeActivity";
    public static final String HOME_ACTIVITY_PATH = "ai.workly.yql.android.home.HomeActivity";
    public static final String TOPIC_MENTION_PATH = "ai.workly.yql.android.chat.mention.MentionTopicActivity";
    public static final String PREIVEW_PATH = "ai.workly.yql.android.preview.start.PreviewStartActivity";
    public static final String TEAM_CHAT_PATH = "ai.workly.yql.android.chat.home.team.TeamChatActivity";
    public static final String USER_INFO_PATH = "ai.workly.yql.android.user.UserInfoActivity";
    public static final String FORWARD_MESSAGE_PATH = "ai.workly.yql.android.chat.forward.ForwardMessageActivity";
    public static final String CHANNEL_PATH = "com.workly.ai.channel.publish.PublishMessageActivity";

    public static final String SP_CREATE_SELECT_TOTAL_COUNT = "sp_create_select_total_count";
    public static final String SP_CREATE_SELECT_OUTSIDE_COUNT = "sp_create_select_outside_count";
    public static final String SP_GROUP_ADD_MEMBER_COUNT = "sp_group_add_member_count";

    public static final int REQUEST_CHOOSE_IMAGE = 881;
    public static final int REQUEST_CHOOSE_FILE = 882;
    public static final int REQUEST_CHOOSE_FOLDER = 883;
    public static final int REQUEST_CODE_TAKE_PHOTO = 10000;
    public static final int REQUEST_CODE_PICK_PHOTO = 10001;
    public static final int REQUEST_CODE_CROP_PHOTO = 10002;

    public static final String KEY_EVENT_ID = "key_event_id";
    public static final String KEY_ROOM_ID = "key_room_id";
    public static final String KEY_CALL_ID = "key_call_id";
    public static final String KEY_TAGERT_EVENT_ID = "key_tagert_event_id";
    public static final String KEY_TAGERT_KEYWORD = "key_tagert_keyword";
    public static final String KEY_MATRIX_ID = "key_matrix_id";
    public static final String KEY_DISPLAY_NAME = "key_display_name";
    public static final String KEY_URL = "key_url";
    public static final String KEY_FILE_NAME = "key_file_name";
    public static final String KEY_FILE_SIZE = "key_file_size";
    public static final String KEY_MIME_TYPE = "key_mime_type";
    public static final String KEY_SEND_TIME = "key_send_time";
    public static final String KEY_COLLECTION_ID = "key_collection_id";
    public static final String KEY_SINGLE_CHOOSE = "key_single_choose";
    public static final String KEY_SELECT_IDS = "key_select_ids";
    public static final String KEY_FORWARD_TEXTS = "key_forward_texts";
    public static final String KEY_FORWARD_ATTACHMENTS = "key_forward_attachments";
    public static final String KEY_FORWARD_FOR_OUTSIDE = "key_forward_for_outside";
    public static final String KYE_MERGE_EVENT_IDS = "kye_merge_event_ids";
    public static final String KYE_FORWARD_EVENT_IDS = "kye_forward_event_ids";
    public static final String KYE_FORWARD_FROM_ROOM_ID = "kye_forward_from_room_id";
    public static final String KEY_SEARCH_WORD = "key_search_word";
    public static final String KEY_SEARCH_PREFIX = "key_search_prefix";
    public static final String KEY_IS_DIRECT = "key_is_direct";
    public static final String KEY_MULTI_MODE = "key_multi_mode";
    public static final String KEY_SELECT_ROOMID = "key_select_roomid";
    public static final String KEY_SELECT_TYPE = "key_select_type";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_IS_VIDEO_CALL = "key_is_video_call";
    public static final String KEY_TARGET_PATH = "key_target_path";

    public static Pattern MOBILE_PHONE = Pattern.compile(
            "(^|(?<=\\D))((1\\d{10})|(0\\d{10,11})|(1\\d{2}-\\d{4}-\\d{4})|(0\\d{2,3}-\\d{7,8})|(\\d{7,8})|((4|8)00\\d{1}-\\d{3}-\\d{3})|((4|8)00\\d{7}))(?!\\d)");
    /**
     * Moved form AppConstant
     */
    //部门
    public final static int DEPARTMENT_TYPE = 0;
    //搜索部门
    public final static int SEARCH_DEPARTMENT_TYPE = 5;
    //人员
    public final static int USER_TYPE = 1;
    //搜索用户
    public final static int SEARCH_USER_TYPE = 1;
    //搜索联系人
    public final static int SEARCH_CONTACT_TYPE = 12;
    // Search Room saved in contacts app
    public final static int SEARCH_CONTACT_GROUP_TYPE = 13;

    public final static int SEARCH_CONTACT_USER_TYPE = 10;
    public final static int SEARCH_ORG_USER_TYPE = 11;
    public final static int SEARCH_MATRIX_USER_TYPE = 12;

    public final static int LARGE_MEMBER_COUNT = 100;

    public final static String SELECT_CREATE_GROUP = "create_group";
    public final static String SELECT_GROUP_ADD_MEMBER = "add_member_to_group";
    public final static String SELECT_START_DIRECT_CHAT = "direct_chat";

    public final static String MSGTYPE_MERGE = "each.chat.merge";
//    public final static List<CreateDirectRoomEntity> CREATE_DIRECT_ROOMS = new ArrayList<>();
    public final static String FLAVOR_XIAOMI = "xiaomi";
}
