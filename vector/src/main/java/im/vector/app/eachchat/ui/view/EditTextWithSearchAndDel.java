package im.vector.app.eachchat.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import im.vector.app.R;

/**
 * Created by chengww on 2019-10-15.
 */
public class EditTextWithSearchAndDel extends AppCompatEditText {
    private Drawable imgAble;
    private Drawable imaSearch;

    public EditTextWithSearchAndDel(Context context) {
        super(context);
        init(context, null);
    }
    public EditTextWithSearchAndDel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    public EditTextWithSearchAndDel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    private void init(Context context, AttributeSet attrs) {
        imgAble = ContextCompat.getDrawable(getContext(), R.mipmap.ic_delete);
        imaSearch = ContextCompat.getDrawable(getContext(), R.mipmap.ic_search_inner);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable editable) {
                setDrawable();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditTextWithSearchAndDel);
            boolean showMagnifier = a.getBoolean(R.styleable.EditTextWithSearchAndDel_showMagnifier, true);
            if (!showMagnifier) {
                imaSearch = null;
            }
            a.recycle();
        }
        setDrawable();
    }
    //设置删除图片
    private void setDrawable() {
        if(!isEnabled() || length() < 1)
            setCompoundDrawablesWithIntrinsicBounds(imaSearch, null, null, null);
        else
            setCompoundDrawablesWithIntrinsicBounds(imaSearch, null, imgAble, null);
    }
    // 处理删除事件
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imgAble != null && event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getRawX();
            int eventY = (int) event.getRawY();
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - 100;
            if(rect.contains(eventX, rect.centerY()) && isEnabled())
                setText("");
        }
        return super.onTouchEvent(event);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
