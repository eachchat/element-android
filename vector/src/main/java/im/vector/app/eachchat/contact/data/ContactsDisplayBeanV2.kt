package im.vector.app.eachchat.contact.data

import android.os.Parcelable
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

import org.json.JSONException


@Entity(
    tableName = "contactsV2",
    indices = [Index(value = ["nickName"]), Index(value = ["id"], unique = true)]
)
@Parcelize
@TypeConverters(ContactConverter::class)
class ContactsDisplayBeanV2(
    @PrimaryKey(autoGenerate = true)
    var IdInDatabase: Int = 0,//数据库中的id

    var id: String? = null,//联系人ID
    var userId: String? = null,//
    var photo: String? = null,//头像
    var photoUrl: String? = null,//头像url
    var photoType: String? = null,

    var matrixId: String? = null,//matrixId
    var family: String? = null,//姓氏
    var given: String? = null,//名字
    var firstName: String? = null,//姓氏拼音
    var middleName: String? = null,//中间名拼音
    var lastName: String? = null,//名字拼音
    var nickName: String? = null,//昵称拼音

    var organization: String? = null,//公司
    var department: String? = null,//部门
    var title: String? = null,//职位

    var telephoneList: MutableList<TelephoneBean>? = null,//电话号码
    @Ignore
    var editTelephoneList: EditPhoneList? = null,

    var emailList: MutableList<EmailBean>? = null,//电子邮件
    @Ignore
    var editEmailList: EditEmailList? = null,

    var addressList: MutableList<AddressBean>? = null,//地址
    @Ignore
    var editAddressList: EditAddressList? = null,

    var urlList: MutableList<UrlBean>? = null,//网站
    @Ignore
    var editUrlList: EditUrlList? = null,

    var imppList: MutableList<ImppBean>? = null,//聊天工具
    @Ignore
    var editImppList: EditImppList? = null,

    var dateList: MutableList<DateBean>? = null,//重要日期
    @Ignore
    var editDateList: EditDateList? = null,

    var relationship: String? = null,//关系

    var note: String? = null,//备注

    var categories: String? = null,//标签

    var updateTimestamp: Long? = null,//更新时间

    var del: Int = 0,//删除状态

    //没有用到的字段
    var fnEncoding: String? = null,
    var nEncoding: String? = null,
    var nCharset: String? = null,
    var orgEncoding: String? = null,
    var orgCharset: String? = null,
    var nkEncoding: String? = null,
    var nkCharset: String? = null,
    var titleEncoding: String? = null,
    var titleCharset: String? = null,
    var noteEncoding: String? = null,
    var noteCharset: String? = null,
    @Transient
    var contactAdded: Boolean = false,
    @Transient
    var fromOrg: Boolean = false,
    var valid: Int = 0,
    var lastSeenTs: Long = 0
) : Parcelable {
    //    internal fun String?.matrixHost() = this?.run {
//        val index = lastIndexOf(":")
//        val result = if (index > -1 && index + 1 < length) substring(index + 1) else this
//        result
//    } ?: this
//
//    fun matrixHostEquals(compareMatrixId: String?): Boolean {
//        if (compareMatrixId.isNullOrEmpty()) return false
//        val myUserId = BaseModule.getMatrixHolder().getSession()?.myUserId
//        if (myUserId.isNullOrEmpty()) return false
//        return myUserId.matrixHost().equals(compareMatrixId.matrixHost(), true)
//    }

    @IgnoredOnParcel @Transient
    var displayName: String? = null
        get() {
            if (!this.nickName.isNull()) {
                 return this.nickName
            } else if (!this.family.isNull() || !this.given.isNull()) {
                var family = this.family
                var given = this.given
                if (this.family.isNull()) family = ""
                if (this.given.isNull()) given = ""
                //format English name
//                if (CharUtils.isAllEnglish(family) && CharUtils.isAllEnglish(given)){
//                    return "$given $family"
//                }
                return family + given
            } else {
                return ""
            }
        }

    @IgnoredOnParcel @Transient
    var displayTitle: String? = null
        get() {
            var organization = this.organization
            var title = this.title
            if (this.organization.isNull()) organization = ""
            if (this.title.isNull()) title = ""
            if (organization == "") {
                return title
            } else {
                return "$organization $title"
            }
        }

    @Ignore
    fun toContactsDisplayBean() = ContactsDisplayBean().apply {
        avatar = this@ContactsDisplayBeanV2.photo
        avatarUrl = this@ContactsDisplayBeanV2.photoUrl
        remarkName = this@ContactsDisplayBeanV2.displayName
        matrixId = this@ContactsDisplayBeanV2.matrixId.toString()
        email = emailList?.find { it.type == EmailBean.WORK }?.value
        mobile = telephoneList?.find { it.type == TelephoneBean.CELL }?.value
        telephone = telephoneList?.find { it.type == TelephoneBean.WORK }?.value
        company = this@ContactsDisplayBeanV2.organization
        userTitle = this@ContactsDisplayBeanV2.title
        contactAdded = this@ContactsDisplayBeanV2.contactAdded
        fromOrg = this@ContactsDisplayBeanV2.fromOrg
        contactId = this@ContactsDisplayBeanV2.id.toString()
        valid = this@ContactsDisplayBeanV2.valid
        lastSeenTs = this@ContactsDisplayBeanV2.lastSeenTs
        del = this@ContactsDisplayBeanV2.del
    }
}

@Parcelize
class TelephoneBean(
    var id: Long? = null,
    var contactId: String? = "",
    var value: String? = "",
    var type: String? = ""
) :
    Parcelable {
    companion object {
        const val CELL = "CELL"
        const val WORK = "WORK"
        const val HOME = "HOME"
        const val OTHER = "OTHER"
    }
}

@Parcelize
class AddressBean(
    var id: Long? = null,
    var contactId: String? = null,
    var country: String? = null,
    var streetAddress: String? = null,
    var locality: String? = null,
    var region: String? = null,
    var subLocality: String? = null,
    var postalCode: String? = null,
    var type: String? = null
) :
    Parcelable {
    companion object {
        const val HOME = "HOME"
        const val WORK = "WORK"
        const val OTHER = "OTHER"
    }
}

@Parcelize
class EmailBean(
    var id: Long? = 0,
    var contactId: String? = "",
    var value: String? = "",
    var type: String? = ""
) :
    Parcelable {
    companion object {
        const val HOME = "HOME"
        const val WORK = "WORK"
        const val OTHER = "OTHER"
    }
}

@Parcelize
class DateBean(
    var id: Long? = null,
    var contactId: String? = null,
    var value: String? = null,
    var type: String? = null
) :
    Parcelable {
    companion object {
        const val BDAY = "BDAY"
        const val ANNIVERSARY = "ANNIVERSARY"
        const val OTHER = "OTHER"
    }
}

@Parcelize
class UrlBean(
    var id: Long? = null,
    var contactId: String? = null,
    var value: String? = null,
    var type: String? = null
) :
    Parcelable

@Parcelize
class ImppBean(
    var id: Long? = null,
    var contactId: String? = null,
    var value: String? = null,
    var type: String? = null
) :
    Parcelable {
    companion object {
        const val WHATSAPP = "Whatsapp"
        const val TEAMS = "teams"
        const val MESSENGER = "messenger"
        const val TELEGRAM = "telegram"
        const val FACEBOOK = "facebook"
        const val SKYPE = "skype"
        const val QQ = "qq"
        const val WECHAT = "wechat"
    }
}

@Parcelize
class EditPhoneList(
    var addList: MutableList<TelephoneBean>? = null,
    var updateList: MutableList<TelephoneBean>? = null,
    var delList: MutableList<TelephoneBean>? = null
) : Parcelable

@Parcelize
class EditEmailList(
    var addList: MutableList<EmailBean>? = null,
    var updateList: MutableList<EmailBean>? = null,
    var delList: MutableList<EmailBean>? = null
) : Parcelable

@Parcelize
class EditAddressList(
    var addList: MutableList<AddressBean>? = null,
    var updateList: MutableList<AddressBean>? = null,
    var delList: MutableList<AddressBean>? = null
) : Parcelable

@Parcelize
class EditDateList(
    var addList: MutableList<DateBean>? = null,
    var updateList: MutableList<DateBean>? = null,
    var delList: MutableList<DateBean>? = null
) : Parcelable

@Parcelize
class EditUrlList(
    var addList: MutableList<UrlBean>? = null,
    var updateList: MutableList<UrlBean>? = null,
    var delList: MutableList<UrlBean>? = null
) : Parcelable

@Parcelize
class EditImppList(
    var addList: MutableList<ImppBean>? = null,
    var updateList: MutableList<ImppBean>? = null,
    var delList: MutableList<ImppBean>? = null
) : Parcelable

//类型转换器
object ContactConverter {
    @TypeConverter
    fun revertTelephoneList(string: String?): MutableList<TelephoneBean>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<TelephoneBean>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertEmailList(string: String?): MutableList<EmailBean>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<EmailBean>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertAddressList(string: String?): MutableList<AddressBean>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<AddressBean>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertDateList(string: String?): MutableList<DateBean>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<DateBean>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertUrlBean(string: String?): MutableList<UrlBean>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<UrlBean>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertImppList(string: String?): MutableList<ImppBean>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<ImppBean>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertEditTelephoneList(string: String?): EditPhoneList? {
        try {
            return Gson().fromJson(string, object : TypeToken<EditPhoneList>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertEditEmailList(string: String?): EditEmailList? {
        try {
            return Gson().fromJson(string, object : TypeToken<EditEmailList>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertEditAddressList(string: String?): EditAddressList? {
        try {
            return Gson().fromJson(string, object : TypeToken<EditAddressList>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertEditDateList(string: String?): EditDateList? {
        try {
            return Gson().fromJson(string, object : TypeToken<EditDateList>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertEditUrlBean(string: String?): EditUrlList? {
        try {
            return Gson().fromJson(string, object : TypeToken<EditUrlList>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertEditImppList(string: String?): EditImppList? {
        try {
            return Gson().fromJson(string, object : TypeToken<EditImppList>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun converter(src: Any?): String {
        // 使用Gson方法把src转成json格式的string，便于我们用的解析
        return Gson().toJson(src)
    }
}



