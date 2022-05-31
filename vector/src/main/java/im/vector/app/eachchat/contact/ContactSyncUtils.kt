package im.vector.app.eachchat.contact

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPStaticUtils
import com.blankj.utilcode.util.TimeUtils
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.bean.OrgSearchInput
import im.vector.app.eachchat.contact.api.BaseConstant
import im.vector.app.eachchat.contact.api.ContactService
import im.vector.app.eachchat.contact.api.ContactServiceV2
import im.vector.app.eachchat.contact.api.bean.ContactIncrementInput
import im.vector.app.eachchat.contact.api.bean.ContactIncrementInputV2
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.ContactsMatrixUser
import im.vector.app.eachchat.contact.data.ContactsRoom
import im.vector.app.eachchat.contact.database.ContactDaoHelper
import im.vector.app.eachchat.contact.database.ContactDaoV2
import im.vector.app.eachchat.contact.database.ContactMatrixUserDao
import im.vector.app.eachchat.contact.database.ContactRoomDao
import im.vector.app.eachchat.contact.database.UpdateTimeDao
import im.vector.app.eachchat.contact.event.MQTTEvent
import im.vector.app.eachchat.contact.event.UpdateMyContactsEvent
import im.vector.app.eachchat.database.AppDatabase
import im.vector.app.eachchat.mqtt.MQTTService
import im.vector.app.eachchat.mqtt.MessageConstant
import im.vector.app.eachchat.mqtt.ModuleLoader
import im.vector.app.eachchat.mqtt.UserCache
import im.vector.app.eachchat.net.CloseableCoroutineScope
import im.vector.app.eachchat.net.NetConstant
import im.vector.app.eachchat.service.LoginApi
import im.vector.app.eachchat.utils.AppCache
import im.vector.app.eachchat.widget.bot.data.BotDao
import im.vector.app.eachchat.widget.bot.data.BotService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.matrix.android.sdk.api.extensions.orFalse
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.profile.ProfileService
import timber.log.Timber

/**
 * Created by chengww on 2020/11/6
 * @author chengww
 */
class ContactSyncUtils {

    private val tag = "mqtt"
    private var contactDao: ContactDaoHelper? = null
    private var contactDaoV2: ContactDaoV2? = null
    private var roomDao: ContactRoomDao? = null
    private var updateTimeDao: UpdateTimeDao? = null
    private var contactMatrixUserDao: ContactMatrixUserDao? = null
    private var botDao: BotDao? = null

    private var orgJob: Job? = null
    private var enterpriseSettingJob: Job? = null
    private var tenantNameJob: Job? = null
    private var contactJob: Job? = null
    private var roomJob: Job? = null
    private var gmsJob: Job? = null
    private val scope: CloseableCoroutineScope by lazy { CloseableCoroutineScope() }

    fun init(context: Context, application: Application) {
        if (contactDao == null) {
            contactDao = ContactDaoHelper.getInstance()
        }

        if (contactDaoV2 == null) {
            contactDaoV2 = AppDatabase.getInstance(context).contactDaoV2()
        }

        if (roomDao == null) {
            roomDao = AppDatabase.getInstance(context).contactRoomDao()
        }

        if (updateTimeDao == null) {
            updateTimeDao = AppDatabase.getInstance(context).updateTimeDao()
        }

        if (contactMatrixUserDao == null) {
            contactMatrixUserDao = AppDatabase.getInstance(context).contactMatrixUserDao()
        }

        if (botDao == null) {
            botDao = AppDatabase.getInstance(BaseModule.getContext()).botDao()
        }

        orgSettings()
        requestEnterpriseSettings()
        // requestTenantName()
        requestGMSConfig(application)
        syncBots()
    }

    private fun syncBots() {
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val response = BotService.getInstance().getBots()
                if (response.isSuccess) {
                    botDao?.bulkInsert(response.results)
                    Timber.e("群应用同步成功")
                } else {
                    Timber.e("群应用同步失败")
                }
            }.exceptionOrNull()?.let {
                Timber.e("群应用同步失败")
                it.printStackTrace()
            }
        }

    }

    private var service: ContactService = ContactService.getInstance()
    private var serviceV2: ContactServiceV2 = ContactServiceV2.getInstance()
    fun syncContacts() {
        if (contactDao == null) {
            LogUtils.iTag(tag, "Please first call the init function")
            return
        }
        contactJob = sync(SyncType.CONTACT)
    }

    fun syncContactsRooms() {
        if (roomDao == null) {
            LogUtils.iTag(tag, "Please first call the init function")
            return
        }
        roomJob = sync(SyncType.ROOM)
    }

    private fun orgSettings() {
        if (orgJob?.isActive.orFalse()) return
        orgJob = scope.launch(Dispatchers.IO) {
            runCatching {
                // Settings
                val response = service.settings()
                if (response.isSuccess) {
                    response.obj?.let {
                        SPStaticUtils.put(SP_CONTACTS_OPEN_ORG, it.openOrg > 0)
                        if (it.totalMembersFirstTime > 0) {
                            SPStaticUtils.put(
                                    BaseConstant.SP_CREATE_SELECT_TOTAL_COUNT,
                                    it.totalMembersFirstTime
                            )
                        }
                        if (it.maxOtherHsMembersFirstTime > 0) {
                            SPStaticUtils.put(
                                    BaseConstant.SP_CREATE_SELECT_OUTSIDE_COUNT,
                                    it.maxOtherHsMembersFirstTime
                            )
                        }
                        if (it.totalMembersNextTime > 0) {
                            SPStaticUtils.put(
                                    BaseConstant.SP_GROUP_ADD_MEMBER_COUNT,
                                    it.totalMembersNextTime
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestEnterpriseSettings() {
        if (enterpriseSettingJob?.isActive.orFalse()) return
        enterpriseSettingJob = scope.launch(Dispatchers.IO) {
            runCatching {
                val response = service.getEnterpriseSettings()
                if (response.isSuccess) {
                    response.obj?.let {
                        AppCache.setAutoAcceptInviteEnable(it.inOrg)
                        AppCache.setUploadFileLimit(it.uploadUserLimit)
                        AppCache.setUploadCompressImageLimit(it.uploadUserCompressLimit)
                    }
                }
            }
        }
    }

    private fun requestTenantName() {
        if (tenantNameJob?.isActive.orFalse()) return
        tenantNameJob = scope.launch(Dispatchers.IO) {
            runCatching {
                val response = service.getTenantName()
                if (response.isSuccess) {
                    response.obj?.let {
                        if (it.isBlank()) return@runCatching
                        AppCache.setTenantName(it)
                    }
                }
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    private fun requestGMSConfig(application: Application) {
        if (gmsJob?.isActive.orFalse()) return
        gmsJob = scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val homeServerUrl = AppCache.getTenantName()
                val response = LoginApi.getInstance()?.gms(OrgSearchInput(homeServerUrl))
                if (response?.isSuccess == true) {
                    val isOpenContact = response.obj?.book?.contactSwitch ?: false
                    AppCache.setIsOpenContact(isOpenContact)
                    val isOpenGroup = response.obj?.book?.groupSwitch ?: false
                    AppCache.setIsOpenGroup(isOpenGroup)
                    val isOpenOrg = response.obj?.book?.orgSwitch ?: false
                    AppCache.setIsOpenOrg(isOpenOrg)
                    val isOpenVideoCall = response.obj?.im?.videoSwitch ?: false
                    AppCache.setIsOpenVideoCall(isOpenVideoCall)
                    response.obj?.im?.videoLimit?.let { AppCache.setVideoLimit(it) }
                    response.obj?.im?.audioLimit?.let { AppCache.setAudioLimit(it) }
                    response.obj?.im?.uploadLimit?.let { AppCache.setUploadLimit(it) }
                    response.obj?.entry?.cooperationUrl?.let {
                        NetConstant.setServerHost(it)
                    }
                    response.obj?.mqtt?.mqttUrl?.let {
                        NetConstant.setMqttServiceHost(it)
                    }
                    AppCache.setTenantName(homeServerUrl)
                    ModuleLoader.loadModule(application)
                    MQTTService.sendMQTTEvent(MQTTEvent(MessageConstant.CMD_UPDATE_USER, UserCache.getUpdateUserTime(), 0))
                    MQTTService.sendMQTTEvent(MQTTEvent(MessageConstant.CMD_UPDATE_CONTACT, "0", 0))
                    MQTTService.sendMQTTEvent(MQTTEvent(MessageConstant.CMD_UPDATE_CONTACT_ROOM, "0", 0))
                    MQTTService.sendMQTTEvent(MQTTEvent(MessageConstant.CMD_UPDATE_DEPARTMENT, UserCache.getUpdateDepartmentTime(), 0))
                }
            }.exceptionOrNull()?.let {
                Timber.v("联系人同步异常")
                it.printStackTrace()
            }
        }
    }

    private fun sync(syncType: SyncType): Job? {
        val job: Job?
        val incrementName: String
        when (syncType) {
            SyncType.CONTACT -> {
                job = contactJob
                incrementName = "updateContact"
            }
            SyncType.ROOM    -> {
                job = roomJob
                incrementName = "updateContactRoom"
            }
        }
        if (job?.isActive.orFalse()) return null
        return scope.launch(Dispatchers.IO) {
            runCatching {
                // sync contacts
                val contactsUpdateTime = updateTimeDao?.getContactsTime() ?: 0
                val increment = when (syncType) {
                    SyncType.CONTACT -> {
                        serviceV2.getIncrement(
                                ContactIncrementInputV2(
                                        contactsUpdateTime, PER_PAGE, 0
                                )
                        )
                    }
                    SyncType.ROOM    -> service.incrementContactRoom(
                            ContactIncrementInput(
                                    incrementName, updateTimeDao?.getContactsRoomsTime()
                                    ?: 0, PER_PAGE, 0
                            )
                    )
                }
                if (increment.isSuccess) {
                    increment.obj?.let {
                        if (it.updateTime > 0) {
                            val tempUpdateTime = TimeUtils.millis2String(it.updateTime)
                            when (syncType) {
                                SyncType.CONTACT -> {
                                    updateTimeDao?.updateContactsTimeV2(it.updateTime)
                                    LogUtils.iTag(tag, "CMD_UPDATE_CONTACT response-updateTime = $tempUpdateTime")
                                }
                                SyncType.ROOM    -> {
                                    updateTimeDao?.updateContactsRoomsTime(it.updateTime)
                                    LogUtils.iTag(tag, "CMD_UPDATE_CONTACT_ROOM response-updateTime = $tempUpdateTime")
                                }
                            }
                        }
                    }

                    val contacts = increment.results
                    when (syncType) {
                        SyncType.CONTACT -> {
                            if (contacts != null) {
                                syncContactEndV2(contacts)
                                if (increment.hasNext) {
                                    getNextContactsIncrement(contactsUpdateTime, contacts.size)
                                }
                            }
                            LogUtils.iTag(tag, "CMD_UPDATE_CONTACT response-size = ${contacts?.size}")
                        }
                        SyncType.ROOM    -> {
                            if (contacts != null) {
                                syncContactRoomEnd(contacts)
                            }
                            LogUtils.iTag(tag, "CMD_UPDATE_CONTACT_ROOM response-size = ${contacts?.size}")
                        }
                    }
                }
            }
        }
    }

    private suspend fun getNextContactsIncrement(contactsUpdateTime: Long, sequence: Int) {
        val increment = serviceV2.getIncrement(
                ContactIncrementInputV2(
                        contactsUpdateTime, PER_PAGE, sequence
                )
        )
        if (increment.isSuccess) {
            val contacts = increment.results
            if (contacts != null) {
                syncContactEndV2(contacts)
                if (increment.hasNext) {
                    getNextContactsIncrement(contactsUpdateTime, sequence + contacts.size)
                } else {
                    increment.obj?.updateTime?.let { updateTimeDao?.updateContactsTimeV2(it) }
                }
            }
        }
    }

    private fun syncContactEndV2(contacts: List<Any?>) {
        contacts.forEach { contact ->
            (contact as? ContactsDisplayBeanV2)?.let {
                contactDaoV2?.insertContact(contact)
            }
        }
        if (contacts.isNotEmpty().orFalse()) {
            EventBus.getDefault().post(UpdateMyContactsEvent)
        }
    }

    private fun syncContactRoomEnd(contacts: List<Any?>) {
        contacts.forEach { room ->
            (room as? ContactsRoom)?.let {
                if (it.roomDel) roomDao?.delete(it.roomId)
                else roomDao?.insertRooms(it)
            }
        }
    }

    private var session: Session? = null
    fun syncContactMatrixUser(matrixId: String) {
        if (session == null) session = BaseModule.getSession()
        session?.let { session ->
            scope.launch(Dispatchers.IO) {
                runCatching {
//                    val data = awaitCallback<JsonDict> {
//                        session.getProfile(matrixId, it)
//                    }
                    val data = withContext(Dispatchers.IO) {
                        session.getProfile(matrixId)
                    }
                    val displayName = data[ProfileService.DISPLAY_NAME_KEY] as? String?
                    val avatar = data[ProfileService.AVATAR_URL_KEY] as? String?
                    if (!displayName.isNullOrEmpty() || !avatar.isNullOrEmpty()) {
                        contactMatrixUserDao?.insert(
                                ContactsMatrixUser(
                                        matrixId,
                                        avatar ?: "",
                                        displayName ?: "",
                                        System.currentTimeMillis()
                                )
                        )
                    }
                }
            }
        }
    }

    fun isOpenOrg(): Boolean = SPStaticUtils.getBoolean(SP_CONTACTS_OPEN_ORG, true)

    companion object {
        private const val SP_CONTACTS_OPEN_ORG = "sp_contacts_open_org"
        private const val PER_PAGE = 200

        private var INSTANCE: ContactSyncUtils? = null

        @JvmStatic
        fun getInstance() = INSTANCE ?: ContactSyncUtils().also { INSTANCE = it }

        /**
         * Used to force [getInstance] to create a new instance
         * next time it's called.
         */
        @JvmStatic
        fun destroyInstance() {
            INSTANCE?.enterpriseSettingJob?.cancel()
            INSTANCE?.orgJob?.cancel()
            INSTANCE?.contactJob?.cancel()
            INSTANCE?.roomJob?.cancel()
            INSTANCE?.gmsJob?.cancel()
            INSTANCE?.scope?.close()
            INSTANCE = null
        }
    }
}

private enum class SyncType {
    CONTACT, ROOM
}
