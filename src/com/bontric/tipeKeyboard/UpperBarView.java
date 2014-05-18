/**
 *@name UpperBarView
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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class UpperBarView extends View {
    private ActionArea symButton;
    private ActionArea enterButton;

    public UpperBarView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init();
    }

    public UpperBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UpperBarView(Context context) {
        super(context);
        init();
    }

    public void init() {
        this.setBackgroundColor(KeyboardHandler.background_color);
        LayoutParams params = new LinearLayout.LayoutParams(
                KeyboardHandler.keyboard_width,
                KeyboardHandler.upper_bar_view_height);
        this.setLayoutParams(params);

        int width = params.width;
        int height = params.height;

        symButton = new ActionArea(0, 0, width / 3, height,
                KeyboardHandler.background_color, "Sym") {

            @Override
            public void onTouch() {
                KeyboardHandler.handleSym();
                if (KeyboardHandler.current_charset.equals(KeyboardHandler.symbol_set)) {
                    setLabel("qwe");
                } else {
                    setLabel("Sym");
                }
            }
        };

        Bitmap tempIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.sym_keyboard_return);

        enterButton = new ActionArea(2 * width / 3, 0, width / 3, height,
                KeyboardHandler.background_color, tempIcon) {
            @Override
            public void onTouch() {
                KeyboardHandler.input_connection.handleEnter();

            }
        };

    }

    @SuppressLint("NewApi")
    public void onDraw(Canvas canvas) {
        symButton.draw(canvas);
        enterButton.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF touched = Util.getEventMedianPos(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (symButton.contains(touched)) {
                    symButton.onTouch();
                } else if (enterButton.contains(touched)) {
                    enterButton.onTouch();
                }
                break;
        }

        return true;
    }

}
