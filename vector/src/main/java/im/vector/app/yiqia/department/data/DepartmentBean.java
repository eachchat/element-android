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

import im.vector.app.yiqia.contact.api.BaseConstant;
import im.vector.app.yiqia.contact.api.bean.Department;

/**
 * Created by zhouguanjie on 2019/9/10.
 */
public class DepartmentBean extends Department implements IDisplayBean {

    private int count;

    private String departmentType;

    public DepartmentBean(Department department) {
        setId(department.getId());
        setParentId(department.getParentId());
        setParentName(department.getParentName());
        setDisplayName(department.getDisplayName());
        setDepartmentType(department.getDepartmentType());
    }

    @Override
    public String getDepartmentType() {
        return departmentType;
    }

    @Override
    public void setDepartmentType(String departmentType) {
        this.departmentType = departmentType;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    @Override
    public int getItemType() {
        return BaseConstant.DEPARTMENT_TYPE;
    }

    @Override
    public String getMainContent() {
        return getDisplayName();
    }

    @Override
    public String getMinorContent() {
        return null;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getType() {
        return BaseConstant.SEARCH_DEPARTMENT_TYPE;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public Intent getExtra() {
        return null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DepartmentBean)) {
            return false;
        }
        DepartmentBean departmentBean = (DepartmentBean) obj;
        return TextUtils.equals(departmentBean.getId(), getId());
    }
}
