package im.vector.app.yiqia.net.interceptor;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.matrix.android.sdk.api.session.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import im.vector.app.eachchat.BaseModule;
import im.vector.app.yiqia.cache.TokenStore;
import im.vector.app.yiqia.net.data.NetConstant;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhouguanjie on 2019/8/9.
 */
public class HeaderInterceptor implements Interceptor {

    private final HashMap<String, Object> headers;
    private final Session session;

    public HeaderInterceptor(HashMap<String, Object> headers, Session session) {
        this.headers = headers;
        this.session = session;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        if (headers != null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        HttpUrl url = requestBuilder.build().url();
        String requestPath = url.encodedPath();
        if (TextUtils.equals(requestPath, "/api/services/auth/v1/logout") || TextUtils.equals(requestPath, "/api/services/auth/v1/token/refresh")) {
            if (!TextUtils.isEmpty(TokenStore.getRefreshToken(session))) {
                requestBuilder.addHeader(NetConstant.AUTHORIZATION, String.format("%s %s", NetConstant.BEARER, TokenStore.getRefreshToken(session)));
            }
        } else if (!TextUtils.equals(requestPath, "/api/services/auth/v1/login")) {
            if (!TextUtils.isEmpty(TokenStore.getAccessToken(session))) {
                requestBuilder.addHeader(NetConstant.AUTHORIZATION, String.format("%s %s", NetConstant.BEARER, TokenStore.getAccessToken(session)));
            }
        }

        Request request = requestBuilder.build();
        Response.Builder responseBuilder = chain.proceed(request).newBuilder();
        return responseBuilder.build();
    }
}
