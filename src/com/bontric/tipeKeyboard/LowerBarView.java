/**
 *@name LowerBarView
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class LowerBarView extends View {
    private ActionArea shiftButton;
    private ActionArea spaceButton;
    private ActionArea deleteButton;

    public LowerBarView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init();
    }

    public LowerBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LowerBarView(Context context) {
        super(context);
        init();
    }

    public void init() {
        this.setBackgroundColor(KeyboardHandler.background_color);
        LayoutParams params = new LinearLayout.LayoutParams(
                KeyboardHandler.keyboard_width,
                KeyboardHandler.lower_bar_view_height);
        this.setLayoutParams(params);

        int width = params.width;
        int height = params.height;
        Bitmap tempIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.sym_keyboard_shift);

        shiftButton = new ActionArea(0, 0, width / 3, height,
                KeyboardHandler.background_color, tempIcon) {

            @Override
            public void onTouch() {
                if (!KeyboardHandler.is_symbol_set) {
                    KeyboardHandler.shift_state = !KeyboardHandler.shift_state;
                }
                KeyboardHandler.handleShift();

            }
        };

        tempIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.sym_keyboard_space);

        spaceButton = new ActionArea(width / 3, 0, width / 3, height,
                KeyboardHandler.background_color, tempIcon) {
            @Override
            public void onTouch() {
                KeyboardHandler.input_connection.handleSpace();

            }


        };

        tempIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.sym_keyboard_delete);
        deleteButton = new ActionArea(2 * width / 3, 0, width / 3, height,
                KeyboardHandler.background_color, tempIcon) {
            @Override
            public void onTouch() {
                KeyboardHandler.input_connection.handleDelete();

            }
        };
        this.invalidate();
    }

    @SuppressLint("NewApi")
    public void onDraw(Canvas canvas) {
        shiftButton.draw(canvas);
        spaceButton.draw(canvas);
        deleteButton.draw(canvas);
    }

	/*
     * ###############################################################
	 * Unflexible longpress inmplementation TODO move this to Action area implementation!
	 */

    private Runnable longPressDeleteRunnable = new Runnable() {
        public void run() {
            deleteButton.onTouch();
            longPressHandler.postDelayed(longPressDeleteRunnable,
                    repeatIntervall);
            wasLongPressed = true;

        }
    };
    private Runnable longPressSpaceRunnable = new Runnable() {
        public void run() {
            KeyboardHandler.input_connection.handleSpaceOnly();
            wasLongPressed = true;

        }
    };
    private Handler longPressHandler = new Handler();
    private final int longpressTimeout = 250; // final for now
    private final int repeatIntervall = 70;
    private boolean wasLongPressed = false;

	/*
     * ################################################################
	 */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF touch = Util.getEventMedianPos(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (deleteButton.contains(touch)) {
                    longPressHandler.postDelayed(longPressDeleteRunnable,
                            longpressTimeout);
                }
                if (spaceButton.contains(touch)) {
                    longPressHandler.postDelayed(longPressSpaceRunnable,
                            KeyboardHandler.longpress_timeout);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (deleteButton.contains(touch)) {
                    longPressHandler.removeCallbacks(longPressDeleteRunnable);
                    longPressHandler.postDelayed(longPressDeleteRunnable,
                            longpressTimeout);
                } else if (spaceButton.contains(touch)) {
                    longPressHandler.removeCallbacks(longPressSpaceRunnable);

                    longPressHandler.postDelayed(longPressSpaceRunnable,
                            KeyboardHandler.longpress_timeout);
                } else {
                    wasLongPressed = false;
                }
                break;
            case MotionEvent.ACTION_UP:

                longPressHandler.removeCallbacks(longPressDeleteRunnable);
                longPressHandler.removeCallbacks(longPressSpaceRunnable);
                if (wasLongPressed) {
                    wasLongPressed = false;
                    return true;
                }
                if (shiftButton.contains(touch)) {
                    shiftButton.onTouch();
                } else if (spaceButton.contains(touch)) {
                    spaceButton.onTouch();
                } else if (deleteButton.contains(touch)) {
                    deleteButton.onTouch();
                }
                break;
        }

        return true;
    }

}
