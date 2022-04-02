package im.vector.app.yiqia.mqtt

import android.app.Service
import android.content.Context
import im.vector.app.yiqia.contact.ContactSyncUtils.Companion.getInstance
import im.vector.app.yiqia.database.AppDatabase.Companion.getInstance
import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import im.vector.app.yiqia.contact.event.MQTTEvent
import im.vector.app.yiqia.mqtt.MQTTService
import im.vector.app.yiqia.contact.ContactSyncUtils
import im.vector.app.yiqia.mqtt.UserCache
import im.vector.app.yiqia.contact.data.IncrementUpdateInput
import im.vector.app.yiqia.rx.SimpleObserver
import im.vector.app.yiqia.contact.data.UpdateGroupValue
import im.vector.app.yiqia.database.AppDatabase
import im.vector.app.eachchat.BaseModule
import im.vector.app.eachchat.bean.Response
import im.vector.app.yiqia.contact.event.UpdateUserEvent
import im.vector.app.yiqia.contact.event.UpdateUserCompleteEvent
import im.vector.app.yiqia.contact.api.bean.Department
import im.vector.app.yiqia.contact.data.User
import im.vector.app.yiqia.contact.event.UpdateDepartmentEvent
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception

/**
 * Created by zhouguanjie on 2019/9/10.
 *
 *
 */
class MQTTService : Service() {
    private var curUpdateUserTime: Long = 0
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMQTTEvent(event: MQTTEvent?) {
        if (event == null) {
            return
        }
        //TODO
        start(this, event.cmd, event.noticeId, event.updateTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            val cmd = intent.getStringExtra(KEY_CMD)
            // long noticeId = intent.getLongExtra(KEY_NOTICE_ID, 0);
            val updateTime = intent.getStringExtra(KEY_UPDATE_TIME)
            onEvent(cmd, updateTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun onEvent(event: String?, updateTime: String?) {
        if (TextUtils.isEmpty(event)) {
            return
        }
        var mqttTime = ""
        if (!TextUtils.isEmpty(updateTime)) {
            mqttTime = TimeUtils.millis2String(updateTime!!.toLong())
        }
        when (event) {
            MessageConstant.CMD_UPDATE_USER         -> {
                val t = updateTime!!.toLong()
                if (t == curUpdateUserTime && t != 0L) {
                    return
                }
                curUpdateUserTime = t
                LogUtils.iTag(TAG, "CMD_UPDATE_USER, updateTime = $mqttTime")
                onUpdateUser(0)
            }
            MessageConstant.CMD_UPDATE_CONTACT      -> {
                LogUtils.iTag(TAG, "CMD_UPDATE_CONTACT, updateTime = $mqttTime")
                getInstance().syncContacts()
            }
            MessageConstant.CMD_UPDATE_CONTACT_ROOM -> {
                LogUtils.iTag(TAG, "CMD_UPDATE_CONTACT_ROOM, updateTime = $mqttTime")
                getInstance().syncContactsRooms()
            }
            MessageConstant.CMD_UPDATE_DEPARTMENT   -> {
                LogUtils.iTag(TAG, "CMD_UPDATE_DEPARTMENT, updateTime = $mqttTime")
                onUpdateDepartment(0)
            }
            MessageConstant.CMD_LOCAL_UPDATE_ALL    -> {
                if (UserCache.isInitDepartments()) {
                    onUpdateDepartment(0)
                }
                if (UserCache.isInitContacts()) {
                    onUpdateUser(0)
                }
            }
        }
    }

    fun onUpdateUser(sequenceId: Int) {
        // val time = UserCache.getUpdateUserTime()
        val input = IncrementUpdateInput()
        input.name = "updateUser"
        input.sequenceId = sequenceId
        input.updateTime = 0
        input.perPage = 50
        ApiService.getUserService().updateUsers(input)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(object : SimpleObserver<retrofit2.Response<Response<UpdateGroupValue?, List<User?>?>?>?>() {
                    override fun onNext(responseResponse: retrofit2.Response<Response<UpdateGroupValue?, List<User?>?>?>?) {
                        curUpdateUserTime = 0
                        if (responseResponse?.isSuccessful != true) {
                            return
                        }
                        val response = responseResponse.body()
                        if (response == null || !response.isSuccess || response.obj == null) {
                            return
                        }
                        try {
                            // use SQLiteDatabase encrypted storage
                            getInstance(BaseModule.getContext()).UserDao().bulkInsert(response.results)
                            EventBus.getDefault().post(UpdateUserEvent)
                            LogUtils.iTag(TAG, "CMD_UPDATE_USER load success, response.getResults().size() = " + response.results!!.size)
                            if (response.hasNext) {
                                onUpdateUser(input.sequenceId + response.results!!.size)
                            } else {
                                UserCache.setUpdateUserTime(response.obj!!.updateTime.toString())
                                EventBus.getDefault().post(UpdateUserCompleteEvent())
                                val tempUpdateTime = TimeUtils.millis2String(response.obj!!.updateTime)
                                LogUtils.iTag(TAG, "CMD_UPDATE_USER load complete, response.getObj().getUpdateTime()= $tempUpdateTime")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        curUpdateUserTime = 0
                    }
                })
    }

    fun onUpdateDepartment(sequenceId: Int) {
        // val time = UserCache.getUpdateDepartmentTime()
        val input = IncrementUpdateInput()
        input.name = "updateDepartment"
        input.sequenceId = sequenceId
        input.updateTime = 0
        input.perPage = 50
        ApiService.getUserService().updateDepartments(input)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(object : SimpleObserver<retrofit2.Response<Response<UpdateGroupValue?, List<Department?>?>?>?>() {
                    override fun onNext(responseResponse: retrofit2.Response<Response<UpdateGroupValue?, List<Department?>?>?>?) {
                        if (responseResponse?.isSuccessful  != true) {
                            return
                        }
                        val response = responseResponse.body()
                        if (response == null || !response.isSuccess || response.obj == null) {
                            return
                        }
                        try {
                            getInstance(BaseModule.getContext()).departmentDao().bulkInsert(response.results)
                            EventBus.getDefault().post(UpdateDepartmentEvent)
                            LogUtils.iTag(TAG, "CMD_UPDATE_DEPARTMENT load success, response.getResults().size() = " + response.results!!.size)
                            if (response.hasNext) {
                                onUpdateDepartment(input.sequenceId + response.results!!.size)
                            } else {
                                UserCache.setUpdateDepartmentTime(response.obj!!.updateTime.toString())
                                val tempUpdateTime = TimeUtils.millis2String(response.obj!!.updateTime)
                                LogUtils.iTag(TAG, "CMD_UPDATE_DEPARTMENT load complete, response.getObj().getUpdateTime()= $tempUpdateTime")
                            }
                        } catch (e: Exception) {
                            LogUtils.iTag(TAG, "CMD_UPDATE_DEPARTMENT load failed, response.getResults().size() = " + e.message)
                            e.printStackTrace()
                        }
                    }
                })
    }

    private val isUpdatingTeamMessage = false //    private void onUpdateTeamMessage() {

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
    companion object {
        private const val TAG = "mqtt"
        private const val KEY_CMD = "key_cmd"
        private const val KEY_NOTICE_ID = "key_notice_id"
        private const val KEY_UPDATE_TIME = "key_update_time"
        fun start(context: Context, cmd: String?, noticeId: Long, updateTime: String?) {
            try {
                val intent = Intent(context, MQTTService::class.java)
                intent.putExtra(KEY_CMD, cmd)
                intent.putExtra(KEY_NOTICE_ID, noticeId)
                intent.putExtra(KEY_UPDATE_TIME, updateTime)
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun sendMQTTEvent(event: MQTTEvent) {
            start(BaseModule.getContext(), event.cmd, event.noticeId, event.updateTime)
        }
    }
}
