/*
 * Copyright 2019 New Vector Ltd
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

package im.vector.app.features.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.facebook.stetho.common.LogUtil
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.eachchat.push.PushHelper
import im.vector.app.features.popup.PopupAlertManager
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class VectorActivityLifecycleCallbacks(private val popupAlertManager: PopupAlertManager, private val activeSessionHolder: ActiveSessionHolder) : Application.ActivityLifecycleCallbacks {
    private var count = 0

    override fun onActivityPaused(activity: Activity) {
        count--
        operationPush(true, activeSessionHolder)
    }

    override fun onActivityResumed(activity: Activity) {
        count++
        operationPush(false, activeSessionHolder)
        popupAlertManager.onNewActivityDisplayed(activity)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    private fun operationPush(start: Boolean, activeSessionHolder: ActiveSessionHolder) {
        Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(object : Observer<Long?> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onError(e: Throwable) {}
                    override fun onNext(value: Long?) {}
                    override fun onComplete() {
                        try {
                            if (start && count == 0) {
                                LogUtil.i("## Push Application startPush")
                                PushHelper.getInstance().startPush(activeSessionHolder)
                            } else if (!start && count > 0) {
                                LogUtil.i("## Push Application stopPush")
                                PushHelper.getInstance().stopPush()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
    }
}
