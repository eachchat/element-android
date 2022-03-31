package im.vector.app.yiqia.contact.data

data class EnterpriseSettingEntity(
    var name: String?,
    var logoUrl: String?,
    var address: String?,
    var website: String?,
    var inOrg: Boolean,
    var uploadUserLimit: Float, // 文件上传大小限制
    var uploadUserCompressLimit: Float, // 图片上传像素大小限制
    var statusDescription: List<String?>?,
    var updateTime: Long
)
