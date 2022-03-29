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

package im.vector.app.yiqia.net.retrofit

import im.vector.app.yiqia.net.data.NetConstant
import im.vector.app.yiqia.net.interceptor.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.matrix.android.sdk.api.session.Session
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class RetrofitManager {
    fun getMatrixRetrofit(url: String, session: Session): Retrofit {

        val httpLoggingInterceptor = HttpLoggingInterceptor { message ->
            try {
                val text: String = URLDecoder.decode(message, "utf-8")
                Timber.v("亿洽OKHttp: $text")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                Timber.v("亿洽OKHttp: $message")
            }
        }

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HeaderInterceptor(getRequestHeader(), session)) // token
                .addInterceptor(httpLoggingInterceptor).build()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    fun getMatrixRetrofit(url: String): Retrofit {

        val httpLoggingInterceptor = HttpLoggingInterceptor { message ->
            try {
                val text: String = URLDecoder.decode(message, "utf-8")
                Timber.v("亿洽OKHttp: $text")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                Timber.v("亿洽OKHttp: $message")
            }
        }

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor).build()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private fun getRequestHeader(): HashMap<String, Any>? {
        val parameters = HashMap<String, Any>()
        parameters[NetConstant.ACCEPT] = NetConstant.APPLICATION_JSON
        parameters[NetConstant.CONTENT_TYPE] = NetConstant.APPLICATION_JSON
        return parameters
    }

    companion object NetWorkManagerHolder {
        val instance: RetrofitManager = RetrofitManager()
    }
}
