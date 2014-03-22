
package com.bontric.DoubleTabKeyboard;

import android.content.Context;
import android.graphics.Point;
import android.inputmethodservice.Keyboard.Key;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class DoubleTabSwipeKeyboardView extends DoubleTabKeyboardView {
	
	private Point startPos = null;
	private Point endPos = null;
	
	public DoubleTabSwipeKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    int eventaction = event.getAction();
	    boolean gotKey;
	    gotKey = false;
	    
	    
	    
	    switch (eventaction) {
	        case MotionEvent.ACTION_DOWN: 
	            // finger touches the screen
	        //	if(this.levelDownState){
	        	
	        		startPos = getEventMedianPos(event);
	        //	}
	        	break;

	        case MotionEvent.ACTION_MOVE:
	            // finger moves on the screen
	        	break;

	        case MotionEvent.ACTION_UP:   
	        	// finger leaves the screen    
	        	
	        	if(startPos != null){
	        		endPos = getEventMedianPos(event);
	        		Point swipeVec = new Point();
	        		//TODO make controllable with a slider :)
	        		swipeVec.x = startPos.x + (int) ((endPos.x - startPos.x)*1.25);
	        		swipeVec.y = startPos.y + (int) ((endPos.y - startPos.y)*1.25);
	        		Log.d("UP", "SwipeX " + swipeVec.x +"\tSwipeY " +swipeVec.y );
	        		Key first = getKeyToPoint(swipeVec);
	        		if(first != null && first.codes[0] >= 0 && first.codes[0] <=35){
	        		
	        			gotKey = true;
	        			this.getOnKeyboardActionListener().onRelease(first.codes[0]);
	        			
	        		}
	        	}
	        	else{
	        		
	        		Point lastPos = getEventMedianPos(event);
	        		Key first = getKeyToPoint(lastPos);
	        		if(first == null)
	        			return true;
	        		
	        		this.getOnKeyboardActionListener().onKey(first.codes[0], first.codes);
	        	}
	        	
	        	levelDownState = false;
	        	invalidate();
	        	
	        	startPos = null;
	        	endPos 	 = null;
	            break;
	    }
	    
	 /*   if(this.levelDownState && !(moving || started) ){
    		this.levelDownState = false;
    		this.invalidate();
    		Point lastPos = getEventMedianPos(event);
    		if(getKeyToPoint(lastPos) == null)
    			return true;
    		
    		this.getOnKeyboardActionListener().onPress(getKeyToPoint(lastPos).codes[0]);
    	}*/ 
	    if(!gotKey)
	    	super.onTouchEvent(event);
	    // tell the system that we handled the event and no further processing is required
	    return true; 
	}

}
