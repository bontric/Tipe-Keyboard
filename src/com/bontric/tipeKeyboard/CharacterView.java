/**
 *@name CharacterView
 *@author Benedikt John Wieder, Jakob Frick
 *
 *
 *
 *
 */

package com.bontric.tipeKeyboard;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.LinkedList;

public class CharacterView extends View {

    private LinkedList<CharacterArea> characterAreas;
    private PointF touchStartPoint;
    private int mWidth;
    private int mHeight;

    public CharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CharacterView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialize the view This Character View is programmed for a 6-key layout.
     */
    public void init() {
        LayoutParams params = new LinearLayout.LayoutParams(
                KeyboardHandler.keyboard_width,
                KeyboardHandler.character_view_height);
        this.setLayoutParams(params);
        mWidth = params.width;
        mHeight = params.height;
        initCharAreas();
    }

    /**
     * Initialize the character areas this is hard coded for 6 areas! ( due to
     * readability)
     */
    private void initCharAreas() {

        // this.setBackgroundColor(Color.RED);
        characterAreas = new LinkedList<CharacterArea>();
        float x = 0;
        float y = 0;
        float width = mWidth / 3;
        float height = mHeight / 2;

        characterAreas.add(new CharacterArea(x, y, width, height,
                KeyboardHandler.char_view_dark_color));
        characterAreas.add(new CharacterArea(x + width, y, width, height,
                KeyboardHandler.char_view_light_color));
        characterAreas.add(new CharacterArea(x + 2 * width, y, width, height,
                KeyboardHandler.char_view_dark_color));
        characterAreas.add(new CharacterArea(x, y + height, width, height,
                KeyboardHandler.char_view_light_color));
        characterAreas.add(new CharacterArea(x + width, y + height, width,
                height, KeyboardHandler.char_view_dark_color));
        characterAreas.add(new CharacterArea(x + 2 * width, y + height, width,
                height, KeyboardHandler.char_view_light_color));

        setLevelUpChars();
    }

	/*
     * ###############################################################
	 * Unflexible longpress inmplementation
	 */

    private Runnable longPressActionRunnable = new Runnable() {
        public void run() {

            isLongPressed = setCharAlternatives();
        }
    };
    private Handler longPressHandler = new Handler();
    private final int longpressTimeout = KeyboardHandler.longpress_timeout;
    private char mLongPressedChar;
    private boolean isLongPressed = false;

    /**
     * @return Returns true when a pressed character has alternatives false if
     * not => longpress check is still pending
     */
    private boolean setCharAlternatives() {
        for (String s : this.getResources().getStringArray(
                R.array.key_alternatives)) {
            if (s.charAt(0) == mLongPressedChar) {
                this.setLevelDownChars(s.substring(1, s.length()));
                this.invalidate();
                return true;
            }
        }

        return false;

    }

	/*
	 * ################################################################
	 */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double sensitivity = 1.2;
        PointF pressedPoint = util.getEventMedianPos(event);
        CharacterArea pressed = getAreaFromTouch(pressedPoint);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartPoint = pressedPoint;

                if (pressed != null) {
                    setLevelDownChars(pressed.getChars());
                    longPressHandler.postDelayed(longPressActionRunnable,
                            longpressTimeout);
                /*
				 * we are already in level down state due to setLevelDownState()
				 * call before! This might look wrong without context..
				 */
                    mLongPressedChar = pressed.getChars().charAt(0);
                    this.invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                longPressHandler.removeCallbacks(longPressActionRunnable);

                if (pressed != null && !isLongPressed) {
                    longPressHandler.postDelayed(longPressActionRunnable,
                            longpressTimeout);
                    mLongPressedChar = pressed.getChars().charAt(0);
                }

                break;
            case MotionEvent.ACTION_UP:
                isLongPressed = false;
                longPressHandler.removeCallbacks(longPressActionRunnable);

                PointF touchEndPoint = util.getEventMedianPos(event);
                PointF swipeVec = new PointF();
            /*
             * extend swipe vector
			 */
                swipeVec.x = touchStartPoint.x
                        + (int) ((touchEndPoint.x - touchStartPoint.x) * sensitivity);
                swipeVec.y = touchStartPoint.y
                        + (int) ((touchEndPoint.y - touchStartPoint.y) * sensitivity);
            /*
             * if the vector extension brings the touch out of CharacterView
			 * We'll just interpret the release point!
			 */
                CharacterArea released = null;
                if (isInBounds(swipeVec.x, swipeVec.y)) {
                    released = getAreaFromTouch(swipeVec);
                } else if (isInBounds(touchEndPoint.x, touchEndPoint.y)) {
                    released = getAreaFromTouch(touchEndPoint);

                }
                if (released != null && !released.getChars().equals("")) {
                /*
				 * make sure this only sends one character a time..
				 */
                    KeyboardHandler.input_connection.sendKey(released.getChars()
                            .charAt(0));
                } else {
                /*
				 * this is just for testing! when you release your finger
				 * outside the Character View you'll send a space to the
				 * inputConnection
				 */
                    KeyboardHandler.input_connection.handleSpace();
                }
                setLevelUpChars();
                this.invalidate();
                break;
        }

        return true;
    }

    private boolean isInBounds(float x, float y) {
        /**
         * check if the touch is out of borders ( we'll then get the min/max
         * values for x/y)
         *
         * => The touch point is relative to this view. While this.getX()/getY()
         * is relative to the Layout(TipeView)
         */
        if (x <= 0 || x >= this.getWidth() || y <= 0 || y >= this.getHeight()) {

            return false;
        }
        return new RectF(0, 0, this.getWidth(), this.getHeight())
                .contains(x, y);
    }

    /**
     * @return null if touch is out of the Characterview Area!
     */
    private CharacterArea getAreaFromTouch(PointF pt) {

        for (CharacterArea cA : characterAreas) {
            if (cA.contains(pt)) {
                return cA;
            }
        }
        return null;
    }

    // =================From here on downwards drawing related things===========


    private void setLevelUpChars() {
        /*
		 * check this if you need more symbol set's
		 */
        String charset = KeyboardHandler.current_charset;
        for (int i = 0; i < 6; ++i) {
            characterAreas.get(i).setChars(
                    charset.substring(i * 6, (i + 1) * 6));
        }
    }

    private void setLevelDownChars(String charset) {

        for (int i = 0; i < characterAreas.size(); ++i) {
            if (i < charset.length()) {
                characterAreas.get(i).setChars("" + charset.charAt(i));
            } else {
                characterAreas.get(i).setChars("");
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (KeyboardHandler.charset_changed) {
            setLevelUpChars();
            KeyboardHandler.charset_changed = false;
            // tell the Keyboard handler that we handled shift.
        }

        for (CharacterArea ca : characterAreas) {
            ca.draw(canvas);
        }
              /*
             *   might look nice..
             *   & it does @ Jakob
             *   @now for testing!
             */
        Paint seperatorPaint = new Paint();
        seperatorPaint.setStrokeWidth(2);
        seperatorPaint.setColor(Color.WHITE);
        canvas.drawLine(0, getHeight(), getWidth(), getHeight(), seperatorPaint);
        canvas.drawLine(0, 0, getWidth(), 0, seperatorPaint);
    }

    private class CharacterArea {
        private String mCharacters;
        private RectF mSpace;
        private int mBgColor;
        private Paint mPaint = new Paint();
        private LinkedList<PointF> textCenters;

        public CharacterArea(float x, float y, float width, float height,
                             int bg_color) {
            this.mSpace = new RectF(x, y, x + width, y + height);
            mBgColor = bg_color;
            mPaint.setTextSize(KeyboardHandler.default_font_size);

        }

        public boolean contains(PointF pt) {
            return mSpace.contains(pt.x, pt.y);
        }

        private void initCenters() {
            float width = Math.abs(mSpace.left - mSpace.right) / 3;
            float height = Math.abs(mSpace.top - mSpace.bottom) / 2;
            float x = mSpace.left;
            float y = mSpace.top;
            PointF center = util.getTextCenterToDraw(
                    "" + mCharacters.charAt(0), new RectF(x, y, x + width, y
                    + height), mPaint);
            textCenters = new LinkedList<PointF>();
            textCenters.add(center);
            textCenters.add(new PointF(center.x + width, center.y));
            textCenters.add(new PointF(center.x + width * 2, center.y));
            textCenters.add(new PointF(center.x, center.y + height));
            textCenters.add(new PointF(center.x + width, center.y + height));
            textCenters
                    .add(new PointF(center.x + width * 2, center.y + height));

        }

        public void draw(Canvas canvas) {

            mPaint.setColor(mBgColor);
            canvas.drawRect(mSpace, mPaint);
            mPaint.setColor(KeyboardHandler.default_font_color);
            mPaint.setFakeBoldText(true);
            mPaint.setTextAlign(Align.CENTER);
            int i = 0;
            if (mCharacters.length() == 6) {
                for (PointF center : textCenters) {
                    canvas.drawText("" + mCharacters.charAt(i), center.x,
                            center.y, mPaint);
                    ++i;
                }

            } else {
                PointF center = util.getTextCenterToDraw(mCharacters, mSpace,
                        mPaint);
                canvas.drawText(mCharacters, center.x, center.y, mPaint);
            }
        }

        public String getChars() {
            return mCharacters;
        }

        public void setChars(String chars) {
            mCharacters = chars;
            if (!mCharacters.equals("")) {
                initCenters();
            }
        }
    }
}