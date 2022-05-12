package org.yiqia.push_rule

import org.matrix.android.sdk.api.pushrules.RuleIds
import org.matrix.android.sdk.api.pushrules.rest.PushRule

object PushRulesUtils {
    //停用不用的通知规则
    fun MutableList<PushRule>.disableUselessPushRules(): MutableList<PushRule> {
        val pushRuleOne2OneEncryptedRoom = this.find { it.ruleId == RuleIds.RULE_ID_ONE_TO_ONE_ENCRYPTED_ROOM }
        val pushRuleEncrypted = this.find { it.ruleId == RuleIds.RULE_ID_ENCRYPTED }
        val pushRuleKeyword = this.find { it.ruleId == RuleIds.RULE_ID_KEYWORDS }
        val pushRuleBot= this.find { it.ruleId == RuleIds.RULE_ID_SUPPRESS_BOTS_NOTIFICATIONS }
        val pushRuleRoomUpdate= this.find { it.ruleId == RuleIds.RULE_ID_TOMBSTONE } // 群聊升级通知
        pushRuleOne2OneEncryptedRoom?.enabled = false
        pushRuleEncrypted?.enabled = false
        pushRuleKeyword?.enabled = false
        pushRuleBot?.enabled = false
        pushRuleRoomUpdate?.enabled = false
        return this
    }
}
