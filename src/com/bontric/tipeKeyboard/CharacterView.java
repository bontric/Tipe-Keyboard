package com.bontric.tipeKeyboard;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CharacterView extends View {

	private LinkedList<CharacterArea> characterAreas;

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

		// +initalize the character areas (6)
		characterAreas = new LinkedList<CharacterArea>();
		int x = (int) getX();
		int y = (int) getY();
		int width = (int) getWidth() / 3;
		int height = (int) getHeight() / 2;
		characterAreas.add(new CharacterArea("Hallo1", x, y, width, height,
				Color.BLACK));
		characterAreas.add(new CharacterArea("Hallo2", x + width, y, width,
				height, Color.WHITE));
		characterAreas.add(new CharacterArea("Hallo3", x + 2 * width, y, width,
				height, Color.BLACK));
		characterAreas.add(new CharacterArea("Hallo4", x, y + height, width,
				height, Color.WHITE));
		characterAreas.add(new CharacterArea("Hallo5", x + width, y + height,
				width, height, Color.BLACK));
		characterAreas.add(new CharacterArea("Hallo6", x + 2 * width, y
				+ height, width, height, Color.WHITE));

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
		private Rect mSpace;
		private int mBgColor;

		public CharacterArea(String mCharacters, int x, int y, int width,
				int height, int bg_color) {
			this.mSpace = new Rect(x, y, x + width, y + height);
			this.mCharacters = mCharacters;
			mBgColor = bg_color;

		}

		public void draw(Canvas canvas) {
			Paint p = new Paint();
			p.setColor(mBgColor);
			canvas.drawRect(mSpace, p);
			p.setColor(Color.GREEN);
			canvas.drawText(mCharacters, mSpace.left, mSpace.top + 20, p);
		}
	}
}
