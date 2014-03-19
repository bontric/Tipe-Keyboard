package com.bontric.DoubleTabKeyboard;


import com.bontric.DoubleTab.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class DoubleTabKeyboardView extends KeyboardView {
	private String charset;
	private boolean levelDownState;
	private Paint paint;
	private int pressedKey;

	public DoubleTabKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	
	}

	public DoubleTabKeyboardView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	public void init(String charset) {
		this.charset = charset;
		this.levelDownState = false;
		this.paint = new Paint();
		paint.setTextSize(40);//murks
		paint.setColor(Color.WHITE);//murks
		paint.setFakeBoldText(true);
		paint.setTextAlign(Align.CENTER);
		this.pressedKey = 0;// key that was pressed before switch to lower layer
	}

	public boolean getLevelDownState() {
		/*
		 * returns weather we're on first (false) or second(true) layer
		 */

		return this.levelDownState;
	}

	public void setLevelDownState(boolean b) {
		/*
		 * sets weather we're on first (false) or second(true) layer
		 */
		this.levelDownState = b;
	}

	public void setPressedKey(int primaryCode) {
		this.pressedKey = primaryCode;
	}

	public int getCharCode(int primaryCode) {
		/*
		 * this returns which button was pressed relative to "pressedKey" Call
		 * this in "layerDown" state ONLY!
		 */
		return (pressedKey / 6) * 6 + (primaryCode / 6);
	}

	public void levelUp(Canvas canvas) {
		/*
		 * draw upper level of keyboard (contains all characters of charset)
		 */
		drawBackgrounds(canvas);
		int i = 0;
		for (Key key : this.getKeyboard().getKeys()) {
			if (key.codes[0] >= 0 && key.codes[0] < charset.length()) {
				String label = "" + charset.charAt(key.codes[0]);
				PointF center = getTextCenterToDraw(label, new RectF(key.x,
						key.y, key.x + key.width, key.y + key.height), paint);
				canvas.drawText(label, center.x, center.y, paint);
				++i;
			}
			if (i >= charset.length()) {
				break;
			}

		}
	}

	public void levelDown(Canvas canvas) {
		/*
		 * Set pressed area before invalidating the view! (setPressedArea(int
		 * primaryCode) )
		 */
		// I know this shouldn't be hard coded.. but it's 6 areas for now ;)
		drawBackgrounds(canvas);
		Key key = this.getKeyboard().getKeys().get(11);// shoot me for this..
		for (int i = (pressedKey / 6) * 6; i < (pressedKey / 6) * 6 + 6; ++i) {

			String label = "" + charset.charAt(i);
			PointF center = getTextCenterToDraw(label, new RectF(3
					* ((i % 6) % 3) * key.width,
					2 * ((i % 6) / 3) * key.height, 3 * ((i % 6) % 3)
							* key.width + 3 * key.width, 2 * ((i % 6) / 3)
							* key.height + 2 * key.height), paint);

			canvas.drawText(label, center.x, center.y+key.height, paint);

		}

	}

	private void drawBackgrounds(Canvas canvas) {
		/*
		 * draw background of charset
		 */
		Paint bgPaint = new Paint();

		for (Key key : this.getKeyboard().getKeys()) {
			if (key.codes[0] > -1) {

				if ((key.codes[0] / 6) % 2 == 0) {
					bgPaint.setColor(this.getResources().getColor(
							R.color.backgroundDark)); // Gets the color
														// specified
														// in res/values/colors
														// ..
														// could me more modular
														// though :)
					canvas.drawRect(key.x, key.y, key.x + key.width, key.y
							+ key.height, bgPaint);

				} else {
					bgPaint.setColor(this.getResources().getColor(
							R.color.backgroundLight));
					;// Gets the color specified ind res/values/colors .. could
						// me
						// more modular though :)
					canvas.drawRect(key.x, key.y, key.x + key.width, key.y
							+ key.height, bgPaint);

				}
			}
			
		}

	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (levelDownState) {
			levelDown(canvas);
		} else {
			levelUp(canvas);
		}
	}

	public static PointF getTextCenterToDraw(String text, RectF region,
			Paint paint) {
		/*
		 * returns a point which holds the coorinates to center given text
		 * within a region -> center.draw()
		 */
		Rect textBounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), textBounds);
		float x = region.centerX() - textBounds.width() * 0.4f;
		float y = region.centerY() + textBounds.height() * 0.4f;
		return new PointF(x, y);
	}

	public void setCharset(String cs) {
		this.charset = cs;
	}
	public String getCharset(){
		return this.charset;
	}
}
