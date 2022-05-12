/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.eachchat.utils;

import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import im.vector.app.eachchat.utils.SPUtils;

/**
 * Created by zhouguanjie on 2020/1/16.
 */
public class AppCache {

    // cache_not_clear 的SP文件在退出登录时不清除
    private final static String SP_NAME_CACHE_NOT_CLEAR = "cache_not_clear";

    private final static String KEY_GET_PNS = "key_get_pns";
    private final static String KEY_BIND_DEVICE = "key_bind_device";
    private final static String KEY_REQUEST_BIND_TIME = "key_request_bind_time";
    private static final String KEY_VOICE_MODE = "key_voice_tel_mode";
    private static final String KEY_VOICE_TIME_LIMIT = "key_voice_time_limit";
    private static final String KEY_IMPORT_RECOVERY_KEY = "key_import_recovery_key";
    private static final String KEY_PUSH_ENABLE = "key_push_enable";
    private static final String KEY_AUTO_ACCEPT_INVITE_ENABLE = "key_auto_accept_invite_enable";
    private static final String KEY_IS_LOGIN_TO_HOME = "key_is_login_to_home";
    private static final String KEY_IME_OPTION = "key_ime_option";
    private static final String KEY_VIDEO_ENABLE = "key_video_enable";
    private static final String KEY_BACKUP_ENABLE = "key_backup_enable";
    private static final String KEY_IS_IN_ORG = "key_is_in_org";
    private static final String KEY_UPLOAD_FILE_LIMIT = "key_upload_file_limit";
    private static final String KEY_UPLOAD_COMPRESS_IMAGE_LIMIT = "key_upload_compress_image_limit";
    private static final String KEY_INVITE_ROOM_ID = "key_invite_room_id";
    private static final String KEY_DELETE_GROUP_IDS = "key_delete_group_ids";
    private static final String KEY_SEND_INPUT_NOTIFICATION = "key_send_input_notification";
    private static final String KEY_SHOW_EMOJI_KEYBOARD = "key_show_emoji_keyboard";
    private static final String KEY_CHAT_BUBBLE_MODE = "key_chat_bubble_mode";
    private static final String KEY_IS_SHOW_PRIVACY_POLICY = "key_is_privacy_policy_show";
    private static final String KEY_IS_OPEN_GROUP = "key_is_open_group";
    private static final String KEY_IS_OPEN_CONTACT = "key_is_open_contact";
    private static final String KEY_IS_OPEN_ORG = "key_is_open_org";
    private static final String KEY_TENANT_NAME = "key_tenant_name";
    private static final String KEY_IS_OPEN_VOICE_CALL = "key_is_open_voice_call";
    private static final String KEY_IS_OPEN_VIDEO_CALL = "key_is_open_video_call";
    private static final String KEY_IS_LDAP = "key_is_ldap";
    private static final String KEY_PASSWORD_CHANGE_INFO = "key_password_change_info";
    private static final String KEY_AUDIO_LIMIT = "key_audio_limit";
    private static final String KEY_VIDEO_LIMIT = "key_video_limit";
    private static final String KEY_UPLOAD_LIMIT = "key_upload_limit";

    public static boolean isChatBubbleMode() {
        return SPUtils.get(KEY_CHAT_BUBBLE_MODE, true);
    }

    public static void setChatBubbleMode(boolean isChatBubble) {
        SPUtils.put(KEY_CHAT_BUBBLE_MODE, isChatBubble);
    }

    public static boolean isShowEmojiKeyboard() {
        return SPUtils.get(KEY_SHOW_EMOJI_KEYBOARD, true);
    }

    public static void setShowEmojiKeyboard(boolean isShow) {
        SPUtils.put(KEY_SHOW_EMOJI_KEYBOARD, isShow);
    }

    public static boolean isSendInputNotification() {
        return SPUtils.get(KEY_SEND_INPUT_NOTIFICATION, true);
    }

    public static void setSendInputNotification(boolean isSending) {
        SPUtils.put(KEY_SEND_INPUT_NOTIFICATION, isSending);
    }

    public static boolean isInOrg() {
        return SPUtils.get(KEY_IS_IN_ORG, true);
    }

    public static void setInOrg(boolean isInOrg) {
        SPUtils.put(KEY_IS_IN_ORG, isInOrg);
    }

    public static float getUploadFileLimit() {
        return SPUtils.get(KEY_UPLOAD_FILE_LIMIT, 0f);
    }

    public static void setUploadFileLimit(float fileLimit) {
        SPUtils.put(KEY_UPLOAD_FILE_LIMIT, fileLimit);
    }

    public static float getUploadCompressImageLimit() {
        return SPUtils.get(KEY_UPLOAD_COMPRESS_IMAGE_LIMIT, 0f);
    }

    public static void setUploadCompressImageLimit(float compressFileLimit) {
        SPUtils.put(KEY_UPLOAD_COMPRESS_IMAGE_LIMIT, compressFileLimit);
    }

    public static String getDeleteGroupIds() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getString(KEY_DELETE_GROUP_IDS);
    }

    public static void putDeleteGroupIds(@Nullable String deleteGroupIds) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_DELETE_GROUP_IDS, deleteGroupIds);
    }

    public static String getInviteRoomId() {
        return SPUtils.get(KEY_INVITE_ROOM_ID, "");
    }

    public static void putInviteRoomId(String inviteRoomId) {
        SPUtils.put(KEY_INVITE_ROOM_ID, inviteRoomId);
    }

    public static boolean getVideoEnable() {
        return SPUtils.get(KEY_VIDEO_ENABLE, false);
    }

    public static void setVideoEnable(boolean enable) {
        SPUtils.put(KEY_VIDEO_ENABLE, enable);
    }

    public static boolean getBackupEnable() {
        return SPUtils.get(KEY_BACKUP_ENABLE, false);
    }

    public static void setBackupEnable(boolean enable) {
        SPUtils.put(KEY_BACKUP_ENABLE, enable);
    }

    public static int getImeOption() {
        return SPUtils.get(KEY_IME_OPTION, EditorInfo.IME_ACTION_UNSPECIFIED);
    }

    public static void setImeOption(int imeOption) {
        SPUtils.put(KEY_IME_OPTION, imeOption);
    }

    public static String getPNS() {
        return SPUtils.get(KEY_GET_PNS, "jiguang");
    }

    public static void setPns(String pns) {
        SPUtils.put(KEY_GET_PNS, pns);
    }

    public static boolean getPushEnable() {
        return SPUtils.get(KEY_PUSH_ENABLE, true);
    }

    public static void setPushEnable(boolean isEnable) {
        SPUtils.put(KEY_PUSH_ENABLE, isEnable);
    }

    public static boolean getAutoAcceptInviteEnable() {
        return SPUtils.get(KEY_AUTO_ACCEPT_INVITE_ENABLE, false);
    }

    public static void setAutoAcceptInviteEnable(boolean isEnable) {
        SPUtils.put(KEY_AUTO_ACCEPT_INVITE_ENABLE, isEnable);
    }

    public static boolean hasBindDevice() {
        return SPUtils.get(KEY_BIND_DEVICE, false);
    }

    public static void setBindDevice(boolean isBind) {
        SPUtils.put(KEY_BIND_DEVICE, isBind);
    }


    public static void setRequstPNSTime(long time) {
        SPUtils.put(KEY_REQUEST_BIND_TIME, time);
    }

    public static long getRequestPNSTime() {
        return SPUtils.get(KEY_REQUEST_BIND_TIME, 0L);
    }

    public static void setSpeakerMode(boolean b) {
        SPUtils.put(KEY_VOICE_MODE, b);
    }

    public static boolean getSpeakerMode() {
        return SPUtils.get(KEY_VOICE_MODE, false);
    }

    public static void setVoiceLengthLimit(long lengthLimit) {
        SPUtils.put(KEY_VOICE_TIME_LIMIT, lengthLimit);
    }

    public static long getVoiceLengthLimit() {
        return SPUtils.get(KEY_VOICE_TIME_LIMIT, 60);
    }

    public static void saveImportRecoveryKey(boolean saved) {
        SPUtils.put(KEY_IMPORT_RECOVERY_KEY, saved);
    }

    public static boolean hasImportRecoveryKey() {
        return SPUtils.get(KEY_IMPORT_RECOVERY_KEY, false);
    }

    public static void saveLoginHomeStatue(boolean saved) {
        SPUtils.put(KEY_IS_LOGIN_TO_HOME, saved);
    }

    public static boolean isLoginToHome() {
        return SPUtils.get(KEY_IS_LOGIN_TO_HOME, true);
    }

    public static boolean isShowPrivacyPolicy() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getBoolean(KEY_IS_SHOW_PRIVACY_POLICY, true);
    }

    public static void setShowPrivacyPolicy(boolean isShow) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_IS_SHOW_PRIVACY_POLICY, isShow);
    }

    public static boolean getIsOpenGroup() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getBoolean(KEY_IS_OPEN_GROUP, true);
    }

    public static void setIsOpenGroup(boolean isOpen) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_IS_OPEN_GROUP, isOpen);
    }

    public static boolean getIsOpenContact() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getBoolean(KEY_IS_OPEN_CONTACT, true);
    }

    public static void setIsOpenContact(boolean isOpen) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_IS_OPEN_CONTACT, isOpen);
    }

    public static boolean getIsOpenOrg() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getBoolean(KEY_IS_OPEN_ORG, true);
    }

    public static void setIsOpenOrg(boolean isOpen) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_IS_OPEN_ORG, isOpen);
    }

    public static void setTenantName(String tenantName) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_TENANT_NAME, tenantName);
    }

    public static String getTenantName() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getString(KEY_TENANT_NAME);
    }

    public static boolean getIsOpenVoiceCall() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getBoolean(KEY_IS_OPEN_VOICE_CALL, true);
    }

    public static void setIsOpenVoiceCall(boolean isOpen) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_IS_OPEN_VOICE_CALL, isOpen);
    }

    public static boolean getIsOpenVideoCall() {
        return com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).getBoolean(KEY_IS_OPEN_VIDEO_CALL, true);
    }

    public static void setIsOpenVideoCall(boolean isOpen) {
        com.blankj.utilcode.util.SPUtils.getInstance(SP_NAME_CACHE_NOT_CLEAR).put(KEY_IS_OPEN_VIDEO_CALL, isOpen);
    }

    public static boolean isLdap() {
        return SPUtils.get(KEY_IS_LDAP, false);
    }

    public static void setIsLdap(boolean isLdap) {
        SPUtils.put(KEY_IS_LDAP, isLdap);
    }

    public static String getPasswordChangeInfo() {
        return SPUtils.get(KEY_PASSWORD_CHANGE_INFO, "");
    }

    public static void setPasswordChangeInfo(String passwordChangeInfo) {
        SPUtils.put(KEY_PASSWORD_CHANGE_INFO, passwordChangeInfo);
    }

    public static long getVideoLimit() {
        return SPUtils.get(KEY_VIDEO_LIMIT, 0);
    }

    public static void setVideoLimit(long videoLimit) {
        SPUtils.put(KEY_VIDEO_LIMIT, videoLimit);
    }

    public static long getAudioLimit() {
        return SPUtils.get(KEY_AUDIO_LIMIT, 0);
    }

    public static void setAudioLimit(long audioLimit) {
        SPUtils.put(KEY_AUDIO_LIMIT, audioLimit);
    }

    public static long getUploadLimit() {
        return SPUtils.get(KEY_UPLOAD_LIMIT, 0);
    }

    public static void setUploadLimit(long uploadLimit) {
        SPUtils.put(KEY_UPLOAD_LIMIT, uploadLimit);
    }
}
