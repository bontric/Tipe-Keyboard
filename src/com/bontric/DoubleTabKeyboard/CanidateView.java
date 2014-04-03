package com.bontric.DoubleTabKeyboard;



import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.os.Build;
import android.preference.PreferenceManager;
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
import com.bontric.DtSettings.DtSettingsMain;

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
	
	private SharedPreferences sharedPref;
	
	
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
	// Things for the right handleing of the view
	//------------------------------------------------
	
	public void initView(){
		curSuggestions = new ArrayList<String>();
		setWillNotDraw(false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
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
    
    public void initPaint(){
    	mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.candidate_font_height));
        mPaint.setColor(sharedPref.getInt(DtSettingsMain.normFontColor,
				Color.WHITE));
        mPaint.setFakeBoldText(true);
        mPaint.setTextAlign(Align.CENTER);
        
        suggestionsArea = new Rect();
    }
    
    // Prepare the first 3 Suggestions text before drawing it
    public void prepareSuggestions(){
    	curSuggestions.clear();
    	if(mSuggestions.size() >= 3){
    		curSuggestions.add(mSuggestions.get(mSuggestions.size()-1));
    		curSuggestions.add(mSuggestions.get(mSuggestions.size()-2));
    		curSuggestions.add(mSuggestions.get(mSuggestions.size()-3));
    	}
    	else 
    		curSuggestions = new ArrayList<String>(mSuggestions);
    }
    
    
    //Draw the suggestions into the view
    public void drawSuggenstionsText(Canvas canvas){
    	prepareSuggestions();
    	
    
    	//Copy current Suggestions and check for oversize
       	int maxStrLength = 8; 			//Size at which strings are shortend
        
    	List<String> tmpSuggstStrs = new ArrayList<String>();
    	for (String singleStr : curSuggestions) {
			if(singleStr.length() > maxStrLength){
				tmpSuggstStrs.add(singleStr.substring(0, maxStrLength) + "...");
			}
			else 
				tmpSuggstStrs.add(singleStr);
		}
    	
    	//Draw texts
    	int i = 0;
    	RectF tmpArea = new RectF();
    	int colors[] = { sharedPref.getInt(
				DtSettingsMain.darkBgColor, Color.BLACK), 
				sharedPref.getInt(
						DtSettingsMain.lightBgColor, Color.GRAY) };
    	int colorPick = 0;
    	
    	for (String string : tmpSuggstStrs) {
    		//Calc to box for the text
    		tmpArea.set(suggestionsArea);
    		tmpArea.left =   screenWidth/tmpSuggstStrs.size() * i;
    		tmpArea.right =  screenWidth/tmpSuggstStrs.size() * (i+1);
    		
    		//Color switches with every box
    		//TODO needs testing for a good ui
    		//mPaint.setColor(colors[(colorPick++)%2]);
    		mPaint.setColor(colors[1]);
    		
    		canvas.drawRect(tmpArea, mPaint);
    		
    		//TODO Might come in handy to export to a seperate utils file @jakob -> @ben
    		mPaint.setColor(sharedPref.getInt(DtSettingsMain.normFontColor,
    				Color.WHITE));
    		PointF textP = DoubleTabKeyboardView.getTextCenterToDraw(string, tmpArea, mPaint);
    		    
    		canvas.drawText(string, (float) (textP.x), textP.y, mPaint);
    		i++;
		}
    		
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	if(canvas == null){
    		return;
    	}
    	
    	//No suggestions nothing to do 
    	if (mSuggestions == null || mSuggestions.size() == 0) return;
    	
    	//Get view height
    	final int height = getHeight();
    	
    	//Draw background
    	mPaint.setColor(sharedPref.getInt(DtSettingsMain.darkBgColor, Color.BLACK));	
    	suggestionsArea.set(0, (int) (height-mPaint.getTextSize()*2), screenWidth, height);	
    	canvas.drawRect(suggestionsArea, mPaint);
    	
    	drawSuggenstionsText(canvas);
    }
    
    //Picks a suggestion on touch
    @Override
    public boolean onTouchEvent(MotionEvent me) {
    	
    	int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        //Test if suggestion was hit
        if(y > suggestionsArea.bottom || y < suggestionsArea.top || curSuggestions.size() == 0)
        	return true;
        
        String chosenSuggest;
        switch (curSuggestions.size()) {
        	// Calculate corresponding suggestions to x
        	//
			case 3:
				
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
        //Something was found, clear the old stuff
        curSuggestions.clear();
        mSuggestions.clear();
        //Tell Service what was picked
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
		//TODO make chosable 
		
		mScs = tsm.newSpellCheckerSession(null, null, this, true);
        mSuggestions = new ArrayList<String>();
	}

    public void stopSpellCheckerSession(){
    	if(mScs != null)
    		mScs.cancel();
    }
    
    @Override
	public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] arg0) {
		// Handle the suggestions gotten from the spell chekcer
		
		 
		if (!isSentenceSpellCheckSupported()) {
            Log.e("TAG", "Sentence spell check is not supported on this platform, "
                    + "but accidentially called.");
            return;
        }
		mSuggestions.clear();
		
        //Get all the suggestions 
        //fewer might be sufficent 
        
		//You have to parse all the information from all info to get single suggestions
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
		//Check if spell checker is supported
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

	
	@Override
	public void onGetSuggestions(SuggestionsInfo[] arg0) {
		mSuggestions.clear();
		
		//Get all the suggestions 
        //fewer might be sufficent 
        
		//You have to parse all the information from all info to get single suggestions
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
		//Request new suggestions for the current input
		
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
