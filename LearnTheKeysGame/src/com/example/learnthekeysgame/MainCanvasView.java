package com.example.learnthekeysgame;

import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;


public class MainCanvasView extends EditText {
	private Paint paint;
	private Point currentPos;
	private double charSpeed;
	private int screenWidth;
	private int screenHeight;
	private int points;
	private boolean toDraw;
	private char currentChar;
	private Context cxt;

	public boolean isToDraw() {
		return toDraw;
	}

	public void setToDraw(boolean toDraw) {
		this.toDraw = toDraw;
	}

	public MainCanvasView(Context context) {
		super(context);
		cxt = context;
		setScreenDimen();
		initPaint();
		charSpeed = 1;
		points = 0;
		resetPos(false);
	}
	
	public MainCanvasView(Context context, AttributeSet attrs ) {
		super(context, attrs);
		cxt = context;
		setScreenDimen();
		initPaint();
		charSpeed = 1;
		points = 0;
		resetPos(false);
		
	}
	
	@SuppressLint("NewApi")
	public void setScreenDimen(){
		WindowManager wm = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
		}
	
	public void initPaint(){
		//Setup paint TODO make settable
		this.paint = new Paint();
		this.setBackgroundColor( Color.BLACK);
		paint.setTextSize(getResources().getDimension(com.example.learnthekeysgame.R.dimen.font_height));
		paint.setColor(Color.WHITE);
		paint.setFakeBoldText(true);
		paint.setTextAlign(Align.CENTER);
	}
	
	public void resetPos(boolean success){
		Random r = new Random();
		int posX = r.nextInt(screenWidth-40)+20;
		currentPos = new Point(posX, 0);
		if(success)
			charSpeed = charSpeed * 1.2 ;
		//toDraw = false;
		startNewChar();
	}
	
	public void startNewChar(){
		Random r = new Random();
		currentChar = (char) (r.nextInt(122-97)+97);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		//super.onDraw(canvas);
		initPaint();
		if(!toDraw)
			return;
		//Drawboundaries
		paint.setAlpha(40);
		paint.setColor(Color.YELLOW);
		canvas.drawRect(0, (int) (screenHeight*0.5)-250, screenWidth,  (int) (screenHeight*0.5)-200, paint);
		
		paint.setColor(Color.WHITE);
		paint.setAlpha(255);
		canvas.drawText("" + currentChar, currentPos.x, currentPos.y+=(int) charSpeed, paint);	
		if(currentPos.y > screenHeight*0.5 -100){
			resetPos(false);
			
		}
		
		//TODO internationalisieren + better color
		paint.setAlpha(124);
		paint.setColor(Color.RED);
		canvas.drawText("Punkte " + points, screenWidth-100, 50, paint);	
	}

}
