package org.yiqia

import org.matrix.android.sdk.api.pushrules.RuleIds
import org.matrix.android.sdk.api.pushrules.rest.PushRule

object PushRulesUtils {
    //移除不用的通知规则
    fun MutableList<PushRule>.disableUselessPushRules(): MutableList<PushRule> {
        val pushRuleOne2OneEncryptedRoom = this.find { it.ruleId == RuleIds.RULE_ID_ONE_TO_ONE_ENCRYPTED_ROOM }
        val pushRuleEncrypted = this.find { it.ruleId == RuleIds.RULE_ID_ENCRYPTED }
        val pushRuleRoomKeyword = this.find { it.ruleId == RuleIds.RULE_ID_KEYWORDS }
        val pushRuleRoomNotify = this.find { it.ruleId == RuleIds.RULE_ID_ROOM_NOTIF }
        val pushRuleBot= this.find { it.ruleId == RuleIds.RULE_ID_SUPPRESS_BOTS_NOTIFICATIONS }
        pushRuleOne2OneEncryptedRoom?.enabled = false
        pushRuleEncrypted?.enabled = false
        pushRuleRoomNotify?.enabled = false
        pushRuleRoomKeyword?.enabled = false
        pushRuleBot?.enabled = false
        return this
    }
}
