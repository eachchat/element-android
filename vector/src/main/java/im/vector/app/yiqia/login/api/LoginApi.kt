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

package im.vector.app.yiqia.login.api

import im.vector.app.yiqia.login.data.AuthSettingResult
import im.vector.app.yiqia.login.data.GMSResult
import im.vector.app.yiqia.login.data.OrgSearchInput
import im.vector.app.yiqia.net.data.Response
import im.vector.app.yiqia.net.retrofit.RetrofitManager
import org.matrix.android.sdk.api.session.Session
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface LoginApi {

    companion object {
        const val GMS_URL = "https://gms.yiqia.com";

        fun getInstance(): LoginApi = RetrofitManager.instance.getMatrixRetrofit(GMS_URL).create(
            LoginApi::class.java)
    }

    @POST("/api/services/global/v1/configuration")
    suspend fun gms(@Body input: OrgSearchInput): Response<GMSResult?, Any?>

    @POST("/api/services/global/v1/tenant/names")
    suspend fun orgNames(@Body input: OrgSearchInput): Response<Any?, List<String>>

    @GET
    suspend fun authSettings(@Url url: String): Response<AuthSettingResult?, Any?>
}


