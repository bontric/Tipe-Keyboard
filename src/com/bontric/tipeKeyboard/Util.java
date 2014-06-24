/**
 *@name Util
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

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * @author koogle Class for some usefull definitions used across classes
 */
class Util {

    public static PointF getTextCenterToDraw(String text, RectF region,
                                             Paint paint) {
        //well this thing is bs
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float x = region.centerX();//- textBounds.width() * 0.4f;
        float y = region.centerY() + textBounds.height() * 0.4f;
        return new PointF(x, y);
    }

    // Calculates the median of all points of an motion event
    public static PointF getEventMedianPos(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        PointF medianPoint = new PointF();
        medianPoint.x = event.getX(0);
        medianPoint.y = event.getY(0);

        for (int c = 1; c < pointerCount; c++) {
            medianPoint.x = (medianPoint.x + event.getX(c)) / 2;
            medianPoint.y = (medianPoint.y + event.getY(c)) / 2;
        }
        return medianPoint;
    }

    public static boolean stringContainsSeperators(String str) {
        for (char s : KeyboardHandler.word_separators.toCharArray()) {
            if (str.contains(s + "")) {
                return true;
            }
        }
        return false;

    }

    /**
     * Returns the word surrounding the cursor by checking for word seperators
     *
     * @param beforeSel string before cursor
     * @param afterSel  current string after cursor
     * @return selected word
     */
    public static String getWordBetweenSeperators(String beforeSel, String afterSel) {

        int beginIndex = -1;
        int endIndex = afterSel.length();

        for (char c : KeyboardHandler.word_separators.toCharArray()) {
            if (beforeSel.length() > 0 && beforeSel.contains(c + "") && beforeSel.lastIndexOf(c) > beginIndex) {
                beginIndex = beforeSel.lastIndexOf(c);
            }
            if (afterSel.length() > 0 && afterSel.contains(c + "") && afterSel.indexOf(c) < endIndex) {
                endIndex = afterSel.indexOf(c);
            }
        }
        String a = beforeSel.substring(beginIndex + 1);
        String b = afterSel.substring(0, endIndex);

        return a + b;

    }


    public static int[] getBeforeAfterLength(String beforeSel, String afterSel) {
        /*
         * TODO this needs proper documentation
          *
         */
        int[] beforeAfterLength = new int[2];
        beforeAfterLength[0] = -1;
        beforeAfterLength[1] = afterSel.length();
        for (char c : KeyboardHandler.word_separators.toCharArray()) {
            if (beforeSel.length() > 0 && beforeSel.contains(c + "") && beforeSel.lastIndexOf(c) > beforeAfterLength[0]) {
                beforeAfterLength[0] = beforeSel.lastIndexOf(c);
            }
            if (afterSel.length() > 0 && afterSel.contains(c + "") && afterSel.indexOf(c) < beforeAfterLength[1]) {
                beforeAfterLength[1] = afterSel.indexOf(c);
            }
        }

        beforeAfterLength[0] = (beforeSel.length() - beforeAfterLength[0]);
        if (beforeAfterLength[0] > 0) {
            beforeAfterLength[0]--;
        }
        if (beforeAfterLength[0] == 0) {
            beforeAfterLength[0]++;
        }
        return beforeAfterLength;
    }
}
