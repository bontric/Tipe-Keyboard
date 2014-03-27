/*
 * @author Jakob Frick
 */
package com.bontric.DoubleTabKeyboard;

import com.bontric.DtSettings.DtSettingsMain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class DoubleTabSwipeKeyboardView extends DoubleTabKeyboardView {
	private boolean isNonCharacterKey = false;
	private Point startPos = null;
	private Point endPos = null;
	SharedPreferences sharedPref;

	public DoubleTabSwipeKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

	}

	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/**
		 * Definitions for sliding behavior: swiping off a non-character key to
		 * a character key or an empty area => do nothing
		 * 
		 * (Delete Key IS an exception since it is Long-pressable!
		 * 
		 * swiping off a non-character key to a non-character key => interpret
		 * end position
		 * 
		 * swiping off a character key to a non-character key => do nothing
		 * 
		 * Any touch related behavior using swipe not defined here is considered
		 * a bug. In which case it should be reported (or fixed)
		 */
		int eventaction = event.getAction();
		boolean gotKey;
		gotKey = false;
		double sensitivity = 1 + 0.25 * sharedPref.getFloat(
				DtSettingsMain.swipeSensitivity, (float) 1);

		switch (eventaction) {
		case MotionEvent.ACTION_DOWN:
			startPos = getEventMedianPos(event);

			Key k = getKeyToPoint(startPos);
			if (k != null && k.codes[0] < 0) {
				isNonCharacterKey = true;
			}
			break;

		case MotionEvent.ACTION_MOVE:

			break;

		case MotionEvent.ACTION_UP:
			if (startPos != null) {
				endPos = getEventMedianPos(event);
				if (getKeyToPoint(endPos) == null
						|| !(getKeyToPoint(endPos).codes[0] >= 0 ^ isNonCharacterKey)) {
					Key startKey = getKeyToPoint(startPos);
					if (startKey != null && startKey.repeatable) {

						super.onTouchEvent(event);
					}
					startPos = null;
					endPos = null;
					isNonCharacterKey = false;
					levelDownState = false;
					invalidate();
					return true;
				}
				Point swipeVec = new Point();
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
			isNonCharacterKey = false;
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
