/*
 * @author Jakob Frick
 */
package com.bontric.DoubleTabKeyboard;

import com.bontric.DtSettings.DtSettingsMain;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.inputmethodservice.Keyboard.Key;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DoubleTabSwipeKeyboardView extends DoubleTabKeyboardView {

	private Point startPos = null;
	private Point endPos = null;
	SharedPreferences sharedPref;
	
	public DoubleTabSwipeKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();
		boolean gotKey;
		gotKey = false;
		double sensitivity = 1 + 0.25 * sharedPref.getFloat(
				DtSettingsMain.swipeSensitivity, (float) 1);

		switch (eventaction) {
		case MotionEvent.ACTION_DOWN:
			// finger touches the screen
			startPos = getEventMedianPos(event);
			break;

		case MotionEvent.ACTION_MOVE:
			// finger moves on the screen
			break;

		case MotionEvent.ACTION_UP:
			// finger leaves the screen

			if (startPos != null) {
				endPos = getEventMedianPos(event);
				Point swipeVec = new Point();
				// TODO make controllable with a slider :)
				swipeVec.x = startPos.x
						+ (int) ((endPos.x - startPos.x) * sensitivity);
				swipeVec.y = startPos.y
						+ (int) ((endPos.y - startPos.y) * sensitivity);
				Key first = getKeyToPoint(swipeVec);
				if (first != null && first.codes[0] >= 0
						&& first.codes[0] <= 35) {

					gotKey = true;
					this.getOnKeyboardActionListener()
							.onRelease(first.codes[0]);

				}
			} else {

				Point lastPos = getEventMedianPos(event);
				Key first = getKeyToPoint(lastPos);
				if (first == null)
					return true;

				this.getOnKeyboardActionListener().onKey(first.codes[0],
						first.codes);
			}

			levelDownState = false;
			invalidate();

			startPos = null;
			endPos = null;
			break;
		}

		if (!gotKey) {
			super.onTouchEvent(event);
		}
		// tell the system that we handled the event and no further processing
		// is required
		return true;
	}
}
	
