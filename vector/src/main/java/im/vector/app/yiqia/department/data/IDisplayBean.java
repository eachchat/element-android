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

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * 多实体类 抽象成一个展示接口
 * <p>
 * Created by zhouguanjie on 2019/9/10.
 */
public interface IDisplayBean extends MultiItemEntity {

    String getAvatar();

    String getMainContent();

    String getMinorContent();

    String getId();

    int getCount();

    int getType();

    Intent getExtra();
}
