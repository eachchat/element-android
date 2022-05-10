package im.vector.app.eachchat.service

import im.vector.app.eachchat.bean.Response
import im.vector.app.eachchat.net.NetWorkManager
import im.vector.app.eachchat.bean.SearchGroupCountInput
import im.vector.app.eachchat.bean.SearchGroupCountResponse
import im.vector.app.eachchat.bean.SearchGroupMessageResponse
import im.vector.app.eachchat.bean.SearchInput
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by zhouguanjie on 2021/1/4.
 */
interface SearchService {

    @POST("/api/services/search/v1/chats")
    suspend fun searchGroupMessage(@Body input: SearchInput): Response<Any?, List<SearchGroupMessageResponse?>>

    @POST("/api/services/search/v1/chats/statistics")
    suspend fun searchGroupMessageCount(@Body input: SearchGroupCountInput): Response<SearchGroupCountResponse?, Any?>

    companion object {
        fun getInstance(): SearchService = NetWorkManager.getInstance().retrofit.create(SearchService::class.java)
    }

}
