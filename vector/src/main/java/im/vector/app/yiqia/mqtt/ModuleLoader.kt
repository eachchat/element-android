package im.vector.app.yiqia.mqtt

import android.app.Application
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import im.vector.app.eachchat.BaseModule
import im.vector.app.eachchat.net.NetConstant
import im.vector.app.yiqia.mqtt.data.IMParam
import org.eclipse.paho.android.service.MqttService
import timber.log.Timber

/**
 * Created by chengww on 11/25/20
 * @author chengww
 */
object ModuleLoader {
    var isMQTTConnected = false

    fun loadModule(appContext: Application) {

//        runCatching {
//            BaseModule.load()
//        }.exceptionOrNull()?.let { Timber.e("ModuleLoader" + it.stackTraceToString()) }

        val param = IMParam().also {
            it.serverURI = NetConstant.getMqttHostWithProtocol()
            it.userName = NetConstant.USER_NAME
            it.passWord = NetConstant.PASS_WORD
        }

        IMManager.getClient().init(appContext, param)
        IMManager.getClient().connect(null, object : IMCallback.ConnectCallback {
            override fun onSuccess() {
                runCatching {
                    isMQTTConnected = true
//                    BaseModule.setIsMQTTConnected(true)
                    appContext.startService(
                            Intent(appContext, MqttService::class.java)
                    )
                }.exceptionOrNull()?.let { Timber.e("ModuleLoader" + it.stackTraceToString()) }
                LogUtils.iTag("mqtt", "mqtt connect onSuccess")
            }

            override fun onError(errorCode: Int) {
//                BaseModule.setIsMQTTConnected(false)
                isMQTTConnected = false
                LogUtils.iTag("mqtt", "mqtt connect onError errorCode = $errorCode")
            }
        })
    }
}
