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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.airbnb.mvrx.fragmentViewModel
import com.blankj.utilcode.util.UriUtils
import im.vector.app.R
import im.vector.app.core.dialogs.GalleryOrCameraDialogHelper
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.extensions.registerStartForActivityResult
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.core.resources.ColorProvider
import im.vector.app.databinding.FragmentContactManageBinding
import im.vector.app.eachchat.base.BaseModule
import im.vector.app.eachchat.utils.FileUtils
import im.vector.app.eachchat.utils.ToastUtil
import im.vector.app.eachchat.utils.permission.Func
import im.vector.app.eachchat.utils.permission.PermissionUtil
import im.vector.lib.multipicker.MultiPicker
import java.io.File
import javax.inject.Inject

class ContactManageFragment @Inject constructor(
        private val colorProvider: ColorProvider,
) : VectorBaseFragment<FragmentContactManageBinding>(), GalleryOrCameraDialogHelper.Listener {
    private val viewModel: ContactManageViewModel by fragmentViewModel()

    private var dirRequest = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        uri?.let {
            // call this to persist permission across decice reboots
            requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // do your stuff
            viewModel.exportVcard(uri)
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
                        viewModel.loadVCards(it.contentUri)
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
                    mActivity.showWaitingView(getString(R.string.please_wait))
                } else {
                    mActivity.hideWaitingView()
                }
            }
        }
        viewModel.keysEvents.observe(viewLifecycleOwner) {
            when (it) {
                is VcardEvents.ExportVcard -> {
                    if (it.success) {
                        ToastUtil.showSuccess(requireContext(), getString(R.string.export_success))
                    } else {
                        ToastUtil.showError(requireContext(), getString(R.string.export_fail))
                    }
                }
                is VcardEvents.ImportVcard ->
                    if (it.success) {
                        ToastUtil.showSuccess(requireContext(), getString(R.string.import_success))
                    } else {
                        ToastUtil.showError(requireContext(), getString(R.string.import_fail))
                    }
            }
        }
    }

    private fun initClickListener() {
        views.backLayout.onClick {
            requireActivity().onBackPressed()
        }
        views.llImportVcf.onClick {
            openFile()
        }
        views.llExportVcf.onClick {
            createFolderIntent()
        }
    }

    private fun openFile() {
        val requestObject = PermissionUtil.with(activity).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
                .onAllGranted(object : Func() {
                    override fun call() {
                        MultiPicker.get(MultiPicker.FILE).startWith(pickImageActivityResultLauncher)
                    }
                }).onAnyDenied(object : Func() {
                    override fun call() {
                        ToastUtil.showError(activity, R.string.permission_defined)
                    }
                }).ask(1)
        (activity as VectorBaseActivity<*>).setPermissionRequestObject(requestObject)
    }

    private fun createFolderIntent() {
        val requestObject = PermissionUtil.with(activity).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
                .onAllGranted(object : Func() {
                    override fun call() {
                        dirRequest.launch("contact.vcf")
                    }
                }).onAnyDenied(object : Func() {
                    override fun call() {
                        ToastUtil.showError(activity, R.string.permission_defined)
                    }
                }).ask(1)
        (activity as VectorBaseActivity<*>).setPermissionRequestObject(requestObject)
    }

    override fun onImageReady(uri: Uri?) {
    }
}
