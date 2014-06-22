/**
 *@name TipeCharacterView
 *@author Benedikt John Wieder, Jakob Frick
 *Copyright  2014 Benedikt Wieder, Jakob Frick

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *
 *
 */

package com.bontric.tipeKeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

import java.util.LinkedList;

class TapTapCharacterView extends View {

    private LinkedList<CharacterArea> characterAreas;
    private int mWidth;
    private int mHeight;
    private boolean isLevelDown;
    private Paint seperatorPaint = new Paint();

    public TapTapCharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TapTapCharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TapTapCharacterView(Context context) {
        super(context);
    }

    /**
     * Initialize the view This Character View is programmed for a 6-key layout.
     */
    public void init() {
        isLevelDown = false;
        LayoutParams params = new LayoutParams(
                KeyboardHandler.keyboard_width,
                KeyboardHandler.character_view_height);
        this.setLayoutParams(params);
        mWidth = params.width;
        mHeight = params.height;
        seperatorPaint.setStrokeWidth(2);
        seperatorPaint.setColor(KeyboardHandler.default_font_color);
        initCharAreas();
    }

    /**
     * Initialize the character areas this is hard coded for 6 areas! ( due to
     * readability)
     */
    private void initCharAreas() {

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
        PointF pos = Util.getEventMedianPos(event);
        CharacterArea pressed = getAreaFromTouch(pos);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (pressed != null && isLevelDown && pressed.getChars().length()>0) {
                    longPressHandler.postDelayed(longPressActionRunnable,
                            longpressTimeout);
                    mLongPressedChar = pressed.getChars().charAt(0);
                    this.invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                longPressHandler.removeCallbacks(longPressActionRunnable);
                if (pressed != null && !isLongPressed && isLevelDown && pressed.getChars().length()>0) {
                    longPressHandler.postDelayed(longPressActionRunnable,
                            longpressTimeout);
                    mLongPressedChar = pressed.getChars().charAt(0);
                }

                break;
            case MotionEvent.ACTION_UP:
                if(pressed == null){
                    this.setLevelUpChars();
                    this.invalidate();
                    return false;
                }
                if (isLongPressed) {
                    isLongPressed = false;
                } else if (!isLevelDown) {
                    setLevelDownChars(pressed.getChars());
                    longPressHandler.removeCallbacks(longPressActionRunnable);
                    this.invalidate();
                } else if (isInBounds(pos.x, pos.y) && !pressed.getChars().equals("")) {
                    KeyboardHandler.input_connection.sendKey(pressed.getChars().charAt(0));
                    setLevelUpChars();
                    longPressHandler.removeCallbacks(longPressActionRunnable);
                    this.invalidate();
                }


        }






        return true;
    }

    /**
     * check if the touch is out of borders ( we'll then get the min/max
     * values for x/y)
     * <p/>
     * => The touch point is relative to this view. While this.getX()/getY()
     * is relative to the whole Layout
     */
    private boolean isInBounds(float x, float y) {

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
        isLevelDown = false;
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
        isLevelDown = true;
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


        if (KeyboardHandler.charset_changed) {
            setLevelUpChars();
            KeyboardHandler.charset_changed = false;
            // tell the Keyboard handler that we handled shift.
        }

        for (CharacterArea ca : characterAreas) {
            ca.draw(canvas);
        }

        canvas.drawLine(0, getHeight(), getWidth(), getHeight(), seperatorPaint);
        canvas.drawLine(0, 0, getWidth(), 0, seperatorPaint);
    }

    private class CharacterArea {

        private String mCharacters;
        private RectF mSpace;
        private int mBgColor;
        private Paint mPaint = new Paint();
        private Paint mBgPaint = new Paint();
        private LinkedList<PointF> textCenters;

        public CharacterArea(float x, float y, float width, float height,
                             int bg_color) {
            this.mSpace = new RectF(x, y, x + width, y + height);
            mBgColor = bg_color;
            mBgPaint.setColor(mBgColor);
            mPaint.setTextSize(KeyboardHandler.default_font_size);
            mPaint.setColor(KeyboardHandler.default_font_color);
            mPaint.setFakeBoldText(true);
            mPaint.setTextAlign(Align.CENTER);
        }

        public boolean contains(PointF pt) {
            return mSpace.contains(pt.x, pt.y);
        }

        private void initCenters() {
            float width = Math.abs(mSpace.left - mSpace.right) / 3;
            float height = Math.abs(mSpace.top - mSpace.bottom) / 2;
            float x = mSpace.left;
            float y = mSpace.top;
            PointF center = Util.getTextCenterToDraw(
                    "" + mCharacters.charAt(0), new RectF(x, y, x + width, y
                            + height), mPaint
            );
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
            canvas.drawRect(mSpace, mBgPaint);

            int i = 0;
            if (mCharacters.length() == 6) {
                for (PointF center : textCenters) {
                    canvas.drawText("" + mCharacters.charAt(i), center.x,
                            center.y, mPaint);
                    ++i;
                }

            } else {
                PointF center = Util.getTextCenterToDraw(mCharacters, mSpace,
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