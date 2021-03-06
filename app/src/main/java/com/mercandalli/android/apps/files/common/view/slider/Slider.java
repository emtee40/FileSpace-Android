package com.mercandalli.android.apps.files.common.view.slider;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mercandalli.android.apps.files.R;

/**
 * Created by Jonathan on 23/09/2015.
 * https://github.com/navasmdc/MaterialDesignLibrary
 */
public class Slider extends SliderCustomView {

    private int backgroundColor = Color.parseColor("#4CAF50");
    private SliderBall mSliderBall;
    private Bitmap bitmap;
    private int max = 100;
    private int min = 0;
    private NumberIndicator numberIndicator;
    private OnValueChangedListener onValueChangedListener;
    private boolean placedBall = false;
    private boolean press = false;
    private boolean showNumberIndicator = false;
    private int value = 0;
    private ValueToDisplay valueToDisplay;
    private int initialValue = 0;

    private final Paint mTmpPaint = new Paint();
    private final Paint mPaint = new Paint();
    private final Paint mTransparentPaint = new Paint();

    public boolean isNumberIndicator = true;

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
        mTransparentPaint.setColor(ContextCompat.getColor(context, android.R.color.transparent));
        mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public OnValueChangedListener getOnValueChangedListener() {
        return onValueChangedListener;
    }

    public void setOnValueChangedListener(
            OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    // GETTERS & SETTERS

    public int getValue() {
        return value;
    }

    public void setProgress(final int value) {
        setValue(value);
    }

    public void setValue(final int value) {
        if (!placedBall) {
            post(new Runnable() {

                @Override
                public void run() {
                    setValue(value);
                }
            });
        } else {
            this.value = value;
            float division = (mSliderBall.xFin - mSliderBall.xIni) / max;
            ViewHelper.setX(mSliderBall,
                    value * division + getHeight() / 2 - mSliderBall.getWidth() / 2);
            mSliderBall.changeBackground();
        }
    }

    @Override
    public void invalidate() {
        if (mSliderBall != null) {
            mSliderBall.invalidate();
        }
        super.invalidate();
    }

    public boolean isShowNumberIndicator() {
        return showNumberIndicator;
    }

    public void setShowNumberIndicator(boolean showNumberIndicator) {
        this.showNumberIndicator = showNumberIndicator;
        numberIndicator = (showNumberIndicator) ? new NumberIndicator(
                getContext()) : null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onTouch(event);
    }

    public boolean onTouch(MotionEvent event) {
        isLastTouch = true;
        if (isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {

                if (numberIndicator != null
                        && !numberIndicator.isShowing()
                        && isNumberIndicator) {
                    numberIndicator.show();
                }

                if ((event.getX() <= getWidth() && event.getX() >= 0)) {
                    press = true;
                    // calculate value
                    int newValue = 0;
                    float division = (mSliderBall.xFin - mSliderBall.xIni) / (max - min);
                    if (event.getX() > mSliderBall.xFin) {
                        newValue = max;
                    } else if (event.getX() < mSliderBall.xIni) {
                        newValue = min;
                    } else {
                        newValue = min + (int) ((event.getX() - mSliderBall.xIni) / division);
                    }

                    if (value != newValue) {
                        value = newValue;
                        if (onValueChangedListener != null) {
                            onValueChangedListener.onValueChanged(newValue);
                        }
                    }

                    // move ball indicator
                    float x = event.getX();
                    x = (x < mSliderBall.xIni) ? mSliderBall.xIni : x;
                    x = (x > mSliderBall.xFin) ? mSliderBall.xFin : x;
                    ViewHelper.setX(mSliderBall, x);
                    mSliderBall.changeBackground();

                    // If slider has number indicator
                    if (numberIndicator != null && isNumberIndicator) {
                        // move number indicator
                        numberIndicator.indicator.x = x;
                        numberIndicator.indicator.finalY = SliderUtils
                                .getRelativeTop(this) - getHeight() / 2;
                        numberIndicator.indicator.finalSize = getHeight() / 2;
                        numberIndicator.mNumberIndicator.setText("");
                    }

                } else {
                    press = false;
                    isLastTouch = false;
                    if (numberIndicator != null) {
                        numberIndicator.dismiss();
                    }
                }

            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {

                if (event.getAction() == MotionEvent.ACTION_UP && onValueChangedListener != null) {
                    onValueChangedListener.onValueChangedUp(value);
                }

                if (numberIndicator != null) {
                    numberIndicator.dismiss();
                }
                isLastTouch = false;
                press = false;

            }
        }
        return true;
    }


    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        if (isEnabled()) {
            beforeBackground = backgroundColor;
        }
    }

    /**
     * Make a dark color to press effect
     *
     * @return
     */
    protected int makePressColor() {
        int r = (this.backgroundColor >> 16) & 0xFF;
        int g = (this.backgroundColor >> 8) & 0xFF;
        int b = (this.backgroundColor >> 0) & 0xFF;
        r = (r - 30 < 0) ? 0 : r - 30;
        g = (g - 30 < 0) ? 0 : g - 30;
        b = (b - 30 < 0) ? 0 : b - 30;
        return Color.argb(70, r, g, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!placedBall) {
            placeBall();
        }

        mTmpPaint.reset();

        if (value == min) {
            // Crop line to transparent effect

            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(canvas.getWidth(),
                        canvas.getHeight(), Bitmap.Config.ARGB_8888);
            }
            Canvas temp = new Canvas(bitmap);
            mTmpPaint.setColor(Color.parseColor("#B0B0B0"));
            mTmpPaint.setStrokeWidth(SliderUtils.dpToPx(2, getResources()));
            temp.drawLine(getHeight() / 2, getHeight() / 2, getWidth()
                    - getHeight() / 2, getHeight() / 2, mTmpPaint);
            temp.drawCircle(ViewHelper.getX(mSliderBall) + mSliderBall.getWidth() / 2,
                    ViewHelper.getY(mSliderBall) + mSliderBall.getHeight() / 2,
                    mSliderBall.getWidth() / 2, mTransparentPaint);

            canvas.drawBitmap(bitmap, 0, 0, mPaint);
        } else {
            mTmpPaint.setColor(Color.parseColor("#B0B0B0"));
            mTmpPaint.setStrokeWidth(SliderUtils.dpToPx(2, getResources()));
            canvas.drawLine(getHeight() / 2, getHeight() / 2, getWidth()
                    - getHeight() / 2, getHeight() / 2, mTmpPaint);
            mTmpPaint.setColor(backgroundColor);
            float division = (mSliderBall.xFin - mSliderBall.xIni) / (max - min);
            int value = this.value - min;

            canvas.drawLine(getHeight() / 2, getHeight() / 2, value * division
                    + getHeight() / 2, getHeight() / 2, mTmpPaint);

        }

        if (press && !showNumberIndicator) {
            mTmpPaint.setColor(backgroundColor);
            mTmpPaint.setAntiAlias(true);
            canvas.drawCircle(ViewHelper.getX(mSliderBall) + mSliderBall.getWidth() / 2,
                    getHeight() / 2, getHeight() / 3, mTmpPaint);
        }
        invalidate();
    }

    // Set attributes of XML to View
    protected void setAttributes(AttributeSet attrs) {
        setBackgroundResource(R.drawable.background_transparent);

        // Set size of view
        setMinimumHeight(SliderUtils.dpToPx(48, getResources()));
        setMinimumWidth(SliderUtils.dpToPx(80, getResources()));

        // Set background Color
        // Color by resource
        int backgroundColor = attrs.getAttributeResourceValue(ANDROIDXML,
                "background", -1);
        if (backgroundColor != -1) {
            setBackgroundColor(getResources().getColor(backgroundColor));
        } else {
            // Color by hexadecimal
            int background = attrs.getAttributeIntValue(ANDROIDXML, "background", -1);
            if (background != -1) {
                setBackgroundColor(background);
            }
        }

        showNumberIndicator = attrs.getAttributeBooleanValue(MATERIALDESIGNXML,
                "showNumberIndicator", false);
        min = attrs.getAttributeIntValue(MATERIALDESIGNXML, "min", 0);
        max = attrs.getAttributeIntValue(MATERIALDESIGNXML, "max", 0);
        value = attrs.getAttributeIntValue(MATERIALDESIGNXML, "value", min);

        mSliderBall = new SliderBall(getContext());
        RelativeLayout.LayoutParams params = new LayoutParams(SliderUtils.dpToPx(20,
                getResources()), SliderUtils.dpToPx(20, getResources()));
        params.addRule(CENTER_VERTICAL, TRUE);
        mSliderBall.setLayoutParams(params);
        addView(mSliderBall);

        initialValue = min;

        if (value != min) {
            initialValue = value;
            setValue(value);
        }

        // Set if slider content number indicator
        // TODO
        if (showNumberIndicator) {
            numberIndicator = new NumberIndicator(getContext());
        }

    }

    public void updateAfterRotation() {
        mSliderBall.xFin = getWidth() - getHeight() / 2 - mSliderBall.getWidth() / 2;
    }

    private void placeBall() {
        ViewHelper.setX(mSliderBall, getHeight() / 2 - mSliderBall.getWidth() / 2);
        mSliderBall.xIni = ViewHelper.getX(mSliderBall);
        mSliderBall.xFin = getWidth() - getHeight() / 2 - mSliderBall.getWidth() / 2;
        mSliderBall.xCen = getWidth() / 2 - mSliderBall.getWidth() / 2;
        placedBall = true;
    }

    // Event when slider change value
    public interface OnValueChangedListener {
        void onValueChanged(int value);

        void onValueChangedUp(int value);
    }

    class SliderBall extends View {

        float xIni, xFin, xCen;

        public SliderBall(Context context) {
            super(context);
            setBackgroundResource(R.drawable.background_switch_ball_uncheck);
        }

        public void changeBackground() {
            if (value != min) {
                setBackgroundResource(R.drawable.background_checkbox);
                LayerDrawable layer = (LayerDrawable) getBackground();
                GradientDrawable shape = (GradientDrawable) layer
                        .findDrawableByLayerId(R.id.shape_background);
                shape.setColor(backgroundColor);
            } else {
                setBackgroundResource(R.drawable.background_switch_ball_uncheck);
            }
        }

    }

    // Slider Number Indicator

    class Indicator extends RelativeLayout {

        boolean animate = true;
        // Final size after animation
        float finalSize = 0;
        // Final y position after animation
        float finalY = 0;
        boolean numberIndicatorResize = false;
        // Size of number indicator
        float size = 0;
        // Position of number indicator
        float x = 0;
        float y = 0;

        private Paint paint, paintBorder;

        public Indicator(Context context) {
            super(context);
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            //setBackgroundColor(getResources().getColor(R.color.actionbar));

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(backgroundColor);

            // Border and shadow
            paintBorder = new Paint();
            paintBorder.setAntiAlias(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (!numberIndicatorResize) {
                LayoutParams params = (LayoutParams) numberIndicator.mNumberIndicator
                        .getLayoutParams();
                params.height = (int) finalSize * 2;
                params.width = (int) finalSize * 2;
                numberIndicator.mNumberIndicator.setLayoutParams(params);
            }

            if (animate) {
                if (y == 0) {
                    y = finalY + finalSize * 2;
                }
                y -= SliderUtils.dpToPx(6, getResources());
                size += SliderUtils.dpToPx(2, getResources());
            }

            float cx = ViewHelper.getX(mSliderBall) + SliderUtils.getRelativeLeft((View) mSliderBall.getParent())
                    + mSliderBall.getWidth() / 2;

            paintBorder.setShadowLayer(size / 1.6f, 0.0f, size / 4.0f, Color.BLACK);

            canvas.drawCircle(cx, y, size / 1.6f, paintBorder);
            canvas.drawCircle(cx, y, size, paint);

            if (animate && size >= finalSize) {
                animate = false;
            }
            if (!animate) {
                ViewHelper.setX(numberIndicator.mNumberIndicator,
                        cx - size);
                ViewHelper.setY(numberIndicator.mNumberIndicator, y - size);

                if (valueToDisplay != null) {
                    numberIndicator.mNumberIndicator.setText(valueToDisplay.convert(value));
                } else {
                    numberIndicator.mNumberIndicator.setText(String.valueOf(value));
                }
            }

            invalidate();
        }

    }

    class NumberIndicator extends Dialog {

        Indicator indicator;
        TextView mNumberIndicator;

        public NumberIndicator(Context context) {
            //super(context, android.R.style.Theme_Translucent);
            super(context, android.R.style.Theme_Translucent);
        }

        @Override
        public void dismiss() {
            super.dismiss();
            if (indicator != null) {
                indicator.y = 0;
                indicator.size = 0;
                indicator.animate = true;
            }
        }

        @Override
        public void onBackPressed() {
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.number_indicator_spinner);
            setCanceledOnTouchOutside(false);

            RelativeLayout content = (RelativeLayout) this
                    .findViewById(R.id.number_indicator_spinner_content);
            indicator = new Indicator(this.getContext());
            content.addView(indicator);

            mNumberIndicator = new TextView(getContext());
            mNumberIndicator.setTextColor(Color.WHITE);
            mNumberIndicator.setTypeface(null, Typeface.BOLD);
            mNumberIndicator.setGravity(Gravity.CENTER);
            content.addView(mNumberIndicator);

            indicator.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.FILL_PARENT));
        }

    }

    public boolean isPress() {
        return press;
    }

    public interface ValueToDisplay {
        String convert(int value);
    }

    public void setValueToDisplay(ValueToDisplay valueToDisplay) {
        this.valueToDisplay = valueToDisplay;
    }
}