/**
 *@name TipeCharacterView
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Copyright  2014 Benedikt Wieder, Jakob Frick

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

public class TipeCharacterView extends View {

    private LinkedList<CharacterArea> characterAreas;
    private PointF touchStartPoint;
    private int mWidth;
    private int mHeight;
    Paint seperatorPaint = new Paint();

    public TipeCharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TipeCharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TipeCharacterView(Context context) {
        super(context);
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
        double sensitivity = 1.2;
        PointF pressedPoint = Util.getEventMedianPos(event);
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

                PointF touchEndPoint = Util.getEventMedianPos(event);
                PointF swipeVec = new PointF();
            /*
             * extend swipe vector
			 */
                swipeVec.x = touchStartPoint.x
                        + (int) ((touchEndPoint.x - touchStartPoint.x) * sensitivity);
                swipeVec.y = touchStartPoint.y
                        + (int) ((touchEndPoint.y - touchStartPoint.y) * sensitivity);
            /*
             * if the vector extension brings the touch out of TipeCharacterView
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
				 * If activated :  when you release your finger
				 * outside the Character View you'll send a space to the
				 * inputConnection
				 */
                    if(KeyboardHandler.space_leaving_char_area){
                    KeyboardHandler.input_connection.handleSpace();
                    }
                }
                setLevelUpChars();
                this.invalidate();
                break;
        }

        return true;
    }
    
    /**
     * check if the touch is out of borders ( we'll then get the min/max
     * values for x/y)
     *
     * => The touch point is relative to this view. While this.getX()/getY()
     * is relative to the Layout(TipeView)
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


            //canvas.drawRect(mSpace, seperatorPaint);
            //canvas.drawRect(mSpace.left+1,mSpace.top+1,mSpace.right-1,mSpace.bottom-1, mBgPaint);
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