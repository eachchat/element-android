package im.vector.app.eachchat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import im.vector.app.R;
import im.vector.app.eachchat.utils.ScreenUtils;


/**
 * Created by chengww on 2019-10-17.
 */
public class AlertDialog {
    private Context context;
    private Dialog dialog;
    private LinearLayout lLayout_bg;
    private TextView txt_title;
    private TextView txt_msg;
    private TextView btn_neg;
    private TextView btn_pos;
    private View divPosNeg;
    private View divider;
    private View buttonLayout;
    private Display display;
    private boolean showTitle = false;
    private boolean showMsg = false;
    private boolean showPosBtn = false;
    private boolean showNegBtn = false;
    private boolean customPosBtn = false;

    private boolean notShowHint = false;
    private FrameLayout customLayout;

    public AlertDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
        }
    }

    public AlertDialog builder() {
        // 获取Dialog布局
        View view = View.inflate(context, R.layout.view_alertdialog, null);
        // 获取自定义Dialog布局中的控件
        lLayout_bg = view.findViewById(R.id.lLayout_bg);
        txt_title = view.findViewById(R.id.txt_title);
        txt_title.setVisibility(View.GONE);
        txt_msg = view.findViewById(R.id.txt_msg);
        txt_msg.setVisibility(View.GONE);
        //防止内容过长，支持滑动
        txt_msg.setMaxHeight(ScreenUtils.getScreenHeight(context) / 2);
        txt_msg.setMovementMethod(ScrollingMovementMethod.getInstance());
        btn_neg = view.findViewById(R.id.btn_neg);
        btn_neg.setVisibility(View.GONE);
        btn_pos = view.findViewById(R.id.btn_pos);
        btn_pos.setVisibility(View.GONE);
        divPosNeg = view.findViewById(R.id.div_pos_neg);
        divider = view.findViewById(R.id.button_divider);
        customLayout = view.findViewById(R.id.custom_layout);

        buttonLayout = view.findViewById(R.id.button_layout);

        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.AlertDialogStyle);
        dialog.setContentView(view);

        // 调整dialog背景大小
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams(
                (int) (width * 0.8),
                LinearLayout.LayoutParams.WRAP_CONTENT));

        return this;
    }

    public AlertDialog setBG(Drawable background) {
        lLayout_bg.setBackground(background);
        return this;
    }

    public AlertDialog setNotShowHint(boolean notShowHint) {
        this.notShowHint = notShowHint;
        return this;
    }

    public AlertDialog setCustomLayout(View view) {
        customLayout.setVisibility(View.VISIBLE);
        customLayout.addView(view);
        return this;
    }

    public AlertDialog setTitle(String title) {
        showTitle = true;
        if ("".equals(title)) {
            txt_title.setText(R.string.hint);
        } else {
            txt_title.setText(title);
        }
        return this;
    }

    public AlertDialog setTitleGravity(int gravity) {
        txt_title.setGravity(gravity);
        return this;
    }

    public AlertDialog setTitle(@StringRes int resId) {
        return setTitle(context.getString(resId));
    }

    public AlertDialog setMsg(String msg) {
        showMsg = true;
        if ("".equals(msg)) {
            txt_msg.setText(R.string.content);
        } else {
            txt_msg.setText(msg);
        }
        return this;
    }

    public AlertDialog setMsgTextColor(int color) {
        txt_msg.setTextColor(color);
        return this;
    }

    public AlertDialog setMsgTextSize(float size) {
        txt_msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        return this;
    }

    public AlertDialog setMsgGravity(int gravity) {
        txt_msg.setGravity(gravity);
        return this;
    }

    public AlertDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public AlertDialog setPositiveButton(String text,
                                         final View.OnClickListener listener) {
        return setPositiveButton(text, listener, true);
    }

    public AlertDialog setPositiveButton(String text,
                                         final View.OnClickListener listener, boolean autoDismiss) {
        showPosBtn = true;
        if ("".equals(text)) {
            btn_pos.setText(R.string.confirm);
        } else {
            btn_pos.setText(text);
        }
        btn_pos.setOnClickListener(v -> {
            if (listener != null)
                listener.onClick(v);
            if (autoDismiss) {
                dialog.dismiss();
            }
        });
        return this;
    }

    public void setPositiveButtonClickable(boolean clickable) {
        btn_pos.setEnabled(clickable);
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public AlertDialog setPositiveButton(int resId,
                                         final View.OnClickListener listener) {
        return setPositiveButton(context.getString(resId), listener);
    }

    public AlertDialog setPositiveButtonColor(int color) {
        btn_pos.setTextColor(context.getResources().getColor(color));
        customPosBtn = true;
        return this;
    }

    public AlertDialog setNegativeButton(String text,
                                         final View.OnClickListener listener) {
        showNegBtn = true;
        if ("".equals(text)) {
            btn_neg.setText(R.string.cancel);
        } else {
            btn_neg.setText(text);
        }
        btn_neg.setOnClickListener(v -> {
            if (listener != null)
                listener.onClick(v);
            dialog.dismiss();
        });
        return this;
    }

    public AlertDialog setNegativeButton(int resId,
                                         final View.OnClickListener listener) {
        return setNegativeButton(context.getString(resId), listener);
    }

    private void setLayout() {
        if (!notShowHint && !showTitle && !showMsg) {
            txt_title.setText(R.string.hint);
            txt_title.setVisibility(View.VISIBLE);
        }

        if (showTitle) {
            txt_title.setVisibility(View.VISIBLE);
        }

        if (showMsg) {
            txt_msg.setVisibility(View.VISIBLE);
        }

        if (!showPosBtn && !showNegBtn) {
            btn_pos.setText(R.string.confirm);
            btn_pos.setVisibility(View.VISIBLE);
            btn_pos.setBackgroundResource(R.drawable.alertdialog_single_selector);
            btn_pos.setOnClickListener(v -> dialog.dismiss());
        }

        if (showPosBtn && showNegBtn) {
            btn_pos.setVisibility(View.VISIBLE);
            btn_neg.setVisibility(View.VISIBLE);
            divPosNeg.setVisibility(View.VISIBLE);
        }

        if (showPosBtn && !showNegBtn) {
            btn_pos.setVisibility(View.VISIBLE);
            divPosNeg.setVisibility(View.GONE);
        }

        if (!showPosBtn && showNegBtn) {
            btn_neg.setVisibility(View.VISIBLE);
            divPosNeg.setVisibility(View.GONE);
        }
    }

    protected void clickPositiveButton() {
        btn_pos.performClick();
    }

    public void show() {
        setLayout();
        if (showPosBtn && !showNegBtn) {
            if (!customPosBtn) {
                btn_pos.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btn_pos.getLayoutParams();
            layoutParams.rightMargin = 0;
        }
        dialog.show();
    }

    public AlertDialog onlyShowCustom() {
        showTitle = false;
        showMsg = false;
        lLayout_bg.setBackgroundResource(0);
        txt_title.setVisibility(View.GONE);
        txt_msg.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.GONE);
        return this;
    }

    public @Nullable
    Window getWindow() {
        return dialog.getWindow();
    }

    protected void setCustomLayoutTopMargin(int topMargin) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) customLayout.getLayoutParams();
        lp.setMargins(lp.leftMargin, ScreenUtils.dip2px(context, topMargin), lp.rightMargin, lp.bottomMargin);
        customLayout.setLayoutParams(lp);
    }

    protected void setMsgTopMargin(int topMargin) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) txt_msg.getLayoutParams();
        lp.setMargins(lp.leftMargin, ScreenUtils.dip2px(context, topMargin), lp.rightMargin, lp.bottomMargin);
        txt_msg.setLayoutParams(lp);
    }
}
