package im.vector.app.eachchat.net;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.common.LogUtil;

import org.matrix.android.sdk.api.session.Session;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.inject.Inject;

import im.vector.app.BuildConfig;
import im.vector.app.core.di.ActiveSessionHolder;
import im.vector.app.eachchat.BaseModule;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Created by zhouguanjie on 2019/8/9.
 */
public class NetWorkManager {

    @Inject ActiveSessionHolder activeSessionHolder;


    private Retrofit retrofit, serverRetrofit, retrofitMoshi;

    private NetWorkManager() {
    }

    public static NetWorkManager getInstance() {
        return NetWorkManagerHolder.instance;
    }

    public void init(Context context) {
        Stetho.initializeWithDefaults(context);
    }


    private static HashMap<String, Object> getRequestHeader() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(NetConstant.ACCEPT, NetConstant.APPLICATION_JSON);
        parameters.put(NetConstant.CONTENT_TYPE, NetConstant.APPLICATION_JSON);
        return parameters;
    }


    public Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (NetWorkManager.class) {
                if (retrofit == null) {
                    MyLoggingInterceptor httpLoggingInterceptor = new MyLoggingInterceptor("yql-request");
                    httpLoggingInterceptor.setPrintLevel(BuildConfig.DEBUG ? MyLoggingInterceptor.Level.BODY
                            : MyLoggingInterceptor.Level.NONE);
                    httpLoggingInterceptor.setColorLevel(Level.INFO);

                    OkHttpClient client = new OkHttpClient
                            .Builder()
                            .addInterceptor(new HeaderInterceptor(getRequestHeader())) // token
                            .addInterceptor(httpLoggingInterceptor)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();
                    client.dispatcher().setMaxRequestsPerHost(8);
                    // retrofit
                    String baseUrl = NetConstant.getServerHostWithProtocol();
                    Retrofit.Builder builder = new Retrofit
                            .Builder()
                            .client(client)
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create());
                    try {
                        builder.baseUrl(baseUrl);  //baseUrl
                    } catch (Exception e) {
                        LogUtil.e("retrofit", e.getLocalizedMessage());
                        builder.baseUrl("http://no_app_url");
                    }
                    retrofit = builder.build();
                }
            }
        }
        return retrofit;
    }

    public Retrofit getMoshiRetrofit() {
        if (retrofitMoshi == null) {
            synchronized (NetWorkManager.class) {
                if (retrofitMoshi == null) {
                    MyLoggingInterceptor httpLoggingInterceptor = new MyLoggingInterceptor("yql-request");
                    httpLoggingInterceptor.setPrintLevel(BuildConfig.DEBUG ? MyLoggingInterceptor.Level.BODY
                            : MyLoggingInterceptor.Level.NONE);
                    httpLoggingInterceptor.setColorLevel(Level.INFO);

                    OkHttpClient client = new OkHttpClient
                            .Builder()
                            .addInterceptor(new HeaderInterceptor(getRequestHeader()))
                            .addInterceptor(httpLoggingInterceptor)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();
                    client.dispatcher().setMaxRequestsPerHost(8);
                    String baseUrl = NetConstant.getServerHostWithProtocol();
                    Retrofit.Builder builder = new Retrofit
                            .Builder()
                            .client(client)
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .addConverterFactory(MoshiConverterFactory.create());
                    try {
                        builder.baseUrl(baseUrl);
                    } catch (Exception e) {
                        LogUtil.e("retrofit", e.getLocalizedMessage());
                        builder.baseUrl("http://no_app_url");
                    }
                    retrofitMoshi = builder.build();
                }
            }
        }
        return retrofitMoshi;
    }

    public Retrofit getServerRetrofit() {
        if (serverRetrofit == null) {
            synchronized (NetWorkManager.class) {
                if (serverRetrofit == null) {
                    MyLoggingInterceptor httpLoggingInterceptor = new MyLoggingInterceptor("yql-download-request");
                    httpLoggingInterceptor.setPrintLevel(BuildConfig.DEBUG ? MyLoggingInterceptor.Level.BODY
                            : MyLoggingInterceptor.Level.NONE);
                    httpLoggingInterceptor.setColorLevel(Level.INFO);

                    OkHttpClient client = new OkHttpClient
                            .Builder()
                            .addInterceptor(new HeaderInterceptor(null))
                            .addInterceptor(httpLoggingInterceptor)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    Retrofit.Builder builder = new Retrofit
                            .Builder()
                            .client(client)
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

                    String baseUrl = NetConstant.getServerHostWithProtocol();

                    try {
                        builder.baseUrl(baseUrl);
                    } catch (Exception e) {
                        LogUtil.e("retrofit", e.getLocalizedMessage());
                        builder.baseUrl("http://no_app_url");
                    }
                    serverRetrofit = builder.build();
                }
            }
        }
        return serverRetrofit;
    }

    private Retrofit matrixRetrofit;

    /**
     * Used for user logged in.
     * Will set base url to the home server url
     *
     * @return instance of [Retrofit]
     */
    @Nullable
    public Retrofit getMatrixRetrofit() {
        return getMatrixRetrofit(null);
    }

    /**
     * When login, pass a home server url, every time will get a new instance.
     * If homeServerUrl is empty, will set base url to the home server url
     *
     * @param homeServerUrl not empty home server url for login use, and empty for user logged in
     * @return instance of [Retrofit]
     */
    @Nullable
    public Retrofit getMatrixRetrofit(@Nullable String homeServerUrl) {
        String baseUrl = homeServerUrl;
        if (TextUtils.isEmpty(homeServerUrl)) {
            Session session = activeSessionHolder.getSafeActiveSession();
            if (session == null) return null;
            baseUrl = session.getSessionParams().getHomeServerUrl();
        } else {
            return newMatrixRetrofit(baseUrl);
        }

        if (matrixRetrofit == null) {
            synchronized (NetWorkManager.class) {
                if (matrixRetrofit == null) {
                    matrixRetrofit = newMatrixRetrofit(baseUrl);
                }
            }
        }
        return matrixRetrofit;
    }

    public void destroyMatrixRetrofit() {
        matrixRetrofit = null;
    }

    private Retrofit newMatrixRetrofit(String baseUrl) {
        MyLoggingInterceptor httpLoggingInterceptor = new MyLoggingInterceptor("yql-request");
        httpLoggingInterceptor.setPrintLevel(BuildConfig.DEBUG ? MyLoggingInterceptor.Level.BODY
                : MyLoggingInterceptor.Level.NONE);
        httpLoggingInterceptor.setColorLevel(Level.INFO);

        OkHttpClient client = new OkHttpClient
                .Builder()
                .addInterceptor(new HeaderInterceptor(getRequestHeader()))
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        client.dispatcher().setMaxRequestsPerHost(8);
        return new Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void update() {
        retrofit = null;
        getRetrofit();
        serverRetrofit = null;
        getServerRetrofit();
        retrofitMoshi = null;
        getMoshiRetrofit();
    }

    private static class NetWorkManagerHolder {
        private static NetWorkManager instance = new NetWorkManager();
    }

}
