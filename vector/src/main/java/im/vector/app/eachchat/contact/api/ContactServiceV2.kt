package im.vector.app.eachchat.contact.api

import im.vector.app.eachchat.bean.Response
import im.vector.app.eachchat.net.NetWorkManager
import im.vector.app.eachchat.contact.api.bean.ContactIncrementInputV2
import im.vector.app.eachchat.contact.api.bean.ContactIncrementResultObject
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ContactServiceV2 {
    //添加联系人
    @POST("/api/apps/contacts/v1/contact/info")
    suspend fun add(@Body contact: ContactsDisplayBeanV2?): Response<ContactsDisplayBeanV2?, Any?>

    //更新联系人
    @PUT("/api/apps/contacts/v1/contact/info")
    suspend fun edit(@Body contact: ContactsDisplayBeanV2): Response<ContactsDisplayBeanV2?, Any?>

    //删除联系人
    @HTTP(path = "/api/apps/contacts/v1/contact/info/{contactId}", method = "DELETE", hasBody = true)
    suspend fun delete(@Path("contactId") contactId : String): Response<Any?, Any?>

    //上传v-card文件
    @Multipart
    @POST("/api/apps/contacts/v1/contact/upload")
    suspend fun uploadVcf(@Part part: MultipartBody.Part): Response<Any?, Any?>

    //导出v-card文件
    @Streaming
    @GET("/api/apps/contacts/v1/contact/export")
    suspend fun exportVcf(): retrofit2.Response<ResponseBody?>


    //获取联系人增量
    @HTTP(path = "/api/apps/contacts/v1/contact/increment",method = "POST", hasBody = true)
    suspend fun getIncrement(@Body input: ContactIncrementInputV2?): Response<ContactIncrementResultObject?, List<ContactsDisplayBeanV2?>>

    companion object {
        fun getInstance(): ContactServiceV2 = NetWorkManager.getInstance().retrofit.create(ContactServiceV2::class.java)
        fun getCustomTimeOutInstance(timeOut: Int): ContactServiceV2 = NetWorkManager.getInstance().getCustomTimeoutRetrofit(timeOut).create(ContactServiceV2::class.java)
    }
}
