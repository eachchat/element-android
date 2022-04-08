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

package im.vector.app.eachchat.contact.manage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.viewModel
import im.vector.app.R
import im.vector.app.core.dialogs.GalleryOrCameraDialogHelper
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.extensions.registerStartForActivityResult
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.core.resources.ColorProvider
import im.vector.app.databinding.FragmentContactManageBinding
import im.vector.app.eachchat.utils.FileUtils
import im.vector.lib.multipicker.MultiPicker
import java.io.File
import javax.inject.Inject

class ContactManageFragment @Inject constructor(
        private val colorProvider: ColorProvider,
) : VectorBaseFragment<FragmentContactManageBinding>(), GalleryOrCameraDialogHelper.Listener {
    private val viewModel: ContactManageViewModel by fragmentViewModel()

    private val dirRequest = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            // call this to persist permission across decice reboots
            requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // do your stuff
            viewModel.exportVcard(FileUtils.getFolderUri(requireContext(), uri))
        }
    }

    private val galleryOrCameraDialogHelper = GalleryOrCameraDialogHelper(this, colorProvider)

    private val pickImageActivityResultLauncher = this.registerStartForActivityResult { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            MultiPicker
                    .get(MultiPicker.FILE)
                    .getSelectedFiles(requireContext(), activityResult.data)
                    .firstOrNull()
                    ?.let {
                        viewModel.loadVCards(File(FileUtils.getPath(requireContext(), it.contentUri)))
                    }
        }
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentContactManageBinding {
        return FragmentContactManageBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
        initObserver()
    }

    private fun initObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            val mActivity = requireActivity()
            if (mActivity is VectorBaseActivity<*>) {
                if (it) {
                    mActivity.showWaitingView(getKeyEvent(viewModel.keysEvents.value))
                } else {
                    mActivity.hideWaitingView()
                }
            }
        }
    }

    private fun getKeyEvent(event: VcardEvents?): String?{
        when(event) {
            is VcardEvents.ExportVcard -> return getString(R.string.exporting)
            is VcardEvents.ImportVcard -> return getString(R.string.importing)
        }
        return null
    }

    private fun initClickListener() {
        views.backLayout.onClick {
            requireActivity().onBackPressed()
        }
        views.tvImportVcf.onClick {
            MultiPicker.get(MultiPicker.FILE).startWith(pickImageActivityResultLauncher)
        }
        views.tvExportVcf.onClick {
            createFolderIntent()
        }
    }

    fun createFolderIntent(){
        dirRequest.launch(null)
    }

    override fun onImageReady(uri: Uri?) {
    }
}
