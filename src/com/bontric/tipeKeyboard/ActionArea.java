package com.bontric.tipeKeyboard;



import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.util.Log;

public abstract class ActionArea {
	private Bitmap icon;
	private String label;
	private RectF mSpace;
	private Paint mPaint = new Paint();
	private int mBgColor;

	public ActionArea(float x, float y, float width, float height,
			int bg_color, String label) {
		this.mSpace = new RectF(x, y, x + width, y + height);
		mBgColor = bg_color;
		mPaint.setTextSize(KeyboardHandler.defaultFontSize);
		mPaint.setTextAlign(Align.CENTER);
		this.label = label;
	}

	public ActionArea(float x, float y, float width, float height,
			int bg_color, Bitmap icon) {
		this.mSpace = new RectF(x, y, x + width, y + height);
		mBgColor = bg_color;
		mPaint.setTextSize(KeyboardHandler.defaultFontSize);
		mPaint.setTextAlign(Align.CENTER);
		this.icon = icon;
	}

	public boolean contains(PointF pt) {
		return mSpace.contains(pt.x, pt.y);
	}

	public void draw(Canvas canvas) {
		Log.d("Main", "Drawing bg @ : X: " + mSpace.left + ",  ,Y: "
				+ mSpace.top);
		mPaint.setColor(mBgColor);
		canvas.drawRect(mSpace, mPaint);
		mPaint.setColor(KeyboardHandler.CharViewFontColor);
		if (icon != null) {
			int cx = (int) ((mSpace.width() - icon.getWidth()) / 2);
			int cy = (int) (mSpace.height() - icon.getHeight()) / 2;
			canvas.drawBitmap(icon, cx, cy, null);
		} else {
			PointF center = getTextCenterToDraw(label, mSpace, mPaint);
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

	private PointF getTextCenterToDraw(String text, RectF region, Paint paint) {
		// got this from stackoverflow
		Rect textBounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), textBounds);
		float x = region.centerX() - textBounds.width() * 0.4f;
		float y = region.centerY() + textBounds.height() * 0.4f;
		return new PointF(x, y);

	}

	public abstract void onTouch();
}
