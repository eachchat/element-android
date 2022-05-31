/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.eachchat.widget.bot.data

import im.vector.app.eachchat.bean.Response
import im.vector.app.eachchat.net.NetWorkManager
import retrofit2.http.GET
import retrofit2.http.Path

interface BotService {
    @GET("/api/services/global/v1/matrix/apps")
    suspend fun getBots(): Response<Any?, List<Bot>?>

    @GET("/api/services/global/v1/matrix/app/{appMatrixId}/detail")
    suspend fun getBot(@Path("appMatrixId") userId: String): Response<Any?, Bot?>

    companion object {
        const val baseUrl = "http://139.198.18.180:8888"

        fun getInstance(): BotService = NetWorkManager.getInstance().newMatrixRetrofit(baseUrl).create(BotService::class.java)
    }
}
