/**
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
}
