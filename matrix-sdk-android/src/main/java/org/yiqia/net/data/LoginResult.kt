package org.yiqia.net.data

data class OrgSearchInput(val tenantName: String)

data class GMSResult(
    val defaultIdentity: Identity,
    val entry: Entry,
    val mqtt: Entry,
    val matrix: Matrix?,
    val im: IMConfig?,
    val favorite: FavoriteConfig?,
    val book: BookConfig?,
)

data class Identity(val identityType: String?)

data class Matrix(val homeServer: String)

data class IMConfig(val videoSwitch: Boolean, val audioSwitch: Boolean)

data class FavoriteConfig(val favSwitch: Boolean)

data class BookConfig(val contactSwitch: Boolean, val groupSwitch: Boolean, val orgSwitch: Boolean)

data class Entry(val host: String, val port: String, val tls: Boolean) {
    val cooperationUrl: String
        get() = "${if (tls) "https" else "http"}://$host:$port"
//    val mqttUrl: String
//        get() = "tcp://$host:$port"
}

data class AuthSettingResult(
    val authType: String,
    val threeAuthType: String?,
    val userNamePlaceHolder: String?,
    val passwordPlaceHolder: String?,
    val matrixEmail: String?,
    val threeEmail: String?,
    val initPasswordRegex: String?,
    val passwordChangeInfo: String?
)
