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

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import im.vector.app.R;


/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class ToastUtil {
    private static Toast errorToast;
    private static Toast errorTopToast;
    private static Toast successToast;

    /**
     * 全局吐司公共方法
     *
     * @param context Context
     * @param message toast message
     */
    @SuppressLint("ShowToast")
    public static void showError(Context context, CharSequence message) {
        if (TextUtils.isEmpty(message))
            return;
        if (errorToast != null) {
            errorToast.cancel();
        }
        errorToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        View toastView = View.inflate(context, R.layout.error_toast, null);
        errorToast.setView(toastView);
        errorToast.setGravity(Gravity.CENTER, 0, 0);
        TextView errorText = toastView.findViewById(R.id.tv_text_toast);
        errorText.setText(message);
        errorToast.show();
    }

    public static void showError(Context context, int strId) {
        showError(context, context.getResources().getString(strId));
    }

    @SuppressLint("ShowToast")
    public static void showSuccess(Context context, CharSequence message) {
        if (TextUtils.isEmpty(message))
            return;
        if (successToast != null) {
            successToast.cancel();
        }
        successToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        View toastView = View.inflate(context, R.layout.success_toast, null);
        successToast.setView(toastView);
        successToast.setGravity(Gravity.CENTER, 0, 0);
        TextView successText = toastView.findViewById(R.id.tv_text_toast);
        successText.setText(message);
        successToast.show();
    }

    public static void showSuccess(Context context, int strId) {
        showSuccess(context, context.getResources().getString(strId));
    }

    public static void showTopError(Context context, int strId) {
        showTopError(context, context.getString(strId));
    }

    @SuppressLint("ShowToast")
    public static void showTopError(Context context, CharSequence message) {
        if (TextUtils.isEmpty(message))
            return;
        if (errorTopToast != null) {
            errorTopToast.cancel();
        }
        errorTopToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        TextView errorTopText = (TextView) View.inflate(context, R.layout.error_top_toast, null);
        float hOffset = context.getResources().getDimension(R.dimen.toolbar_height);
        errorTopToast.setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL, 0, (int) hOffset);
        errorTopToast.setView(errorTopText);
        errorTopText.setText(message);
        errorTopToast.show();
    }

    public static void recreateToastView() {
        if (errorToast != null) {
            errorToast.cancel();
            errorToast = null;
        }

        if (errorTopToast != null) {
            errorTopToast.cancel();
            errorTopToast = null;
        }

        if (successToast != null) {
            successToast.cancel();
            successToast = null;
        }

    }
}
