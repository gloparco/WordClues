package com.kindredgames.wordclues.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.WordCluesActivity;

public class MeasureLayout extends FrameLayout {

    private static int windowH, windowW;

    private WordCluesActivity activity;

    public void setActivity(WordCluesActivity activity) {
        this.activity = activity;
    }

    public MeasureLayout(Context context) {
        super(context);
    }

    public MeasureLayout(Context context, AttributeSet attrs){
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        windowW = getMeasuredWidth();
        windowH = getMeasuredHeight();
        KGLog.i("W=%s, H=%s", windowW, windowH);
        activity.onMeasure(windowW, windowH);
    }

    public static int getFrameWidth(){
        return windowW;
    }

    public static int getFrameHeight(){
        return windowH;
    }
}