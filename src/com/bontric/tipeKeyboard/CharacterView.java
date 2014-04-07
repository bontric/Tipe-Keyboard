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
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CharacterView extends View {

	private LinkedList<CharacterArea> characterAreas;
	private PointF touchStartPoint;

	public CharacterView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public CharacterView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CharacterView(Context context) {
		super(context);
	}

	/**
	 * initalize the view This Character View is programmed for a 6-key layout.
	 */
	public void init() {
		this.getLayoutParams().height = KeyboardHandler.CHARACTER_VIEW_HEIGHT;
		initCharAreas();
	}

	/**
	 * initalize the character areas this is hard coded for 6 areas! ( due to
	 * readability)
	 */
	private void initCharAreas() {
		characterAreas = new LinkedList<CharacterArea>();
		float x = getX();
		float y = getY();
		float width = getWidth() / 3;
		float height = getHeight() / 2;

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

		setLevelUpChars(KeyboardHandler.CharacterSet);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("Main", "PING!");
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchStartPoint = getEventMedianPos(event);
			CharacterArea pressed = getAreaFromTouch(touchStartPoint.x, touchStartPoint.y);
			if(pressed!= null){
				setLevelDownChars(pressed.getChars());
				this.invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			initCharAreas();
			this.invalidate();
			break;
		}
	
		return true;
	}

	/**
	 * 
	 * @return null if touch is out of the Characterview Area!
	 */
	private CharacterArea getAreaFromTouch(float x, float y) {
		for (CharacterArea cA : characterAreas) {
			if (cA.contains(x, y)) {
				return cA;
			}
		}
		return null;
	}

	// Calculates the median of all points of an motion event
	public PointF getEventMedianPos(MotionEvent event) {
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

	// =================From here on downwards drawing related things===========
	/**
	 * 
	 * @param charset
	 *            this expects the charset to have 36 characters everything else
	 *            will! lead to bugs
	 */
	private void setLevelUpChars(String charset) {
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
		/*
		 * The first time the draw is painted. it won't show anything. this is
		 * due to the IME lifecycle (The first time ini() is called
		 * this.getwidth/height will return 0!) switch keyboard and back within
		 * a textfield to reproduce this. We'll need to find a propper fix fpr
		 * that!
		 */
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
			this.mCharacters = mCharacters;
			mBgColor = bg_color;

		}

		public boolean contains(float x, float y) {
			return mSpace.contains(x, y);
		}

		private void initCenters() {
			float width = Math.abs(mSpace.left - mSpace.right) / 3;
			float height = Math.abs(mSpace.top - mSpace.bottom) / 2;
			float x = mSpace.left;
			float y = mSpace.top;
			PointF center = getTextCenterToDraw(mCharacters, new RectF(x, y, x
					+ width, y + height), mPaint);
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
			// mPaint.setTextAlign(Align.CENTER);
			mPaint.setTextSize(KeyboardHandler.CharViewFontSize);
			int i = 0;
			if (mCharacters.length() == 6) {
				for (PointF center : textCenters) {
					canvas.drawText("" + mCharacters.charAt(i), center.x,
							center.y, mPaint);
					++i;
				}

			} else {
				PointF center = getTextCenterToDraw(mCharacters, mSpace, mPaint);
				canvas.drawText(mCharacters, center.x, center.y, mPaint);
			}
		}

		private PointF getTextCenterToDraw(String text, RectF region,
				Paint paint) {
			// got this from stackoverflow
			Rect textBounds = new Rect();
			paint.getTextBounds(text, 0, text.length(), textBounds);
			float x = region.centerX() - textBounds.width() * 0.4f;
			float y = region.centerY() + textBounds.height() * 0.4f;
			return new PointF(x, y);

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