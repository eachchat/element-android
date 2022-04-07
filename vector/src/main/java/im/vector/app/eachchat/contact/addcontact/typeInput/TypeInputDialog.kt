package ai.workly.eachchat.android.contact.edit.typeInput

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import im.vector.app.R
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


/**
 *create by liuliceng at 2021/11/8
 * 输入类型的dialogFragment
 */
class TypeInputDialog(
        private var types: List<String>?,
        private var defaultType: String? = null,
        private var typeInputListener: ((String) -> Unit)? = null
) :
        DialogFragment() {

    lateinit var manager: LinearLayoutManager
    private var selectedView: View? = null
    private var mPosition: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.BottomDialog).apply {
            setContentView(R.layout.dialog_input_type)
            setCanceledOnTouchOutside(true)
            window?.setBackgroundDrawableResource(R.drawable.bg_round)
        }
        dialog.window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        initView(dialog)
        return dialog
    }

    @SuppressLint("ResourceAsColor")
    fun initView(dialog: Dialog) {
        val tvConfirm = dialog.findViewById<TextView>(R.id.tv_confirm)
        val tvCancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        val etCustomType = dialog.findViewById<EditText>(R.id.et_custom_type)
        val typeList = dialog.findViewById<ListView>(R.id.type_list)

        //only chars and numbers can be committed to Impp type
        if (defaultType == getString(R.string.communication_tool)) {
            etCustomType.doAfterTextChanged {
                val editable: String = etCustomType.text.toString()
                val str: String? = stringFilter(editable)
                if (editable != str) {
                    etCustomType.setText(str)
                    //设置新的光标所在位置
                    if (str != null) {
                        etCustomType.setSelection(str.length)
                    }
                }
            }
        }

        tvConfirm.setOnClickListener {
            if (selectedView is EditText) {
                typeInputListener?.invoke(etCustomType.text.toString())
                dismiss()
            } else {
                selectedView?.let { view ->
                    val tvType = view.findViewById<TextView>(R.id.tv_type)
                    if (mPosition == 0) {
                        defaultType?.let { typeInputListener?.invoke(it) }
                    } else {
                        typeInputListener?.invoke(tvType.text.toString())
                    }
                    dismiss()
                }
            }
        }

        etCustomType.setHint(R.string.custom)

        etCustomType.addTextChangedListener {
            if (etCustomType.text.isNullOrBlank()) {
                tvConfirm.setTextColor(R.color.ff999999)
                tvConfirm.isEnabled = false
            } else {
                tvConfirm.setTextColor(R.color.ff5b6a92)
                tvConfirm.isEnabled = true
            }
        }

        etCustomType.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etCustomType.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.bb_grey_color))
                selectedView = etCustomType
            }
        }

        tvCancel.setOnClickListener {
            dismiss()
        }

        val adapter = ArrayAdapter<String>(requireContext(), R.layout.type_item, R.id.tv_type)
        types?.let { adapter.addAll(it) }
        typeList.adapter = adapter
        typeList.setOnItemClickListener { _, view, position, _ ->
            selectedView?.let {
                val ivLastTypeSelected = it.findViewById<ImageView>(R.id.iv_type_selected)
                ivLastTypeSelected?.visibility = View.INVISIBLE
            }
            selectedView = view
            val ivTypeSelected = view.findViewById<ImageView>(R.id.iv_type_selected)
            ivTypeSelected?.visibility = View.VISIBLE
            mPosition = position
//            if (position == 0) {
//                typeInputListener?.invoke("")
//                dismiss()
//            } else {
//                types?.get(position)?.let { typeInputListener?.invoke(it) }
//                dismiss()
//            }
        }
    }

    override fun onResume() {
        val typeList = dialog?.findViewById<ListView>(R.id.type_list)
        if (typeList != null) {
            for (view in typeList.children) {
                val selectView = view.findViewById<ImageView>(R.id.iv_type_selected)
                selectView?.let { it.visibility = View.INVISIBLE }
            }
        }
        super.onResume()
    }

    @Throws(PatternSyntaxException::class)
    fun stringFilter(str: String?): String? {
        // 只允许字母、数字
        val regEx = "[^a-zA-Z0-9 ]"
        val p: Pattern = Pattern.compile(regEx)
        str?.let {
            val m: Matcher = p.matcher(str)
            return m.replaceAll("").trim()
        }
        return null
    }
}




