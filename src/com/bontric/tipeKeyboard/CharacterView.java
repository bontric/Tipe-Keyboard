/**
 *@name SoftKeyboard
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Do not try to understand this!
 * 
 * 
 *  
 */

package com.bontric.tipeKeyboard;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class CharacterView extends View {

	private LinkedList<CharacterArea> characterAreas;
	private PointF touchStartPoint;
	private int mWidth;
	private int mHeight;

	public CharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public CharacterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CharacterView(Context context) {
		super(context);
		init();
	}

	/**
	 * Initialize the view This Character View is programmed for a 6-key layout.
	 */
	public void init() {
		LayoutParams params = new LinearLayout.LayoutParams(
				KeyboardHandler.KEYBOARD_WIDTH,
				KeyboardHandler.CHARACTER_VIEW_HEIGHT);
		this.setLayoutParams(params);
		mWidth = params.width;
		mHeight = params.height;
		initCharAreas();
	}

	/**
	 * Initialize the character areas this is hard coded for 6 areas! ( due to
	 * readability)
	 * 
	 * @param viewHeight
	 * @param viewWidth
	 */
	private void initCharAreas() {

		// this.setBackgroundColor(Color.RED);
		characterAreas = new LinkedList<CharacterArea>();
		float x = 0;
		float y = 0;
		float width = mWidth / 3;
		float height = mHeight / 2;

		characterAreas.add(new CharacterArea(x, y, width, height,
				KeyboardHandler.CharViewDarkColor));
		characterAreas.add(new CharacterArea(x + width, y, width, height,
				KeyboardHandler.CharViewLightColor));
		characterAreas.add(new CharacterArea(x + 2 * width, y, width, height,
				KeyboardHandler.CharViewDarkColor));
		characterAreas.add(new CharacterArea(x, y + height, width, height,
				KeyboardHandler.CharViewLightColor));
		characterAreas.add(new CharacterArea(x + width, y + height, width,
				height, KeyboardHandler.CharViewDarkColor));
		characterAreas.add(new CharacterArea(x + 2 * width, y + height, width,
				height, KeyboardHandler.CharViewLightColor));

		setLevelUpChars();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		double sensitivity = 1.2;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchStartPoint = util.getEventMedianPos(event);
			CharacterArea pressed = getAreaFromTouch(touchStartPoint);
			if (pressed != null) {
				setLevelDownChars(pressed.getChars());
				this.invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			PointF touchEndPoint = util.getEventMedianPos(event);
			PointF swipeVec = new PointF();
			/*
			 * extend swipe vector by customizable factor (sensitivity)
			 */
			swipeVec.x = touchStartPoint.x
					+ (int) ((touchEndPoint.x - touchStartPoint.x) * sensitivity);
			swipeVec.y = touchStartPoint.y
					+ (int) ((touchEndPoint.y - touchStartPoint.y) * sensitivity);
			/*
			 * if the vector extension brings the touch out of CharacterrView
			 * We'll just interpret the release point!
			 */
			CharacterArea released = null;
			if (isInBounds(swipeVec.x, swipeVec.y)) {
				released = getAreaFromTouch(swipeVec);
			} else if (isInBounds(touchEndPoint.x, touchEndPoint.y)) {
				released = getAreaFromTouch(touchEndPoint);

			}
			if (released != null) {
				/*
				 * make sure this only sends one character a time..
				 */
				KeyboardHandler.inputConnection.sendKey(released.getChars()
						.charAt(0));
			} else {
				/*
				 * this is just for testing! when you release your finger
				 * outside the Character View you'll send a space to the
				 * inputConnection
				 */
				KeyboardHandler.inputConnection.handleSpace();
			}
			setLevelUpChars();
			this.invalidate();
			break;
		}

		return true;
	}

	private boolean isInBounds(float x, float y) {
		/**
		 * check if the touch is out of borders ( we'll then get the min/max
		 * values for x/y)
		 * 
		 * => The touch point is relative to this view. While this.getX()/getY()
		 * is relative to the Layout(TipeView)
		 */
		Log.d("Main", "width= " + getWidth() + " height= " + this.getHeight()
				+ " X: " + x + " Y: " + y + " mY: " + getY() + " mX" + getX());
		if (x <= 0 || x >= this.getWidth() || y <= 0 || y >= this.getHeight()) {
			Log.d("Main", "PING");
			return false;
		}
		return new RectF(0, 0, this.getWidth(), this.getHeight()).contains(x,
				y);
	}

	/**
	 * 
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
	/**
	 * 
	 * @param charset
	 *            this expects the charset to have 36 characters everything else
	 *            will! lead to bugs
	 */
	private void setLevelUpChars() {
		/*
		 * check this if you need more symbol set's
		 */
		String charset = (KeyboardHandler.isSymbolSet ? KeyboardHandler.SymbolSet
				: KeyboardHandler.CharacterSet);
		for (int i = 0; i < 6; ++i) {
			characterAreas.get(i).setChars(
					charset.substring(i * 6, (i + 1) * 6));
		}
	}

	private void setLevelDownChars(String charset) {

		for (int i = 0; i < charset.length() && i < characterAreas.size(); ++i) {
			characterAreas.get(i).setChars("" + charset.charAt(i));
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/*
		 * The first time the draw is painted. it won't show anything. this is
		 * due to the IME lifecycle (The first time init() is called
		 * this.getwidth/height will return 0!) switch keyboard and back within
		 * a textfield to reproduce this. We'll need to find a proper fix for
		 * that!
		 */
		if (KeyboardHandler.charsetChanged) {
			setLevelUpChars();
			KeyboardHandler.charsetChanged = false;
			// tell the Keyboard handler that we handled shift.
		}

		for (CharacterArea ca : characterAreas) {
			ca.draw(canvas);
		}
	}

	private class CharacterArea {
		private String mCharacters;
		private RectF mSpace;
		private int mBgColor;
		private Paint mPaint = new Paint();
		private LinkedList<PointF> textCenters;

		public CharacterArea(float x, float y, float width, float height,
				int bg_color) {
			this.mSpace = new RectF(x, y, x + width, y + height);
			mBgColor = bg_color;
			mPaint.setTextSize(KeyboardHandler.defaultFontSize);

		}

		public boolean contains(PointF pt) {
			return mSpace.contains(pt.x, pt.y);
		}

		private void initCenters() {
			float width = Math.abs(mSpace.left - mSpace.right) / 3;
			float height = Math.abs(mSpace.top - mSpace.bottom) / 2;
			float x = mSpace.left;
			float y = mSpace.top;
			PointF center = util.getTextCenterToDraw("" + mCharacters.charAt(0),
					new RectF(x, y, x + width, y + height), mPaint);
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

			mPaint.setColor(mBgColor);
			canvas.drawRect(mSpace, mPaint);
			mPaint.setColor(KeyboardHandler.CharViewFontColor);
			mPaint.setTextAlign(Align.CENTER);
			int i = 0;
			if (mCharacters.length() == 6) {
				for (PointF center : textCenters) {
					canvas.drawText("" + mCharacters.charAt(i), center.x,
							center.y, mPaint);
					++i;
				}

			} else {
				PointF center = util.getTextCenterToDraw(mCharacters, mSpace, mPaint);
				canvas.drawText(mCharacters, center.x, center.y, mPaint);
			}
		}


		public String getChars() {
			return mCharacters;
		}

		public void setChars(String chars) {
			mCharacters = chars;
			initCenters();
		}
	}
}