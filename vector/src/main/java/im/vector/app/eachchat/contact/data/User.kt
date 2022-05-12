package im.vector.app.eachchat.contact.data

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.widget.ImageView
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bumptech.glide.request.RequestOptions
import com.github.promeg.pinyinhelper.Pinyin
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import im.vector.app.R
import im.vector.app.core.glide.GlideApp
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import java.util.Locale

/**
 * Created by zhouguanjie on 2019/8/15.
 */
@Entity(tableName = "UserInfoStore",
       indices = [Index(value = ["id"], unique = true)]
)
@Parcelize
@TypeConverters(UserInfoConverter::class)
open class User(@PrimaryKey(autoGenerate = true)
           var primaryKey: Int = 0,//数据库中的id
                var id: String? = null,
                var contactId: String? = null,
                open var departmentId: String? = null,
                var userName: String? = null,
                var displayName: String? = null,
                var nickName: String? = null,
                var profileUrl: String? = null,
                var userType: String? = null,
                @SerializedName("title") var userTitle: String? = null,
                @SerializedName("preferredLanguage") var userPreferredLanguage: String? = null,
                @SerializedName("locale") var userLocale: String? = null,
                @SerializedName("timezone") var userTimezone: String? = null,
                var displayNamePy: String? = null,
                var remarkName: String? = null,
                var remarkNamePy: String? = null,
                var avatarOUrl: String? = null,
                var avatarTUrl: String? = null,
                var isActive: Boolean = false,
                var emails: MutableList<Email>? = null,
                var addresses: MutableList<Address>? = null,
                var ims: MutableList<IMS>? = null,
                var phoneNumbers: MutableList<Phone>? = null,
                var workDescription: String? = null,
                var statusDescription: String? = null,
                var managerId: String? = null,
                var del: Int = 0,
                var matrixId: String? = null,
                open var contactUrlAvatar: String? = null,
                open var contactBase64Avatar: String? = null,
                var searchType: Int = 0) : Parcelable, Comparable<User> {

    protected constructor(`in`: Parcel) : this() {
        id = `in`.readString()
        departmentId = `in`.readString()
        userName = `in`.readString()
        displayName = `in`.readString()
        nickName = `in`.readString()
        profileUrl = `in`.readString()
        userType = `in`.readString()
        userTitle = `in`.readString()
        userPreferredLanguage = `in`.readString()
        userLocale = `in`.readString()
        userTimezone = `in`.readString()
        displayNamePy = `in`.readString()
        remarkName = `in`.readString()
        remarkNamePy = `in`.readString()
        avatarOUrl = `in`.readString()
        avatarTUrl = `in`.readString()
        isActive = `in`.readByte().toInt() != 0
        emails = `in`.createTypedArrayList(Email.CREATOR)
        addresses = `in`.createTypedArrayList(Address.CREATOR)
        ims = `in`.createTypedArrayList(IMS.CREATOR)
        phoneNumbers = `in`.createTypedArrayList(Phone.CREATOR)
        workDescription = `in`.readString()
        statusDescription = `in`.readString()
        managerId = `in`.readString()
        del = `in`.readInt()
        matrixId = `in`.readString()
    }

    @IgnoredOnParcel var roomId: String? = null

    val name: String?
        get() {
            if (!TextUtils.isEmpty(displayName)) {
                return displayName
            }
            return if (!TextUtils.isEmpty(nickName)) {
                nickName
            } else userName
        }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is User) {
            return false
        }
        return TextUtils.equals(id, other.id)
    }

    override fun describeContents(): Int {
        return 0
    }

    val pinyin: String?
        get() {
            if (!TextUtils.isEmpty(remarkNamePy)) {
                return remarkNamePy
            }
            if (!TextUtils.isEmpty(displayNamePy)) {
                return displayNamePy
            }
            return if (!TextUtils.isEmpty(name)) {
                Pinyin.toPinyin(name!![0])
            } else "#"
        }

    override fun compareTo(other: User): Int {
        val py = pinyin
        val otherPy = other.pinyin
        if (valid(py) && !valid(otherPy)) {
            return -1
        }
        return if (!valid(py) && valid(otherPy)) {
            1
        } else py!!.uppercase(Locale.ROOT).compareTo(otherPy!!.uppercase(Locale.ROOT))
    }

    private fun valid(py: String?): Boolean {
        return !TextUtils.isEmpty(py) && py!!.matches(Regex("^[a-zA-Z][\\s\\S]*"))
    }

    val firstChar: Char
        get() {
            var result = '#'
            val pinyin = pinyin
            if (!TextUtils.isEmpty(pinyin)) {
                result = pinyin!!.toCharArray()[0]
            }
            if (result in 'a'..'z') {
                result = (result.code - 32).toChar()
            }
            return result
        }

    companion object {
        val CREATOR: Parcelable.Creator<User?> = object : Parcelable.Creator<User?> {
            override fun createFromParcel(`in`: Parcel): User {
                return User(`in`)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }

        fun loadAvatar(context: Context?, url: String?, imageView: ImageView?) {
//        GlideRequest<Drawable> request = GlideApp.with(context)
//                .load(url)
//                .transform(isCircle ? new CircleTransformation(context) : new RoundedCorners(8))
//                .error(R.mipmap.default_person_icon)
//                .placeholder(R.mipmap.default_person_icon);
//        if (cacheFile != null && cacheFile.exists()) {
//            request = request.thumbnail(GlideApp.with(context).load(cacheFile)
//                    .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE));
//        }
//        request.into(imageView);
            if (imageView != null && context != null) {
                GlideApp.with(context)
                        .load(url.resolveMxc())
                        .apply(RequestOptions.circleCropTransform())
                        .error(R.drawable.default_person_icon)
                        .into(imageView)
            }
        }

        fun loadAvatarBlur(context: Context?, url: String?, imageView: ImageView?) {
//        GlideRequest<Drawable> request = GlideApp.with(context)
//                .load(url)
//                .apply(RequestOptions.bitmapTransform(new BlurTransformation(100)));
//        if (cacheFile != null && cacheFile.exists()) {
//            request = request.thumbnail(GlideApp.with(context).load(cacheFile)
//                    .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(100))));
//        }
//        request.into(imageView);
            if (imageView != null && context != null) {
                    GlideApp.with(context)
                            .load(url)
                            .skipMemoryCache(true)
                            .apply(RequestOptions.bitmapTransform(BlurTransformation(100)))
                            .error(R.drawable.default_person_icon)
                            .into(imageView)
                }
            }
        }
    }


//类型转换器
object UserInfoConverter {
    @TypeConverter
    fun revertEmailList(string: String?): MutableList<Email>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<Email>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertImsList(string: String?): MutableList<IMS>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<IMS>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertAddressList(string: String?): MutableList<Address>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<Address>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun revertPhoneList(string: String?): MutableList<Phone>? {
        try {
            return Gson().fromJson(string, object : TypeToken<ArrayList<Phone>>() {}.type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @TypeConverter
    fun converter(src: Any?): String? {
        // 使用Gson方法把src转成json格式的string
        return Gson().toJson(src)
    }
}
