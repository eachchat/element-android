package im.vector.app.eachchat.contact.addcontact

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import im.vector.app.R
import im.vector.app.eachchat.contact.data.AddressBean
import im.vector.app.eachchat.utils.ScreenUtils

/*
 *create by liuliceng on 2021/12/2
 * view to edit address
 */
class AddressEditLayout(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attributeSet, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    var tvCountry: ContactEditAddLayout
    private var tvRegion: ContactEditAddLayout
    private var tvLocality: ContactEditAddLayout
    private var tvSubLocality: ContactEditAddLayout
    private var tvStreet: ContactEditAddLayout
    private var tvPostal: ContactEditAddLayout
    var addressTextWatcher: ((AddressBean?) -> Unit)? = null
    var addressFocusChangeListener: ((AddressBean?) -> Unit)? = null

    init {
        inflate(context, R.layout.layout_address_edit, this)
        tvCountry = findViewById(R.id.tv_country)
        tvRegion = findViewById(R.id.tv_region)
        tvLocality = findViewById(R.id.tv_locality)
        tvSubLocality = findViewById(R.id.tv_subLocality)
        tvStreet = findViewById(R.id.tv_street)
        tvPostal = findViewById(R.id.tv_postal)

        (tvPostal.divider.layoutParams as ConstraintLayout.LayoutParams).setMargins(
            ScreenUtils.dip2px(
                context,
                100F
            ), 0, 0, 0
        )
        (tvRegion.divider.layoutParams as ConstraintLayout.LayoutParams).setMargins(
            ScreenUtils.dip2px(
                context,
                100F
            ), 0, 0, 0
        )
        (tvLocality.divider.layoutParams as ConstraintLayout.LayoutParams).setMargins(
            ScreenUtils.dip2px(
                context,
                100F
            ), 0, 0, 0
        )
        (tvSubLocality.divider.layoutParams as ConstraintLayout.LayoutParams).setMargins(
            ScreenUtils.dip2px(
                context,
                100F
            ), 0, 0, 0
        )
        (tvStreet.divider.layoutParams as ConstraintLayout.LayoutParams).setMargins(
            ScreenUtils.dip2px(
                context,
                100F
            ), 0, 0, 0
        )
        setHint()
    }

    fun initAddress(address: AddressBean) {
        tvCountry.setText(address.country)
        tvRegion.setText(address.region)
        tvLocality.setText(address.locality)
        tvSubLocality.setText(address.subLocality)
        tvStreet.setText(address.streetAddress)
        tvPostal.setText(address.postalCode)
    }

    fun setTitle(title: String? = "") {
        tvCountry.title.text = title
        tvCountry.ivArrow.visibility = VISIBLE
    }

    fun setTitleClickListener(listener: OnClickListener?) {
        listener?.let { tvCountry.title.setOnClickListener(listener) }
    }

    fun setDeleteIcon(listener: OnClickListener) {
        tvCountry.ivReduce.setOnClickListener(listener)
    }

    fun getAddress(): AddressBean? {
        val address = AddressBean()
        address.country = tvCountry.getText()
        address.region = tvRegion.getText()
        address.locality = tvLocality.getText()
        address.subLocality = tvSubLocality.getText()
        address.streetAddress = tvStreet.getText()
        address.postalCode = tvPostal.getText()
        address.type = tvCountry.title.text.toString()
        if ((address.country + address.region + address.locality + address.subLocality + address.streetAddress + address.postalCode).isEmpty()) {
            return null
        }
        return address
    }

    fun setHint() {
        tvCountry.etEdit.hint = context.getString(R.string.country_or_region)
        tvRegion.etEdit.hint = context.getString(R.string.province)
        tvLocality.etEdit.hint = context.getString(R.string.city)
        tvSubLocality.etEdit.hint = context.getString(R.string.district)
        tvStreet.etEdit.hint = context.getString(R.string.street)
        tvPostal.etEdit.hint = context.getString(R.string.mail_code)
    }

    fun setTextWatch() {
        val focusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) return@OnFocusChangeListener
            addressFocusChangeListener?.invoke(getAddress())
        }
        val textChangeListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                addressTextWatcher?.invoke(getAddress())
                tvCountry.ivReduce.isVisible = true
            }
        }
        tvCountry.etEdit.onFocusChangeListener = focusChangeListener
        tvRegion.etEdit.onFocusChangeListener = focusChangeListener
        tvLocality.etEdit.onFocusChangeListener = focusChangeListener
        tvSubLocality.etEdit.onFocusChangeListener = focusChangeListener
        tvStreet.etEdit.onFocusChangeListener = focusChangeListener
        tvPostal.etEdit.onFocusChangeListener = focusChangeListener
        tvCountry.etEdit.addTextChangedListener(textChangeListener)
        tvRegion.etEdit.addTextChangedListener(textChangeListener)
        tvLocality.etEdit.addTextChangedListener(textChangeListener)
        tvSubLocality.etEdit.addTextChangedListener(textChangeListener)
        tvStreet.etEdit.addTextChangedListener(textChangeListener)
        tvPostal.etEdit.addTextChangedListener(textChangeListener)
    }
}

