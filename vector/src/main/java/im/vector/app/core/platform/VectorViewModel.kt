/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.core.platform

import androidx.lifecycle.MutableLiveData
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import im.vector.app.core.utils.DataSource
import im.vector.app.core.utils.PublishDataSource

abstract class VectorViewModel<S : MavericksState, VA : VectorViewModelAction, VE : VectorViewEvents>(initialState: S) :
        MavericksViewModel<S>(initialState) {

    // Used to post transient events to the View
    protected val _viewEvents = PublishDataSource<VE>()
    val viewEvents: DataSource<VE> = _viewEvents
    val loading = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<Throwable>()

    abstract fun handle(action: VA)
}
