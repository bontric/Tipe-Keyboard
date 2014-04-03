package com.bontric.DoubleTabKeyboard;



import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;

import com.bontric.DoubleTab.R;

public class CanidateView extends View implements SpellCheckerSessionListener {
	private Context ctx;
		
	private SpellCheckerSession mScs;
	
	private SoftKeyboard mService;
	private Paint mPaint;
	
	private int screenWidth;
	private int screenHeight;
	
	private List<String> mSuggestions;
	private List<String> curSuggestions;
	private Rect suggestionsArea;
	
	
	public CanidateView(Context context) {
		super(context);
		ctx = context;
		// TODO Auto-generated constructor stub
		initView();
		setScreenDimen();
		initSpellCheckerSession();
		initPaint();
	}
	public CanidateView(Context context, AttributeSet attrs){
		super(context, attrs);
		ctx = context;
		// TODO Auto-generated constructor stub
		initView();
		setScreenDimen();
		initSpellCheckerSession();
		initPaint();
	}
	public CanidateView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		ctx = context;
		// TODO Auto-generated constructor stub
		initView();
		setScreenDimen();
		initSpellCheckerSession();
		initPaint();
	}
	
	@SuppressLint("NewApi")
	public void setScreenDimen() {
		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
	}
	
	
	
	//-----------------------------------------------
	// Things for the right handeling of the view
	//------------------------------------------------
	
	public void initView(){
	/*	setBackgroundColor(getResources().getColor(R.color.candidate_background));
		setHorizontalFadingEdgeEnabled(true);
        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
	*/
		setWillNotDraw(false);
		}
	
	
	@Override
    public int computeHorizontalScrollRange() {
        //TODO make better @jakob
		return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(50, widthMeasureSpec);
        
		final int desiredHeight = ((int)mPaint.getTextSize()) + 40 ;
        
	//	Log.d("Width", measuredWidth + "\t|\t" + desiredHeight);
        // Maximum possible width and desired height
        setMeasuredDimension(measuredWidth,
                resolveSize(desiredHeight, heightMeasureSpec));
    }
	
	
	/**
     * A connection back to the service to communicate with the text field
     * @param softKeyboard
     */
    public void setService(SoftKeyboard softKeyboard) {
        mService = softKeyboard;
    }
    
    public void clear() {
        mSuggestions.clear();
        //mTouchX = OUT_OF_BOUNDS;
        //mSelectedIndex = -1;
        invalidate();
    }
    
    
    public void drawSuggenstionsText(Canvas canvas, Paint paint, int y){
    	curSuggestions = new ArrayList<String>();
    	
    	if(mSuggestions.size()>= 3){
    /*		return mSuggestions.get(mSuggestions.size()-2) + "\t|\t" +
    		   	mSuggestions.get(mSuggestions.size()-1) + "\t|\t" +
    		   	mSuggestions.get(mSuggestions.size()-3);*/
    		curSuggestions.add(mSuggestions.get(mSuggestions.size()-1));
    		curSuggestions.add(mSuggestions.get(mSuggestions.size()-2));
    		curSuggestions.add(mSuggestions.get(mSuggestions.size()-3));
    		
    		canvas.drawText(curSuggestions.get(1), (float) (screenWidth/6 - 
    				paint.measureText(curSuggestions.get(1))*0.5)
    				, y, paint);
    		
    		canvas.drawText(curSuggestions.get(0), (float) (screenWidth/2 - 
					paint.measureText(curSuggestions.get(0))*0.5)
					, y, paint);
    		
    		canvas.drawText(curSuggestions.get(2), (float) (5*screenWidth/6 - 
					paint.measureText(curSuggestions.get(2))*0.5)
					, y, paint);
    		return;
    	}
    	
    	curSuggestions = new ArrayList<String>(mSuggestions);
    	if(mSuggestions.size()== 2){
    	/*	return mSuggestions.get(1) + "\t|\t" +
    		   	mSuggestions.get(0) ;*/
    		canvas.drawText(mSuggestions.get(0), (float) (screenWidth/4 - paint.measureText(mSuggestions.get(0))*0.5)
    				, y, paint);
    		canvas.drawText(mSuggestions.get(1), (float) (3*screenWidth/4 - paint.measureText(mSuggestions.get(1))*0.5)
    				, y, paint);
    	}
    	if(mSuggestions.size()== 1){
    		canvas.drawText(mSuggestions.get(0), (float) (screenWidth/2 - paint.measureText(mSuggestions.get(0))*0.5)
    				, y, paint);
    	}		
    }
    
    public void initPaint(){
    	mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.candidate_font_height));
        mPaint.setFakeBoldText(true);
        mPaint.setStrokeWidth(2);
        
        suggestionsArea = new Rect();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	// TODO Auto-generated method stub
    	if(canvas == null){
    		return;
    	}
    		//super.onDraw(canvas);
    
    	if (mSuggestions == null) return;
    //	String strToDraw = getSuggenstionsText();
    	
    	final int height = getHeight();
    	final int rectOffset = 40;
    	final int y = (int) (((height - mPaint.getTextSize())) - mPaint.ascent());
    //	float textWidth = mPaint.measureText(strToDraw);
    	
    	mPaint.setColor(Color.BLACK);	
    	suggestionsArea.set(0, (int) (height-mPaint.getTextSize()*1.5), screenWidth, height);
    	canvas.drawRect(0, (int) (height-mPaint.getTextSize()*1.5), screenWidth, height, mPaint);
    	mPaint.setColor(Color.WHITE);
    	
    	drawSuggenstionsText(canvas, mPaint, y);
    	//	canvas.drawText(strToDraw, textWidth/2, y-10, mPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me) {
    
    	int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
    	
        if(y > suggestionsArea.bottom || y < suggestionsArea.top || curSuggestions.size() == 0)
        	return true;
        
        String chosenSuggest;
        switch (curSuggestions.size()) {
			case 3:
				//So dirty
				if(x < suggestionsArea.right / 3)
					chosenSuggest = curSuggestions.get(1);
				else 
					if(x < suggestionsArea.right/3*2)
						chosenSuggest = curSuggestions.get(0);
					else
						chosenSuggest = curSuggestions.get(2);
				break;
			case 2:
				if(x > suggestionsArea.right/2)
					chosenSuggest = curSuggestions.get(0);
				else
					chosenSuggest = curSuggestions.get(1);
				break;
			case 1:
				chosenSuggest = curSuggestions.get(0);
				break;
			
			default:
				//Something strange happend
				Log.d("Tipe", "onTouchEvent: problem with size => " + curSuggestions.size() );
				return true;
		}
        curSuggestions.clear();
        mSuggestions.clear();
    	mService.chooseSuggestion(chosenSuggest);
    	return true;
    }
    
    
    //-----------------------------------------------------------------
    //
    // Word suggestion from spell checking
    //
    //-----------------------------------------------------------------
    public void initSpellCheckerSession(){
		final TextServicesManager tsm = (TextServicesManager) ctx.getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
		//TODO make choosable 
		//spellCheck = tsm.newSpellCheckerSession(null, Local.ENGLISH, this, false); 
        mScs = tsm.newSpellCheckerSession(null, null, this, true);
        mSuggestions = new ArrayList<String>();
	}

    public void stopSpellCheckerSession(){
    	if(mScs != null)
    		mScs.cancel();
    }
    
    @Override
	public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] arg0) {
		// TODO Auto-generated method stub
		
		 
		if (!isSentenceSpellCheckSupported()) {
            Log.e("TAG", "Sentence spell check is not supported on this platform, "
                    + "but accidentially called.");
            return;
        }
		mSuggestions.clear();
		
       // Log.d("TAG", "onGetSentenceSuggestions");
        //Get all the suggestions 
        //fewer might be sufficent 
        
        for (int i = 0; i < arg0.length; ++i) {
            final SentenceSuggestionsInfo ssi = arg0[i];
            for (int j = 0; j < ssi.getSuggestionsCount(); ++j) {            	
            	for(int c = 0; c < ssi.getSuggestionsInfoAt(j).getSuggestionsCount(); c++){
            		mSuggestions.add(ssi.getSuggestionsInfoAt(j).getSuggestionAt(c));
            		//Log.d("TAG sentence suggests", mSuggestions.get(mSuggestions.size()-1));
            	}
            }
        }
        invalidate();
        requestLayout();
	}
	
	private boolean isSentenceSpellCheckSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

	
	@Override
	public void onGetSuggestions(SuggestionsInfo[] arg0) {
		mSuggestions.clear();
		// TODO Auto-generated method stub
		//Log.d("TAG", "onGetSuggestions");
		// TODO get list string list 
        for (int i = 0; i < arg0.length; ++i) {
        	for(int c = 0; c < arg0[0].getSuggestionsCount(); c++){
        		mSuggestions.add(arg0[0].getSuggestionAt(c));
        //		Log.d("TAG sentence suggests", mSuggestions.get(mSuggestions.size()-1));
        	}
        }
        invalidate();
        requestLayout();
	}
	
	public void getSuggestionsForWord(String word){
		
		if (mScs != null) {
			mSuggestions.clear();
			
			if(!word.isEmpty())
				mScs.getSentenceSuggestions(new TextInfo[] {new TextInfo(
                    word)}, 3);
		}
	} 
	
	public boolean hasSuggestions(){
		return !mSuggestions.isEmpty();
	}
	
}
