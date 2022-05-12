package im.vector.app.eachchat.ui.index;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

import im.vector.app.R;
import im.vector.app.core.utils.DimensionConverter;
import im.vector.app.eachchat.base.BaseModule;
import im.vector.app.eachchat.contact.data.User;
import im.vector.app.eachchat.department.data.IDisplayBean;
import im.vector.app.eachchat.rx.SimpleObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by zhouguanjie on 2019/8/22.
 */
public class IndexView extends View {

    private static final int INDEX_NONE = -1;

    private char[] CHARS = {'#'};
    /**
     * text大小
     */
    private float textSize = 24.f;
    /**
     * 字符颜色与索引颜色
     */
    private int textColor = Color.BLACK, indexTextColor = Color.RED;
    /**
     * 画笔
     */
    private TextPaint textPaint;
    private Paint backgroupPaint;
    /**
     * 每个item
     */
    private float itemHeight;
    /**
     * 文本居中时的位置
     */
    private float textY;
    /**
     * 当前位置
     */
    private int currentIndex = INDEX_NONE;

    private Drawable indexDrawable;

    private Context context;

    public IndexView(Context context) {
        super(context);
        init(context, null);
    }

    public IndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IndexView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndexView);
            textSize = a.getDimension(R.styleable.IndexView_indexTextSize, textSize);
            textColor = a.getColor(R.styleable.IndexView_charTextColor, textColor);
            indexTextColor = a.getColor(R.styleable.IndexView_indexTextColor, indexTextColor);
            a.recycle();
        }
        backgroupPaint = new Paint();
        backgroupPaint.setAntiAlias(true);
        backgroupPaint.setColor(context.getResources().getColor(R.color.send_btn_color));
        this.context = context;
        indexDrawable = context.getResources().getDrawable(R.drawable.index_color);
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        textPaint.setTypeface(font);
        itemHeight = new DimensionConverter(BaseModule.getContext().getResources()).dpToPx(15);
    }

    public float getItemHeight() {
        return itemHeight;
    }

    public void setCHARSAsync(List<User> contacts) {
        Observable.create((ObservableOnSubscribe<List<Character>>) emitter -> {
            List<Character> characters = new ArrayList<>();
            for (User user : contacts) {
                Character character = user.getFirstChar();
                if (!characters.contains(character)) {
                    characters.add(character);
                }
            }
            CHARS = new char[characters.size()];
            for (int index = 0; index < characters.size(); index++) {
                CHARS[index] = characters.get(index);
            }
            emitter.onNext(characters);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Character>>() {
                    @Override
                    public void onNext(List<Character> characters) {
                        ViewGroup.LayoutParams Params = getLayoutParams();
                        Params.height = 0;
                        if (characters.size() > 0) {
                            Params.height = (int) ((characters.size() + 1) * itemHeight);
                        }
                        setLayoutParams(Params);
                        postInvalidate();
                    }
                });
    }

    public void setCHARS(List<Character> characters) {
        CHARS = new char[characters.size()];
        for (int index = 0; index < characters.size(); index++) {
            CHARS[index] = characters.get(index);
        }
        ViewGroup.LayoutParams Params = getLayoutParams();
        Params.height = 0;
        if (characters.size() > 0) {
            Params.height = (int) ((characters.size() + 1) * itemHeight);
        }
        setLayoutParams(Params);
        postInvalidate();
    }

    public void setCHARSAsyncEx(List<? extends IDisplayBean> contacts) {
        Observable.create((ObservableOnSubscribe<List<Character>>) emitter -> {
            List<Character> characters = new ArrayList<>();
            for (IDisplayBean contract : contacts) {
                if (!(contract instanceof User)) continue;
                User user = (User) contract;
                Character character = user.getFirstChar();
                if (!characters.contains(character)) {
                    characters.add(character);
                }
            }
            CHARS = new char[characters.size()];
            for (int index = 0; index < characters.size(); index++) {
                CHARS[index] = characters.get(index);
            }
            emitter.onNext(characters);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Character>>() {
                    @Override
                    public void onNext(List<Character> characters) {
                        ViewGroup.LayoutParams Params = getLayoutParams();
                        Params.height = 0;
                        if (characters.size() > 0) {
                            Params.height = (int) ((characters.size() + 1) * itemHeight);
                        }
                        setLayoutParams(Params);
                        invalidate();
                    }
                });
    }

    public void setDislayDataAsync(List<IDisplayBean> contacts) {
        Observable.create((ObservableOnSubscribe<List<Character>>) emitter -> {
            List<Character> characters = new ArrayList<>();
            for (IDisplayBean contract : contacts) {
                User user = (User) contract;
                Character character = user.getFirstChar();
                if (!characters.contains(character)) {
                    characters.add(character);
                }
            }
            CHARS = new char[characters.size()];
            for (int index = 0; index < characters.size(); index++) {
                CHARS[index] = characters.get(index);
            }
            emitter.onNext(characters);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Character>>() {
                    @Override
                    public void onNext(List<Character> characters) {
                        ViewGroup.LayoutParams Params = getLayoutParams();
                        Params.height = (int) ((characters.size() + 1) * itemHeight);
                        setLayoutParams(Params);
                        postInvalidate();
                    }
                });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.bottom - fm.top;
//        int height = getHeight() - getPaddingTop() - getPaddingBottom();
//        itemHeight = height / (float) CHARS.length;
        textY = itemHeight - (itemHeight - textHeight) / 2 - fm.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getPaddingLeft() + (new DimensionConverter(BaseModule.getContext().getResources()).dpToPx(20) - getPaddingLeft() - getPaddingRight()) / 2.0f;
        float centerY = getPaddingTop() + textY;
        float centerBackY = getPaddingTop() + itemHeight / 2;
        if (centerX <= 0 || centerY <= 0) return;
        for (int i = 0; i < CHARS.length; i++) {
            char c = CHARS[i];
            textPaint.setColor(i == currentIndex ? indexTextColor : textColor);
            if (i == currentIndex) {
                canvas.drawCircle(centerX, centerBackY, textY / 2, backgroupPaint);
            }
            canvas.drawText(String.valueOf(c), centerX, centerY, textPaint);
            centerY += itemHeight;
            centerBackY += itemHeight;
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int currentIndex = INDEX_NONE;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                setBackgroundDrawable(indexDrawable);
                currentIndex = computeCurrentIndex(event);
                if (currentIndex == INDEX_NONE) {
                    break;
                }
                if (listener != null) {
                    Timber.e("--->" + currentIndex * itemHeight + getPaddingTop() + "");
                    listener.onCharIndexSelected(String.valueOf(CHARS[currentIndex]), currentIndex);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentIndex = computeCurrentIndex(event);
                if (currentIndex == INDEX_NONE || CHARS.length < 1) {
                    break;
                }
                if (listener != null) {
                    listener.onCharIndexSelected(String.valueOf(CHARS[currentIndex]), currentIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                setBackgroundDrawable(null);
                if (listener != null) {
                    listener.onCharIndexSelected(null, 0);
                }
                break;
        }
        if (currentIndex != this.currentIndex) {
            this.currentIndex = currentIndex;
            invalidate();
            if (this.currentIndex != INDEX_NONE && listener != null && CHARS.length > currentIndex) {
                listener.onCharIndexChanged(CHARS[this.currentIndex]);
            }
        }
        return true;
    }

    private int computeCurrentIndex(MotionEvent event) {
        if (itemHeight <= 0) return INDEX_NONE;
        float y = event.getY() - getPaddingTop();
        int index = (int) (y / itemHeight);
        if (index < 0) {
            index = 0;
        } else if (index >= CHARS.length) {
            index = CHARS.length - 1;
        }
        return index;
    }

    private OnCharIndexChangedListener listener;

    public void setOnCharIndexChangedListener(OnCharIndexChangedListener listener) {
        this.listener = listener;
    }

    public interface OnCharIndexChangedListener {

        void onCharIndexChanged(char currentIndex);

        void onCharIndexSelected(String currentIndex, int curPos);
    }


}
