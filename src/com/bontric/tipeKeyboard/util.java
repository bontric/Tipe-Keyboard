/**
 * 
 */
package com.bontric.tipeKeyboard;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * @author koogle
 * Class for some usefull definitions used across classes	
 *
 */
public class util {
	
	public static PointF getTextCenterToDraw(String text, RectF region,
			Paint paint) {

		Rect textBounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), textBounds);
		float x = region.centerX() - textBounds.width() * 0.4f;
		float y = region.centerY() + textBounds.height() * 0.4f;
		return new PointF(x, y);
	}
}
