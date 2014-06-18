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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

import java.util.LinkedList;

public class ZoomCharacterView extends View {

    private LinkedList<CharacterArea> characterAreas;
    private int mWidth;
    private int mHeight;
    Paint seperatorPaint = new Paint();
    private boolean isZoom = false;
    private CharacterArea selected;

    public ZoomCharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ZoomCharacterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomCharacterView(Context context) {
        super(context);
    }

    /**
     * Initialize the view This Character View is programmed for a 6-key layout.
     */
    public void init() {
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
                selected.setChars(s.substring(1, s.length()));
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
        PointF touched = Util.getEventMedianPos(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                selected = getAreaFromTouch(touched);

                if (selected != null) {
                    isZoom = true;
                    longPressHandler.postDelayed(longPressActionRunnable,
                            longpressTimeout);

                    mLongPressedChar = selected.getCharFromPoint(touched);
                    this.invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // TODO @Ben just tamporary..
                if (KeyboardHandler.zoom_factor == 1) {
                    CharacterArea temp = getAreaFromTouch(Util.getEventMedianPos(event));
                    if (!selected.equals(temp)) {
                        selected = temp;
                    }
                    this.invalidate();
                }

                if (selected.getCharFromPoint(touched) != mLongPressedChar && !isLongPressed) {
                    longPressHandler.removeCallbacks(longPressActionRunnable);
                    longPressHandler.postDelayed(longPressActionRunnable,
                            longpressTimeout);
                    mLongPressedChar = selected.getCharFromPoint(touched);
                }
                break;
            case MotionEvent.ACTION_UP:
                isLongPressed = false;
                longPressHandler.removeCallbacks(longPressActionRunnable);
                if (isInBounds(Util.getEventMedianPos(event).x, Util.getEventMedianPos(event).y)) {
                    KeyboardHandler.input_connection.sendKey(selected.getCharFromPoint(Util.getEventMedianPos(event)));
                }else{
                    if(KeyboardHandler.space_leaving_char_area){
                        KeyboardHandler.input_connection.handleSpace();
                    }
                }
                selected = null;
                isZoom = false;
                this.setLevelUpChars();
                this.invalidate();
                break;

        }

        return true;
    }

    /**
     * check if the touch is out of borders ( we'll then get the min/max
     * values for x/y)
     * <p/>
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
        if (isZoom) {
            selected.drawZoom(canvas);
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
        private double zoomFactor = 2; // some value between 1 and 3
        private String mCharacters;
        private RectF mSpace;
        private RectF mZoomSpace;
        private int mBgColor;

        private Paint mPaint = new Paint();
        private Paint mBgPaint = new Paint();
        private Paint mZoomBgPaint = new Paint();

        private LinkedList<PointF> textCenters;

        public CharacterArea(float x, float y, float width, float height,
                             int bg_color) {
            this.mSpace = new RectF(x, y, x + width, y + height);
            mBgColor = bg_color;
            mBgPaint.setColor(mBgColor);
            mZoomBgPaint.setColor(mBgColor);
            mZoomBgPaint.setAlpha(230);
            mZoomBgPaint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
            mPaint.setTextSize(KeyboardHandler.default_font_size);
            mPaint.setColor(KeyboardHandler.default_font_color);
            mPaint.setFakeBoldText(true);
            mPaint.setTextAlign(Align.CENTER);
            zoomFactor = KeyboardHandler.zoom_factor;
        }

        public boolean contains(PointF pt) {
            return mSpace.contains(pt.x, pt.y);
        }

        private void initCenters(RectF area) {
            float width = Math.abs(area.left - area.right) / 3;
            float height = Math.abs(area.top - area.bottom) / 2;
            float x = area.left;
            float y = area.top;
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
            if (mCharacters != "") {
                initCenters(mSpace);

                canvas.drawRect(mSpace, mBgPaint);

                int i = 0;

                for (PointF center : textCenters) {
                    if (i < mCharacters.length()) {
                        canvas.drawText("" + mCharacters.charAt(i), center.x,
                                center.y, mPaint);
                    }
                    ++i;
                }
            }
        }

        public void drawZoom(Canvas canvas) {

            if (mZoomSpace == null) {
                float width = (float) (mSpace.width() * zoomFactor);
                float height;
                if (zoomFactor < 2) {
                    height = (float) (mSpace.height() * zoomFactor);
                } else {
                    height = (float) (mSpace.height() * 2);
                }
                if (mSpace.left == 0) {
                    if (mSpace.top == 0) {
                        //upper left area
                        mZoomSpace = new RectF(0, 0, width, height);
                    } else {
                        //lower left area
                        mZoomSpace = new RectF(0, mSpace.bottom - height, width, mSpace.bottom);
                    }


                } else if (mSpace.right == mWidth) {
                    if (mSpace.top == 0) {
                        // upper right area
                        mZoomSpace = new RectF(mSpace.right - width, 0, mSpace.right, height);
                    } else {
                        // upper left area
                        mZoomSpace = new RectF(mSpace.right - width, mSpace.bottom - height, mSpace.right, mSpace.bottom);
                    }
                } else {
                    if (mSpace.top == 0) {
                        //upper center area
                        mZoomSpace = new RectF(mSpace.centerX() - width / 2, 0, mSpace.centerX() + width / 2, height);
                    } else {
                        //lower center area
                        mZoomSpace = new RectF(mSpace.centerX() - width / 2, mSpace.bottom - height, mSpace.centerX() + width / 2, mSpace.bottom);
                    }

                }
            }

            if (mCharacters != "") {
                initCenters(mZoomSpace);
                mPaint.setAlpha(200);
                canvas.drawRoundRect(mZoomSpace, 50, 50, mPaint);
                mPaint.setAlpha(255);
                canvas.drawRoundRect(mZoomSpace, 50, 50, mZoomBgPaint);

                int i = 0;

                for (PointF center : textCenters) {
                    if (i < mCharacters.length()) {
                        canvas.drawText("" + mCharacters.charAt(i), center.x,
                                center.y, mPaint);
                    }
                    ++i;
                }
            }

        }

        public char getCharFromPoint(PointF touch) {
            PointF sel = null;
            double shortestDist = Math.sqrt(Math.pow(touch.x - textCenters.get(0).x, 2) + Math.pow(touch.y - textCenters.get(0).y, 2));
            for (PointF tC : textCenters) {
                double dist = Math.sqrt(Math.pow(touch.x - tC.x, 2) + Math.pow(touch.y - tC.y, 2));
                if (shortestDist > dist) {
                    sel = tC;
                    shortestDist = dist;
                }
            }
            if (sel == null || textCenters.indexOf(sel)>= mCharacters.length()) {
                return mCharacters.charAt(0);
            }
            return mCharacters.charAt(textCenters.indexOf(sel));
        }

        public String getChars() {
            return mCharacters;
        }

        public void setChars(String chars) {
            mCharacters = chars;

        }

        public boolean equals(CharacterArea a) {
            if(a == null){
                return false;
            }
            return this.mCharacters.compareTo(a.getChars()) == 0;
        }

    }
}