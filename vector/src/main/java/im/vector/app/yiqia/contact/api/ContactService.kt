package im.vector.app.yiqia.contact.api

import im.vector.app.eachchat.bean.Response
import im.vector.app.eachchat.net.NetWorkManager
import im.vector.app.yiqia.contact.api.bean.ContactIncrementBean
import im.vector.app.yiqia.contact.api.bean.ContactIncrementInput
import im.vector.app.yiqia.contact.api.bean.ContactIncrementResultObject
import im.vector.app.yiqia.contact.api.bean.ContactRoomBean
import im.vector.app.yiqia.contact.api.bean.ContactSettingsResult
import im.vector.app.yiqia.contact.api.bean.MatrixIdBean
import im.vector.app.yiqia.contact.api.bean.RemarkName
import im.vector.app.yiqia.contact.data.ContactsDisplayBean
import im.vector.app.yiqia.contact.data.ContactsRoom
import im.vector.app.yiqia.contact.data.EnterpriseSettingEntity
import retrofit2.http.*

/**
 * Created by chengww on 2020/11/4
 * @author chengww
 */
interface ContactService {
    @GET("/api/apps/contacts/v1/contact/setting")
    suspend fun settings(): Response<ContactSettingsResult?, Any?>

    @POST("/api/apps/contacts/v1/increment")
    suspend fun incrementContact(@Body input: ContactIncrementInput): Response<ContactIncrementResultObject?, List<ContactIncrementBean?>>

    @POST("/api/apps/contacts/v1/increment")
    suspend fun incrementContactRoom(@Body input: ContactIncrementInput): Response<ContactIncrementResultObject?, List<ContactsRoom?>>

    @HTTP(path = "/api/apps/contacts/v1/contact", method = "DELETE", hasBody = true)
    suspend fun delete(@Body matrixIdBean: MatrixIdBean): Response<Any?, Any?>

    @POST("/api/apps/contacts/v1/contact")
    suspend fun add(@Body contact: ContactsDisplayBean): Response<ContactIncrementBean?, Any?>

    @PATCH("/api/apps/contacts/v1/contact")
    suspend fun edit(@Body contact: ContactsDisplayBean): Response<ContactIncrementBean?, Any?>

    @PATCH("/api/apps/contacts/v1/contact/remarkname")
    suspend fun remark(@Body remarkName: RemarkName): Response<Any?, Any?>

    @POST("/api/apps/contacts/v1/contact/room")
    suspend fun addRoom(@Body input: ContactRoomBean): Response<ContactsRoom?, Any?>

    @HTTP(path = "/api/apps/contacts/v1/contact/room", method = "DELETE", hasBody = true)
    suspend fun deleteRoom(@Body input: ContactRoomBean): Response<Any?, Any?>

    @GET("/api/services/security/v1/blacklist")
    suspend fun getBlackList(): Response<List<String>, Any?>

    @GET("/api/apps/org/v1/setting/enterprise")
    suspend fun getEnterpriseSettings(): Response<EnterpriseSettingEntity, Any?>

    @GET(ApiConstant.URI_API_PREFIX_PATH_R0 + "user/{userId}/account_data/m.direct")
    suspend fun getDirectMessages(@Path("userId") userId: String): retrofit2.Response<Map<String, List<String>>?>

    @GET("/api/apps/org/v1/tenant/name")
    suspend fun getTenantName(): Response<String?, Any?>

    companion object {
        fun getInstance(): ContactService = NetWorkManager.getInstance().retrofit.create(ContactService::class.java)
    }
}
