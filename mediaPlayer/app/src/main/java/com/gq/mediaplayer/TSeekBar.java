package com.gq.mediaplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
public class TSeekBar extends SeekBar {
    /**
     * 文本的颜色
     */
    private int mTitleTextColor;
    /**
     * 文本的大小
     */
    private float mTitleTextSize;
    private String mTitleText;//文字的内容

    private float img_width, img_height;
    Paint paint;
    private int seekBarMax = 100;
    private float numTextWidth;
    //测量seekbar的规格
    private Rect rect_seek;
    private Paint.FontMetrics fm;

    public static final int TEXT_ALIGN_LEFT = 0x00000001;
    public static final int TEXT_ALIGN_RIGHT = 0x00000010;
    public static final int TEXT_ALIGN_CENTER_VERTICAL = 0x00000100;
    public static final int TEXT_ALIGN_CENTER_HORIZONTAL = 0x00001000;
    public static final int TEXT_ALIGN_TOP = 0x00010000;
    public static final int TEXT_ALIGN_BOTTOM = 0x00100000;
    /**
     * 文本中轴线X坐标
     */
    private float textCenterX;
    /**
     * 文本baseline线Y坐标
     */
    private float textBaselineY;
    /**
     * 文字的方位
     */
    private int textAlign;

    public TSeekBar(Context context) {
        this(context, null);
    }

    public TSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TSeekBar, defStyleAttr, 0);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.TSeekBar_textsize:
                    mTitleTextSize = array.getDimension(attr, 15f);
                    break;
                case R.styleable.TSeekBar_textcolor:
                    mTitleTextColor = array.getColor(attr, Color.WHITE);
                    break;
            }
        }
        array.recycle();
        getImgWH();
        paint = new Paint();
        paint.setAntiAlias(true);//设置抗锯齿
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(mTitleTextSize);//设置文字大小
        paint.setColor(mTitleTextColor);//设置文字颜色
        //设置控件的padding 给提示文字留出位置
        setPadding((int) Math.ceil(img_width) / 2, 0, (int) Math.ceil(img_height) / 2, 0);//(int) Math.ceil(img_height) + 10
        textAlign = TEXT_ALIGN_CENTER_HORIZONTAL | TEXT_ALIGN_CENTER_VERTICAL;
    }

    /**
     * 获取图片的宽高
     */
    private void getImgWH() {
        img_width = 20;
        img_height = 5;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        rect_seek = this.getProgressDrawable().getBounds();
        Paint paints = new Paint();
        paints.setColor(getResources().getColor(R.color.img_full_opaque));
        paints.setAntiAlias(true);
        RectF rect = new RectF(getPaddingLeft() - 10, rect_seek.top - 10,
                getPaddingLeft() + rect_seek.right + 10, rect_seek.bottom + 10);
        canvas.drawRoundRect(rect, 15, 15, paints);
        super.onDraw(canvas);
        setTextLocation();//定位文本绘制的位置
        //定位文字背景图片的位置
        float bm_x = rect_seek.width() * 1f * getProgress() / seekBarMax;
        float bm_y = rect_seek.height() - 20;
        float text_x = bm_x + (img_width - numTextWidth) / 2;
        canvas.drawText(mTitleText, text_x, (float) (textBaselineY + bm_y + (0.16 * img_height / 2)), paint);//画文字
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        invalidate();//监听手势滑动，不断重绘文字和背景图的显示位置
        return super.onTouchEvent(event);
    }

    @Override
    public synchronized void setMax(int max) {
        super.setMax(max);
        this.seekBarMax = max;
    }

    /**
     * 定位文本绘制的位置
     */
    private void setTextLocation() {
        fm = paint.getFontMetrics();
        //文本的宽度
//        mTitleText = getProgress()  + "W";
        if (TextUtils.isEmpty(mTitleText)) {
            mTitleText = "00:00";
        }
        numTextWidth = paint.measureText(mTitleText);
        float textCenterVerticalBaselineY = img_height / 2 - fm.descent + (fm.descent - fm.ascent) / 2;
        switch (textAlign) {
            case TEXT_ALIGN_CENTER_HORIZONTAL | TEXT_ALIGN_CENTER_VERTICAL:
                textCenterX = img_width / 2;
                textBaselineY = textCenterVerticalBaselineY;
                break;
            case TEXT_ALIGN_LEFT | TEXT_ALIGN_CENTER_VERTICAL:
                textCenterX = numTextWidth / 2;
                textBaselineY = textCenterVerticalBaselineY;
                break;
            case TEXT_ALIGN_RIGHT | TEXT_ALIGN_CENTER_VERTICAL:
                textCenterX = img_width - numTextWidth / 2;
                textBaselineY = textCenterVerticalBaselineY;
                break;
            case TEXT_ALIGN_BOTTOM | TEXT_ALIGN_CENTER_HORIZONTAL:
                textCenterX = img_width / 2;
                textBaselineY = img_height - fm.bottom;
                break;
            case TEXT_ALIGN_TOP | TEXT_ALIGN_CENTER_HORIZONTAL:
                textCenterX = img_width / 2;
                textBaselineY = -fm.ascent;
                break;
            case TEXT_ALIGN_TOP | TEXT_ALIGN_LEFT:
                textCenterX = numTextWidth / 2;
                textBaselineY = -fm.ascent;
                break;
            case TEXT_ALIGN_BOTTOM | TEXT_ALIGN_LEFT:
                textCenterX = numTextWidth / 2;
                textBaselineY = img_height - fm.bottom;
                break;
            case TEXT_ALIGN_TOP | TEXT_ALIGN_RIGHT:
                textCenterX = img_width - numTextWidth / 2;
                textBaselineY = -fm.ascent;
                break;
            case TEXT_ALIGN_BOTTOM | TEXT_ALIGN_RIGHT:
                textCenterX = img_width - numTextWidth / 2;
                textBaselineY = img_height - fm.bottom;
                break;
        }
    }

    public void setText(String content) {
        this.mTitleText = content;
    }
}
