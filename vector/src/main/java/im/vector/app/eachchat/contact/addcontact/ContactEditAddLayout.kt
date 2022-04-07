package im.vector.app.eachchat.contact.addcontact

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.adapters.ListenerUtil
import im.vector.app.R
import im.vector.app.eachchat.utils.string.StringUtils

/**
 * Created by chengww on 2020/11/4
 * @author chengww
 */
class ContactEditAddLayout(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attributeSet, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    var title: TextView
    private var tvRequired: TextView
    var etEdit: EditText
    var ivReduce: ImageView
    var ivArrow: ImageView
    var divider: View
    var tvError: TextView
    var layoutTitle: LinearLayout

    init {
        inflate(context, R.layout.view_contact_edit_add, this)
        title = findViewById(R.id.tv_title)
        tvRequired = findViewById(R.id.tv_required)
        etEdit = findViewById(R.id.et_edit)
        ivReduce = findViewById(R.id.iv_reduce)
        ivArrow = findViewById(R.id.iv_arrow)
        divider = findViewById(R.id.view_line)
        tvError = findViewById(R.id.tv_error)
        layoutTitle = findViewById(R.id.layout_title)
        etEdit.addTextChangedListener { editable ->
            if (editable == null) return@addTextChangedListener
            while (editable.length + StringUtils.calcChineseChar(editable) > MAX_LENGTH) {
                editable.delete(editable.length - 1, editable.length)
            }
        }
        attributeSet?.let {
            val a = context.obtainStyledAttributes(attributeSet, R.styleable.ContactEditAddLayout)
                .also { attrs ->

                    attrs.getText(R.styleable.ContactEditAddLayout_leftTitle)?.also {
                        title.text = it
                    }

                    attrs.getText(R.styleable.ContactEditAddLayout_editHint)?.also {
                        etEdit.hint = it
                    }
                }
            a.recycle()
        }
    }

    fun getText() = etEdit.text?.toString()

    fun setText(text: String?) {
        if (!TextUtils.equals(text, getText())) {
            etEdit.setText(text)
            etEdit.setSelection(etEdit.text.length)
        }
    }

    fun setOnTitleClickListener(listener: OnClickListener) {
        layoutTitle.setOnClickListener(listener)
    }

    private fun addTextWatch(textWatcher: TextWatcher) {
        etEdit.addTextChangedListener(textWatcher)
    }

    private fun removeTextWatch(textWatcher: TextWatcher?) {
        etEdit.removeTextChangedListener(textWatcher)
    }

    override fun setEnabled(enabled: Boolean) {
        etEdit.isEnabled = enabled
        super.setEnabled(enabled)
    }


    fun setFocusAble(focusable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            etEdit.focusable = if (focusable) FOCUSABLE else NOT_FOCUSABLE
        }
    }

    fun setReduceIcon(reduceIconClickListener: () -> Unit) {
        showReduceIcon()
        ivReduce.setOnClickListener {
//            etEdit.text.clear()
//            etEdit.onFocusChangeListener.onFocusChange(etEdit, false)
            reduceIconClickListener.invoke()
        }
        etEdit.addTextChangedListener {
            ivReduce.visibility = VISIBLE
        }
    }

    private fun showReduceIcon() {
        if (!etEdit.text.isNullOrBlank()) {
            ivReduce.visibility = VISIBLE
        } else {
            ivReduce.visibility = GONE
        }
    }

    fun setRequired(required: Boolean) {
        tvRequired.visibility = if (required) VISIBLE else INVISIBLE
//        if (required) {
//            title.setTextColor(ContextCompat.getColor(context, R.color.black))
//        } else {
//            title.setTextColor(ContextCompat.getColor(context, R.color.ff999999))
//        }
    }

    fun setViewLineVisible(lineVisible: Boolean) {
        divider.visibility = if (lineVisible) View.VISIBLE else GONE
    }


    fun setFocusChangeListener(listener: OnFocusChangeListener) {
        etEdit.onFocusChangeListener = listener
    }

    companion object {
        const val MAX_LENGTH = 70

        @JvmStatic
        @BindingAdapter("app:text")
        fun setText(layout: ContactEditAddLayout, text: String?) = layout.setText(text)

        @JvmStatic
        @InverseBindingAdapter(attribute = "app:text", event = "app:textAttrChanged")
        fun getText(layout: ContactEditAddLayout): String? = layout.getText()

        @JvmStatic
        @BindingAdapter(value = ["app:textAttrChanged"], requireAll = false)
        fun setListener(layout: ContactEditAddLayout, listener: InverseBindingListener?) {
            listener?.let {
                val newTextWatch: SimpleTextWatcher = object : SimpleTextWatcher {
                    override fun onTextChanged(
                        charSequence: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        it.onChange()
                    }
                }
                ListenerUtil.trackListener(layout, newTextWatch, R.id.textWatcher)?.let { watcher ->
                    layout.removeTextWatch(watcher)
                }
                layout.addTextWatch(newTextWatch)
            }
        }

        @JvmStatic
        @BindingAdapter("app:enabled")
        fun setEnabled(layout: ContactEditAddLayout, enabled: Boolean = true) {
            layout.isEnabled = enabled
        }

        @JvmStatic
        @BindingAdapter("lineVisible")
        fun setLineVisible(layout: ContactEditAddLayout, visible: Boolean) {
            layout.setViewLineVisible(visible)
        }

        @JvmStatic
        @BindingAdapter("required")
        fun required(layout: ContactEditAddLayout, required: Boolean) {
            layout.setRequired(required)
        }
    }
}

internal interface SimpleTextWatcher : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No-op
    }

    override fun afterTextChanged(s: Editable?) {
        // No-op
    }
}
