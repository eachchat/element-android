package im.vector.app.eachchat.mqtt;

/**
 * Created by zhouguanjie on 2019/8/16.
 */
public class MessageConstant {

    public final static int TEXT_MSG = 101;
    public final static int IMAGE_MSG = 102;
    public final static int FILE_MSG = 103;
    public final static int FEED_BACK_MSG = 104;
    public final static int VOICE_MSG = 105;
    public final static int COMBINE_MSG = 106;
    public final static int VOICE_CALL_END_MSG = 107;
    public final static int VIDEO_CALL_END_MSG = 108;
    public final static int TOPIC_MSG = 109;
    public final static int UNKNOW_MSG = -1;


    public final static int CONNECT_FAILED = 100;

    public final static int CONNECT_LOST = 101;
    public final static int MESSAGE_SERVICE_ALREADY = 503;


    public final static int STATUS_SENDING = 1;
    public final static int STATUS_FAIL = 2;
    public final static int STATUS_PLAY = 3;
    public final static int STATUS_SUCCESS = 0;
    public final static int STATUS_DELETE = 1000;

    public final static String CMD_LOCAL_UPDATE_ALL = "localUpdateAll";

    public final static String CMD_UPDATE_GROUP = "updateGroup";
    public final static String CMD_UPDATE_ENCRYPTION_GROUP = "updateEncryptionGroup";

    public final static String CMD_NEW_MESSAGE = "newMessage";
    public final static String CMD_NEW_ENCRYPTION_MESSAGE = "newEncryptionMessage";

    public final static String CMD_UPDATE_USER = "updateUser";
    public final static String CMD_UPDATE_CONTACT = "updateContact";
    public final static String CMD_UPDATE_CONTACT_ROOM = "updateContactRoom";

    public final static String CMD_UPDATE_DEPARTMENT = "updateDepartment";
    public final static String CMD_UPDATE_ENCRYPTION_MESSAGE_READER = "updateMessageReader";
    public final static String CMD_AUDIO_VIDEO_CHAT_NOTICE = "AVChatStatusNotice";

    public final static String CMD_UPDATE_TEAM = "updateTeam";
    public final static String CMD_UPDATE_CONVERSATION = "updateConversation";


    public final static int GROUP_MULTI = 101;

    public final static int GROUP_SINGLE = 102;

    //标识公告
    public final static String NOTICE_TYPE = "notice";
    public final static String MENTION_TYPE = "mention";

    public final static String INVITATION_FEEDBACK = "invitation";
    public final static String NOTICE_FEEDBACK = "notice";
    public final static String UPDATE_GROUP_NAME_FEEDBACK = "updateGroupName";
    public final static String DELETE_GROUP_USER = "deleteGroupUser";
    public final static String GROUP_TRANSFER = "groupTransfer";

    public final static int PAGE_COUNT = 20;

}
