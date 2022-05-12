package im.vector.app.eachchat.version_update

import im.vector.app.eachchat.bean.Response
import im.vector.app.eachchat.net.NetWorkManager
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by chengww on 2020/11/10
 * @author chengww
 */
interface EachChatSettingsService {
    @POST("/api/services/global/v1/version/new")
    suspend fun checkUpdate(@Body body: Map<String, String> = mapOf(Pair("client", "android")))
            : Response<VersionUpdateResult?, Any?>

    companion object {
        fun getInstance(homeServerUrl: String): EachChatSettingsService =
            NetWorkManager.getInstance().getMatrixRetrofit(homeServerUrl)
                ?.create(EachChatSettingsService::class.java)!!
    }
}
