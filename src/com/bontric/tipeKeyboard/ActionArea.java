/**
 *@name ActionArea
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

import android.graphics.*;
import android.graphics.Paint.Align;

public abstract class ActionArea {
    private Bitmap icon = null;
    private String label;
    private RectF mSpace;
    private Paint mPaint = new Paint();
    private Paint iconPaint = new Paint();
    private int mBgColor;

    public ActionArea(float x, float y, float width, float height,
                      int bg_color, String label) {
        this.mSpace = new RectF(x, y, x + width, y + height);
        mBgColor = bg_color;
        mPaint.setTextSize(KeyboardHandler.default_font_size);
        mPaint.setTextAlign(Align.CENTER);
        this.label = label;
        iconPaint = new Paint(KeyboardHandler.highlight_font_color);
        ColorFilter filter = new LightingColorFilter(KeyboardHandler.highlight_font_color, 1);
        iconPaint.setColorFilter(filter);

        mPaint.setFakeBoldText(true);
    }

    public ActionArea(float x, float y, float width, float height,
                      int bg_color, Bitmap icon) {
        this.mSpace = new RectF(x, y, x + width, y + height);
        mBgColor = bg_color;
        mPaint.setTextSize(KeyboardHandler.default_font_size);
        mPaint.setTextAlign(Align.CENTER);
        this.icon = icon;
        iconPaint = new Paint(KeyboardHandler.highlight_font_color);
        ColorFilter filter = new LightingColorFilter(KeyboardHandler.highlight_font_color, 1);
        iconPaint.setColorFilter(filter);

        mPaint.setFakeBoldText(true);

    }

    public boolean contains(PointF pt) {
        return mSpace.contains(pt.x, pt.y);
    }

    public void draw(Canvas canvas) {

        mPaint.setColor(mBgColor);
        canvas.drawRect(mSpace, mPaint);
        mPaint.setColor(KeyboardHandler.highlight_font_color);
        if (icon != null) {
            int cx = (int) ((mSpace.width() - icon.getWidth()) / 2);
            int cy = (int) (mSpace.height() - icon.getHeight()) / 2;
            canvas.drawBitmap(icon, mSpace.left + cx, mSpace.top + cy, iconPaint);
        } else {
            PointF center = Util.getTextCenterToDraw(label, mSpace, mPaint);
            canvas.drawText(label, center.x, center.y, mPaint);
        }

    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public abstract void onTouch();
}
