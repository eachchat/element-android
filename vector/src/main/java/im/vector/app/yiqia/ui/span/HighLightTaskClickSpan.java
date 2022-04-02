package im.vector.app.yiqia.ui.span;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by zhouguanjie on 2019/11/6.
 */
public class HighLightTaskClickSpan extends ClickableSpan {

    String text;
    int type;
    String userId;
    HighLightTaskClickListener listener;
    HightLightTextClickListener textClickListener;
    int color;
    boolean hasUnderline = false;

    float textSize = -1;
    boolean fakeBoldText = false;


    public HighLightTaskClickSpan(int type, String userId, int color, HighLightTaskClickListener listener, boolean hasUnderline) {
        super();
        this.type = type;
        this.userId = userId;
        this.color = color;
        this.listener = listener;
        this.hasUnderline = hasUnderline;
    }

    public HighLightTaskClickSpan(String text, int color, HightLightTextClickListener listener) {
        super();
        this.color = color;
        this.textClickListener = listener;
        this.text = text;
    }

    public HighLightTaskClickSpan(String text, int color, boolean hasUnderline, HightLightTextClickListener listener) {
        super();
        this.color = color;
        this.textClickListener = listener;
        this.text = text;
        this.hasUnderline = hasUnderline;
    }


    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(color);
        ds.setUnderlineText(hasUnderline);

        if (textSize != -1) {
            ds.setTextSize(textSize);
        }
        ds.setFakeBoldText(fakeBoldText);//仿“粗体”设置
    }

    @Override
    public void onClick(View widget) {
        if (listener != null) {
            listener.onClick(type, userId);
        }
        if (textClickListener != null) {
            textClickListener.onClick(text);
        }
    }

    public String getText() {
        return text;
    }

    public interface HighLightTaskClickListener {
        public void onClick(int type, String userId);
    }

    public interface HightLightTextClickListener {
        public void onClick(String text);
    }

}