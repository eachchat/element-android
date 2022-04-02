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

package im.vector.app.yiqia.department.data;

import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import im.vector.app.yiqia.contact.data.User;

/**
 * Created by zhouguanjie on 2019/9/10.
 */
public class DepartmentUserBean extends User implements IDisplayBean {

    private String departmentName;

    private Intent intent;


    public DepartmentUserBean(User user) {
        setId(user.getId());
        setDepartmentId(user.getDepartmentId());
        setUserType(user.getUserType());
        setProfileUrl(user.getAvatarTUrl());
        setDisplayName(user.getDisplayName());
        setNickName(user.getNickName());
        setUserTitle(user.getUserTitle());
        setUserName(user.getUserName());
        setRemarkName(user.getRemarkName());
        setEmails(user.getEmails());
        setPhoneNumbers(user.getPhoneNumbers());
        setAvatarOUrl(user.getAvatarOUrl());
        setAvatarTUrl(user.getAvatarTUrl());
        setRemarkNamePy(user.getRemarkNamePy());
        setDisplayNamePy(user.getDisplayNamePy());
        setMatrixId(user.getMatrixId());
    }

    @Override
    public String getAvatar() {
        return getAvatarTUrl();
    }

    @Override
    public int getItemType() {
        return 1;
    }

    @Override
    public String getMinorContent() {
        return getUserTitle();
    }

    @Override
    public String getMainContent() {
        return getName();
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public Intent getExtra() {
        return intent;
    }

    @Override
    public String getDepartmentId() {
        if (!TextUtils.isEmpty(super.getDepartmentId())) {
            return super.getDepartmentId();
        }
        if (intent != null) return intent.getStringExtra("key_department_id");
        else return null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DepartmentUserBean)) {
            return false;
        }
        DepartmentUserBean userBean = (DepartmentUserBean) obj;
        return TextUtils.equals(userBean.getId(), getId());
    }
}
