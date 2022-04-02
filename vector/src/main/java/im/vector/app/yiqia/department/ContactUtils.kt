package im.vector.app.yiqia.department


import android.content.Context
import im.vector.app.R
import im.vector.app.yiqia.contact.api.bean.Department
import im.vector.app.yiqia.department.data.DepartmentBean
import im.vector.app.yiqia.department.data.IDisplayBean
import java.util.*

/**
 * Created by chengww on 1/12/21
 * @author chengww
 */
object ContactUtils {

    @JvmStatic
    fun getDepartmentIcon(department: IDisplayBean) =
        (department as? DepartmentBean)?.let {
            when (it.departmentType) {
                "company" -> R.mipmap.icon_contacts_company
                "root" -> R.drawable.ic_contacts_org
                "group_chat" -> R.drawable.ic_contacts_group_chat
                else -> R.mipmap.icon_contacts_department
            }
        } ?: R.mipmap.icon_contacts_department

    @JvmStatic
    fun generateGroupChat(): IDisplayBean =
            DepartmentBean(Department("group_chat", "group_chat")
                    .also { it.departmentType = "group_chat" })

    @JvmStatic
    fun convertEmailType(
        context: Context,
        type: String?,
        convertToString: Boolean = false
    ): String {
        if (convertToString) {
            when (type?.uppercase(Locale.ROOT)) {
                "HOME" -> return context.getString(R.string.home)
                "WORK" -> return context.getString(R.string.work)
                "OTHER" -> return context.getString(R.string.other)
            }
        }
//        else {
//            when (type) {
//                context.getString(R.string.home) -> "HOME"
//                context.getString(R.string.work) -> "WORK"
//                context.getString(R.string.other) -> "OTHER"
//            }
//        }
        if (type.isNullOrEmpty()) return context.getString(R.string.e_mail)
        return type
    }

    @JvmStatic
    fun convertAddressType(
        context: Context,
        type: String?,
        convertToString: Boolean = false
    ): String {
        if (convertToString) {
            when (type?.uppercase(Locale.ROOT)) {
                "HOME" -> return context.getString(R.string.home_phone)
                "WORK" -> return context.getString(R.string.work_phone)
                "OTHER" -> return context.getString(R.string.other)
            }
        } else {
            when (type) {
                context.getString(R.string.home_phone) -> return "HOME"
                context.getString(R.string.work) -> return "WORK"
                context.getString(R.string.other) -> return "OTHER"
            }
        }
        if (type.isNullOrEmpty()) return context.getString(R.string.address)
        return type
    }

    @JvmStatic
    fun convertTelephoneType(
        context: Context,
        type: String?,
        convertToString: Boolean = false
    ): String {
        if (convertToString) {
            when (type?.uppercase(Locale.ROOT)) {
                "HOME" -> return context.getString(R.string.home_phone)
                "WORK" -> return context.getString(R.string.work_phone)
                "CELL" -> return context.getString(R.string.cell_phone)
                "OTHER" -> return context.getString(R.string.other)
                "MOBILE" -> return  context.getString(R.string.cell_phone)
            }
        } else {
            when (type) {
                context.getString(R.string.home_phone) -> return "HOME"
                context.getString(R.string.work_phone) -> return "WORK"
                context.getString(R.string.cell_phone) -> return "CELL"
                context.getString(R.string.other) -> return "OTHER"
            }
        }
        if (type.isNullOrEmpty()) return context.getString(R.string.tele_phone)
        return type
    }

    @JvmStatic
    fun convertDateType(
        context: Context,
        type: String?,
        convertToString: Boolean = false
    ): String? {
        if (convertToString) {
            when (type?.uppercase(Locale.ROOT)) {
                "BDAY" -> return context.getString(R.string.birth_day)
                "ANNIVERSARY" -> return context.getString(R.string.anniversary)
                "OTHER" -> return context.getString(R.string.other)
            }
        }
//        else {
//            when (type) {
//                context.getString(R.string.birth_day) -> "BDAY"
//                context.getString(R.string.anniversary) -> "ANNIVERSARY"
//                context.getString(R.string.other) -> "OTHER"
//            }
//        }
        if (type.isNullOrEmpty()) return context.getString(R.string.key_date)
        return type
    }

    @JvmStatic
    fun convertImppType(
        context: Context,
        type: String?,
        convertToString: Boolean = false
    ): String {
        if (convertToString) {
            when (type) {
                "whatsapp" -> return context.getString(R.string.whatsapp)
                "teams" -> return context.getString(R.string.teams)
                "messenger" -> return context.getString(R.string.messenger)
                "facebook" -> return context.getString(R.string.facebook)
                "skype" -> return context.getString(R.string.skype)
                "qq" -> return context.getString(R.string.qq)
                "wechat" -> return context.getString(R.string.wechat)
                "" -> return context.getString(R.string.communication_tool)
                "telegram" -> return context.getString(R.string.telegram)
            }
        } else {
            when (type) {
                context.getString(R.string.telegram) -> return "telegram"
                context.getString(R.string.whatsapp) -> return "whatsapp"
                context.getString(R.string.teams) -> return "teams"
                context.getString(R.string.messenger) -> return "messenger"
                context.getString(R.string.facebook) -> return "facebook"
                context.getString(R.string.skype) -> return "skype"
                context.getString(R.string.qq) -> return "qq"
                context.getString(R.string.wechat) -> return "wechat"
                context.getString(R.string.communication_tool) -> return ""
            }
        }
        if (type.isNullOrEmpty()) return context.getString(R.string.communication_tool)
        return type
    }
}
