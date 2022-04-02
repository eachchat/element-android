package im.vector.app.eachchat.contact.api.bean

import im.vector.app.eachchat.contact.data.ContactsDisplayBean

/**
 * Created by chengww on 2020/11/4
 * @author chengww
 */
data class ContactSettingsResult(var openOrg: Int = 0,
                                 var maxOtherHsMembersFirstTime: Int = 0,
                                 var totalMembersFirstTime: Int = 0,
                                 var totalMembersNextTime: Int = 0)

data class MatrixIdBean(var matrixId: String)

data class RemarkName(var matrixId: String, var remarkName: String)

data class ContactIncrementInput(
        var name: String,
        var updateTime: Long,
        var perPage: Int,
        var sequenceId: Int
)

data class ContactIncrementInputV2(
    var updateTimestamp: Long,
    var perPage: Int,
    var sequenceId: Int
)

data class ContactIncrementResultObject(var updateTime: Long = 0)

data class ContactIncrementBean(
        var contactId: String?, var contactMatrixId: String?, var contactType: String?,
        var contactRemarkName: String?, var contactEmail: String?, var contactMobile: String?,
        var contactTelephone: String?, var contactCompany: String?, var contactTitle: String?,
        var del: Int?, var valid: Int?
) {
    val contactDel: Boolean
        get() = del != null && del!! > 0

    fun toContact() =
            ContactsDisplayBean(
                    matrixId = contactMatrixId.orEmpty(),
                    remarkName = contactRemarkName,
                    email = contactEmail,
                    mobile = contactMobile,
                    telephone = contactTelephone,
                    company = contactCompany,
                    userTitle = contactTitle,
                    contactId = contactId.orEmpty(),
                    valid = this.valid ?: 0
            )
}

data class ContactRoomBean(val roomId: String)

data class CountMediaResult(val countMedia: Long?)

data class CountVideoResult(val countVideo: Long?)

data class CountAudioResult(val countAudio: Long?)
