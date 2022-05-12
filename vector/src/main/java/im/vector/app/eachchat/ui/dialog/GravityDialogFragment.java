package im.vector.app.eachchat.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import im.vector.app.R;


public abstract class GravityDialogFragment extends DialogFragment implements DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    public static final float DEFAULT_DIM_AMOUNT = 0.6f;
    protected View sRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContext() == null || getDialog() == null) return null;
//        if (getEventBusAble()) {
//            EventBus.getDefault().register(this);
//        }
        DisplayMetrics d = getContext().getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        Window window = getDialog().getWindow();
        setSoftInputState(window);
        sRootView = inflater.inflate(getLayoutId(), ((ViewGroup) window.findViewById(android.R.id.content)), false);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = getGravity();
        if (getLayoutWidth() > 0) {
            lp.width = (int) (d.widthPixels * getLayoutWidth()); //设置w,h要在setContentView()之后，不然可能显示的大小有问题
        }
        if (getLayoutHeight() > 0) {
            lp.height = (int) (d.heightPixels * getLayoutHeight());
        }
        if (getLayoutRatio() > 0) {
            lp.height = (int) (lp.width * getLayoutRatio());
        }
        if (getExactWidth() > 0) {
            lp.width = (int) getExactWidth();
        }
        if (getExactHeight() > 0) {
            lp.height = (int) getExactHeight();
        }

        if (getAnimationStyle() > 0) lp.windowAnimations = getAnimationStyle();
        if (getDimAmount() < 0) {
            lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        } else {
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.dimAmount = getDimAmount();
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(lp);
        setCancelable(getCanceledAble());
        getDialog().setCanceledOnTouchOutside(getCanceledOnTouchOutside());
        getDialog().setCancelable(getCanceledAble());
        if (getAnimationStyle() > 0) {
            getDialog().getWindow().setWindowAnimations(getAnimationStyle());
        }
//        if (isHideNavigationBar()) {
//            NavBarUtils.hideNavigationBar(getDialog().getWindow());
//        }
        getDialog().setOnShowListener(this);
        getDialog().setOnDismissListener(this);
        initView(sRootView);
        return sRootView;
    }

    public boolean isShowing() {
        Dialog dialog = getDialog();
        if (dialog == null) return false;
        return dialog.isShowing();
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
//        if (getEventBusAble()) {
//            EventBus.getDefault().unregister(this);
//        }
        super.onDismiss(dialog);
    }

    protected abstract void initView(View sRootView);

    protected abstract int getLayoutId();

    protected int getGravity() {
        return Gravity.CENTER;
    }

    protected int getAnimationStyle() {
        return R.style.AnimBottom2Top;
    }

    /**
     * 弹窗背景透明度 0为纯透明
     */
    protected float getDimAmount() {
        return DEFAULT_DIM_AMOUNT;
    }

    /**
     * 弹窗宽度 相对屏幕宽度占比
     */
    protected float getLayoutWidth() {
        return 0.8f;
    }

    /**
     * 弹窗高度 相对屏幕高度占比
     */
    protected float getLayoutHeight() {
        return -1;
    }

    /**
     * 弹窗宽度 固定数值
     */
    protected float getExactWidth() {
        return -1;
    }

    /**
     * 弹窗高度 固定数值
     */
    protected float getExactHeight() {
        return -1;
    }


    protected float getLayoutRatio() {
        return -1;
    }

    /**
     * false - dialog弹出后会点击屏幕，dialog不消失；点击物理返回键dialog消失
     */
    protected boolean getCanceledOnTouchOutside() {
        return true;
    }

    /**
     * false - dialog弹出后会点击屏幕或物理返回键，dialog不消失
     */
    protected boolean getCanceledAble() {
        return true;
    }

    protected boolean getEventBusAble() {
        return false;
    }

    protected boolean isHideNavigationBar() {
        return false;
    }

    protected void setSoftInputState(Window mWindow) {
    }

}
