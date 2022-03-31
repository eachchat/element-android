package im.vector.app.yiqia.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import im.vector.app.eachchat.BaseModule;
import im.vector.app.eachchat.bean.Response;
import im.vector.app.yiqia.contact.ContactSyncUtils;
import im.vector.app.yiqia.contact.api.bean.Department;
import im.vector.app.yiqia.contact.data.IncrementUpdateInput;
import im.vector.app.yiqia.contact.data.UpdateGroupValue;
import im.vector.app.yiqia.contact.data.User;
import im.vector.app.yiqia.contact.event.MQTTEvent;
import im.vector.app.yiqia.contact.event.UpdateDepartmentEvent;
import im.vector.app.yiqia.contact.event.UpdateUserCompleteEvent;
import im.vector.app.yiqia.contact.event.UpdateUserEvent;
import im.vector.app.yiqia.database.AppDatabase;
import im.vector.app.yiqia.rx.SimpleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhouguanjie on 2019/9/10.
 * <p>
 */
public class MQTTService extends Service {

    private final static String TAG = "mqtt";
    private final static String KEY_CMD = "key_cmd";
    private final static String KEY_NOTICE_ID = "key_notice_id";
    private final static String KEY_UPDATE_TIME = "key_update_time";
    private long curUpdateUserTime = 0;

    public static void start(Context context, String cmd, long noticeId, String updateTime) {
        try {
            Intent intent = new Intent(context, MQTTService.class);
            intent.putExtra(KEY_CMD, cmd);
            intent.putExtra(KEY_NOTICE_ID, noticeId);
            intent.putExtra(KEY_UPDATE_TIME, updateTime);
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMQTTEvent(MQTTEvent event) {
        if (event == null) {
            return;
        }
        //TODO
        MQTTService.start(this, event.cmd, event.noticeId, event.updateTime);
    }

    public static void sendMQTTEvent(MQTTEvent event) {
        MQTTService.start(BaseModule.getContext(), event.cmd, event.noticeId, event.updateTime);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent != null) {
                String cmd = intent.getStringExtra(KEY_CMD);
                // long noticeId = intent.getLongExtra(KEY_NOTICE_ID, 0);
                String updateTime = intent.getStringExtra(KEY_UPDATE_TIME);
                onEvent(cmd, updateTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onEvent(String event, String updateTime) {
        if (TextUtils.isEmpty(event)) {
            return;
        }
        String mqttTime = "";
        if (!TextUtils.isEmpty(updateTime)) {
            mqttTime = TimeUtils.millis2String(Long.parseLong(updateTime));
        }
        switch (event) {
            case MessageConstant.CMD_UPDATE_USER:
                long t = Long.parseLong(updateTime);
                if (t == curUpdateUserTime && t != 0) {
                    return;
                }
                curUpdateUserTime = t;
                LogUtils.iTag(TAG, "CMD_UPDATE_USER, updateTime = " + mqttTime);
                onUpdateUser(0);
                break;
            case MessageConstant.CMD_UPDATE_CONTACT:
                LogUtils.iTag(TAG, "CMD_UPDATE_CONTACT, updateTime = " + mqttTime);
                ContactSyncUtils.getInstance().syncContacts();
                break;
            case MessageConstant.CMD_UPDATE_CONTACT_ROOM:
                LogUtils.iTag(TAG, "CMD_UPDATE_CONTACT_ROOM, updateTime = " + mqttTime);
                ContactSyncUtils.getInstance().syncContactsRooms();
                break;
            case MessageConstant.CMD_UPDATE_DEPARTMENT:
                LogUtils.iTag(TAG, "CMD_UPDATE_DEPARTMENT, updateTime = " + mqttTime);
                onUpdateDepartment(0);
                break;
            case MessageConstant.CMD_LOCAL_UPDATE_ALL:
                if (UserCache.isInitDepartments()) {
                    onUpdateDepartment(0);
                }
                if (UserCache.isInitContacts()) {
                    onUpdateUser(0);
                }
                break;
        }

    }

    public void onUpdateUser(int sequenceId) {
        String time = UserCache.getUpdateUserTime();
        IncrementUpdateInput input = new IncrementUpdateInput();
        input.setName("updateUser");
        input.setSequenceId(sequenceId);
        input.setUpdateTime(Long.parseLong(time));
        ApiService.getUserService().updateUsers(input)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new SimpleObserver<retrofit2.Response<Response<UpdateGroupValue, List<User>>>>() {
                    @Override
                    public void onNext(retrofit2.Response<Response<UpdateGroupValue, List<User>>> responseResponse) {
                        curUpdateUserTime = 0;
                        if (!responseResponse.isSuccessful()) {
                            return;
                        }
                        Response<UpdateGroupValue, List<User>> response = responseResponse.body();
                        if (response == null || !response.isSuccess() || response.getObj() == null) {
                            return;
                        }

                        try {
                            // use SQLiteDatabase encrypted storage
                            AppDatabase.getInstance(BaseModule.getContext()).UserDao().bulkInsert(response.getResults());
                            EventBus.getDefault().post(UpdateUserEvent.INSTANCE);
                            LogUtils.iTag(TAG, "CMD_UPDATE_USER load success, response.getResults().size() = " + response.getResults().size());
                            if (response.isHasNext()) {
                                onUpdateUser(input.getSequenceId() + response.getResults().size());
                            } else {
                                UserCache.setUpdateUserTime(String.valueOf(response.getObj().getUpdateTime()));
                                EventBus.getDefault().post(new UpdateUserCompleteEvent());

                                String tempUpdateTime = TimeUtils.millis2String(response.getObj().getUpdateTime());
                                LogUtils.iTag(TAG, "CMD_UPDATE_USER load complete, response.getObj().getUpdateTime()= " + tempUpdateTime);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        curUpdateUserTime = 0;
                    }
                });
    }

    public void onUpdateDepartment(int sequenceId) {
        String time = UserCache.getUpdateDepartmentTime();
        IncrementUpdateInput input = new IncrementUpdateInput();
        input.setName("updateDepartment");
        input.setSequenceId(sequenceId);
        input.setUpdateTime(Long.parseLong(time));
        ApiService.getUserService().updateDepartments(input)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new SimpleObserver<retrofit2.Response<Response<UpdateGroupValue, List<Department>>>>() {
                    @Override
                    public void onNext(retrofit2.Response<Response<UpdateGroupValue, List<Department>>> responseResponse) {
                        if (!responseResponse.isSuccessful()) {
                            return;
                        }
                        Response<UpdateGroupValue, List<Department>> response = responseResponse.body();
                        if (response == null || !response.isSuccess() || response.getObj() == null) {
                            return;
                        }
                        try {
                            AppDatabase.getInstance(BaseModule.getContext()).departmentDao().bulkInsert(response.getResults());
                            EventBus.getDefault().post(UpdateDepartmentEvent.INSTANCE);

                            LogUtils.iTag(TAG, "CMD_UPDATE_DEPARTMENT load success, response.getResults().size() = " + response.getResults().size());
                            if (response.isHasNext()) {
                                onUpdateDepartment(input.getSequenceId() + response.getResults().size());
                            } else {
                                UserCache.setUpdateDepartmentTime(String.valueOf(response.getObj().getUpdateTime()));

                                String tempUpdateTime = TimeUtils.millis2String(response.getObj().getUpdateTime());
                                LogUtils.iTag(TAG, "CMD_UPDATE_DEPARTMENT load complete, response.getObj().getUpdateTime()= " + tempUpdateTime);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private boolean isUpdatingTeamMessage = false;

//    private void onUpdateTeamMessage() {
//        if (isUpdatingTeamMessage) {
//            return;
//        }
//        isUpdatingTeamMessage = true;
//        if (UserCache.getUpdateTeamMessageSeqId() == -1L) {
//            initTeamChat();
//            return;
//        }
//        Observable.create((ObservableOnSubscribe<Response<Object, List<Message>>>) emitter -> {
//            Response<Object, List<Message>> response = IMManager.getClient().getTeamIncrement(1,
//                    50, UserCache.getUpdateTeamMessageSeqId());
//            long seqId = 0;
//            if (response.isSuccess()) {
//                if (response.getResults() != null) {
//                    List<Message> newMessages = new ArrayList<>();
//                    for (Message message : response.getResults()) {
//                        MessageStoreHelper.insertOrUpdate(message);
//                        if (message.getSequenceId() > seqId) {
//                            seqId = message.getSequenceId();
//                        }
//                        if (TextUtils.equals(String.valueOf(message.getTeamId()), UserCache.getCurrentGroupId())) {
//                            newMessages.add(message);
//                        }
//                    }
//                    if (newMessages.size() > 0) {
//                        EventBus.getDefault().post(new SyncMessageEvent(newMessages));
//                    }
//                    if (seqId > UserCache.getUpdateTeamMessageSeqId()) {
//                        UserCache.saveUpdateTeamMessageSeqId(seqId);
//                    }
//                }
//            }
//            emitter.onNext(response);
//        }).subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SimpleObserver<Response<Object, List<Message>>>() {
//                    @Override
//                    public void onNext(Response<Object, List<Message>> response) {
//                        isUpdatingTeamMessage = false;
//                        if (response.isSuccess() && response.isHasNext()
//                                && response.getResults() != null
//                                && response.getResults().size() > 0) {
//                            onUpdateTeamMessage();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        super.onError(e);
//                        e.printStackTrace();
//                        isUpdatingTeamMessage = false;
//                    }
//                });
//    }

//    private void initTeamChat() {
//        Observable.create((ObservableOnSubscribe<Response<MaxSeqIdBean, Object>>) emitter -> {
//            List<Integer> teamIds = new ArrayList<>();
//            teamIds.add(-1);
//            Response<MaxSeqIdBean, Object> response = IMManager.getClient().initTeamChat(teamIds);
//            emitter.onNext(response);
//        }).subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SimpleObserver<Response<MaxSeqIdBean, Object>>() {
//                    @Override
//                    public void onNext(Response<MaxSeqIdBean, Object> response) {
//                        isUpdatingTeamMessage = false;
//                        if (response.isSuccess() && response.getObj() != null) {
//                            long maxSeqId = response.getObj().getMaxSequenceId();
//                            if (maxSeqId > 0) {
//                                UserCache.saveUpdateTeamMessageSeqId(maxSeqId);
//                                onUpdateTeamMessage();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        super.onError(e);
//                        isUpdatingTeamMessage = false;
//                    }
//                });
//    }

}
