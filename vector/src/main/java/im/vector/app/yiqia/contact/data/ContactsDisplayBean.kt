package im.vector.app.yiqia.contact.data

import android.os.Parcelable
import androidx.annotation.WorkerThread
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.github.promeg.pinyinhelper.Pinyin
import com.google.gson.annotations.SerializedName
import im.vector.app.eachchat.BaseModule
import im.vector.app.yiqia.contact.database.ContactDaoHelper
import kotlinx.parcelize.IgnoredOnParcel

import kotlinx.parcelize.Parcelize

import org.matrix.android.sdk.api.session.content.ContentUrlResolver
import java.util.regex.Pattern

/**
 * Created by chengww on 2020/10/27
 * @author chengww
 */
@Entity(tableName = "contacts")
@Parcelize
data class ContactsDisplayBean(
    @Transient
    var avatar: String? = null,
    var remarkName: String? = null,
    var matrixId: String,
    var email: String? = null,
    var mobile: String? = null,
    var telephone: String? = null,
    var company: String? = null,
    @SerializedName("title")
    var userTitle: String? = null,
    @Transient
    var contactAdded: Boolean = false,
    @Transient
    var fromOrg: Boolean = false,
    @Transient
    var displayName: String? = null,
    @PrimaryKey
    var contactId: String = "",
    var valid: Int = 0,
    var lastSeenTs: Long = 0,
    var del: Int = 0,
) : Parcelable {

    companion object {
        const val CONTACTS_DEPARTMENT_ID = "-0x10086"
    }


    @IgnoredOnParcel var avatarUrl: String? = null
        set(value) {
            field = value.resolveMxc()
        }

    @kotlinx.parcelize.IgnoredOnParcel
    @Transient
    var mainTitle: String? = null
        get() {
            return if (field.isNullOrEmpty()) {
                remarkName.emptyTake(displayName).emptyTake(userName)
            } else {
                field
            }
        }

    @kotlinx.parcelize.IgnoredOnParcel
    @Transient
    var subTitle: String? = null
        get() {
            return if (field.isNullOrEmpty()) {
                when {
                    fromOrg -> userTitle
//                    matrixHostEquals(matrixId) -> userTitle
                    else -> {
                        when {
                            company.isNullOrEmpty() && !userTitle.isNullOrEmpty() -> userTitle
                            !company.isNullOrEmpty() && userTitle.isNullOrEmpty() -> company
                            !company.isNullOrEmpty() && !userTitle.isNullOrEmpty() -> "$company $userTitle"
                            else -> matrixId
                        }
                    }
                }
            } else {
                field
            }
        }
    val userName: String?
        get() = matrixId.userName()

    @Ignore
    fun toUser() = User().apply {
        id = matrixId
        contactId = this@ContactsDisplayBean.contactId
        avatarTUrl = avatar
        avatarOUrl = avatar.resolveMxc()
        contactUrlAvatar = this@ContactsDisplayBean.avatarUrl
        contactBase64Avatar = this@ContactsDisplayBean.avatar
        matrixId = this@ContactsDisplayBean.matrixId
        del = this@ContactsDisplayBean.del
        email?.let {
            emails = listOf(Email().also {
                it.isPrimary = true
                it.value = email
            }).toMutableList()
        }
        val workPhone = telephone?.let {
            Phone().also {
                it.value = telephone
                it.type = "work"
            }
        }

        mobile?.let {
            val mobilePhone = Phone().also {
                it.value = mobile
            }
            phoneNumbers = if (workPhone == null)
                listOf(mobilePhone).toMutableList() else
                listOf(mobilePhone, workPhone).toMutableList()
        }

        fromOrg = false
        userTitle = subTitle
        displayName = mainTitle
        remarkName = this@ContactsDisplayBean.remarkName
        displayNamePy = namePy(mainTitle)
        departmentId = CONTACTS_DEPARTMENT_ID
    }

    @Ignore
    fun toContactsDisplayBeanV2() = ContactsDisplayBeanV2().apply {
        photoUrl = this@ContactsDisplayBean.avatar
        family = this@ContactsDisplayBean.displayName
//        given = this@ContactsDisplayBean.displayName?.substring(1)
        matrixId = this@ContactsDisplayBean.matrixId
        organization = this@ContactsDisplayBean.company
        title = this@ContactsDisplayBean.userTitle
        contactAdded = this@ContactsDisplayBean.contactAdded
        fromOrg = this@ContactsDisplayBean.fromOrg
        nickName = this@ContactsDisplayBean.displayName
        id = this@ContactsDisplayBean.contactId
        valid = this@ContactsDisplayBean.valid
        lastSeenTs = this@ContactsDisplayBean.lastSeenTs
        val mTelephoneList = ArrayList<TelephoneBean>()
        if (!mobile.isNullOrEmpty())
            mTelephoneList.add(TelephoneBean(type = TelephoneBean.CELL, value = mobile))
        if (!telephone.isNullOrEmpty())
            mTelephoneList.add(TelephoneBean(type = TelephoneBean.WORK, value = telephone))
        telephoneList = mTelephoneList
        if (!email.isNullOrEmpty())
            emailList = mutableListOf(EmailBean(type = EmailBean.WORK, value = email))
    }

    constructor() : this(null, null, "")

    fun namePy(name: String?) = getNamePy(name)

    val firstChar: Char
        get() = namePy(mainTitle)[0]
}

@WorkerThread
fun List<org.matrix.android.sdk.api.session.user.model.User>?.toContactList(
        local: ContactDaoHelper,
        afterBlock: ((contact: ContactsDisplayBean) -> Unit)? = null
): List<ContactsDisplayBean>? {
    if (this == null) return null
    val userList = ArrayList<ContactsDisplayBean>()
    this.forEach { user ->
        val contact = local.getContactByMatrixId(user.userId)?.also {
            it.contactAdded = true
            it.avatar = user.avatarUrl
        } ?: user.toContact(false)
        afterBlock?.invoke(contact)
        userList.add(contact)
    }
    return userList
}

fun org.matrix.android.sdk.api.session.user.model.User.toContact(contactAdded: Boolean = false) =
    ContactsDisplayBean(
        avatarUrl,
        displayName,
        userId,
        contactAdded = contactAdded,
        displayName = displayName
    )

fun User.toContact(company: String?, contactAdded: Boolean = false) =
    ContactsDisplayBean(
        avatarTUrl,
        displayName.emptyTake(userName),
        matrixId ?: "",
        userTitle = userTitle,
        contactAdded = contactAdded,
        fromOrg = true,
        displayName = displayName,
        company = company
    ).also {
        phoneNumbers?.forEach { phone ->
            if ("work".equals(phone.type, true)) {
                it.telephone = phone.value
            } else {
                it.mobile = phone.value
            }
        }

        emails?.forEach { email ->
            it.email = email.value
        }
    }

fun User.toContactV2(company: String?, department: String? = null, contactAdded: Boolean = false) =
    ContactsDisplayBeanV2(
        photo = avatarTUrl,
        family = displayName.emptyTake(userName),
        matrixId = matrixId ?: "",
        title = userTitle,
        contactAdded = contactAdded,
        fromOrg = true,
        nickName = displayName,
        organization = company,
        department = department
    ).also {
        phoneNumbers?.forEach { phone ->
            val telephoneBean = TelephoneBean()
            if ("work".equals(phone.type, true)) {
                telephoneBean.value = phone.value
                telephoneBean.type = TelephoneBean.WORK
            } else {
                telephoneBean.value = phone.value
                telephoneBean.type = TelephoneBean.HOME
            }
            if (it.telephoneList == null) {
                it.telephoneList = ArrayList()
            }
            it.telephoneList?.add(telephoneBean)
        }

        emails?.forEach { email ->
            val emailBean = EmailBean()
            emailBean.value = email.value
            emailBean.type = EmailBean.WORK
            if (it.emailList == null) {
                it.emailList = ArrayList()
            }
            it.emailList?.add(emailBean)
        }
    }

internal fun String?.emptyTake(newValue: String?) =
    this.takeIf { !it.isNullOrEmpty() && it != "null" } ?: newValue

internal fun String?.isNull() = (this.isNullOrEmpty() || this == "null")

fun String?.userName() = this?.run {
    var result = if (this.startsWith("@")) this.substring(1) else this
    val index = result.lastIndexOf(":")
    if (index > -1) result = result.substring(0, index)
    result
} ?: this

internal fun String?.matrixHost() = this?.run {
    val index = lastIndexOf(":")
    val result = if (index > -1 && index + 1 < length) substring(index + 1) else this
    result
} ?: this

fun matrixHostEquals(compareMatrixId: String?, myUserId: String?): Boolean {
    if (compareMatrixId.isNullOrEmpty()) return false
    if (myUserId.isNullOrEmpty()) return false
    return myUserId.matrixHost().equals(compareMatrixId.matrixHost(), true)
}

const val CONTACTS_DEPARTMENT_ID = "-0x10086"

fun getNamePy(name: String?): String {
    if (name.isNullOrBlank()) return "#"
    val py = Pinyin.toPinyin(name, "")
    return if (Pattern.matches("^[a-zA-Z][\\s\\S]*", py)) py
    else "#"
}

fun String?.resolveMxc() =
        when {
            isNullOrEmpty() -> this
            startsWith("mxc://") -> BaseModule.getSession().contentUrlResolver()
                    .resolveThumbnail(this, 250, 250, ContentUrlResolver.ThumbnailMethod.SCALE) ?: this
            else -> this
        }
