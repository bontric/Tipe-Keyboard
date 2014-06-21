/**
 *@name CandidateView
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Copyright  2014 Benedikt Wieder, Jakob Frick

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *
 */


package com.bontric.tipeKeyboard;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.textservice.*;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import com.bontric.tipeSettings.TipeSettings;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CandidateView extends View implements SpellCheckerSessionListener {

    private Context ctx;
    private SpellCheckerSession mScs;
    private InputHandler inputHandler;
    private List<String> mSuggestions;
    private List<String> curSuggestions;
    private RectF suggestionsArea = new RectF();
    private Paint mPaint;
    Paint seperatorPaint = new Paint();
    private float mHeight;
    private float screenWidth;
    private SharedPreferences sharedPref;
    int maxStrLength = 12;            //Size at which strings are shortend

    public CandidateView(Context context) {
        super(context);
        ctx = context;
        initView();
        setScreenDimen();
        initSpellCheckerSession();
        initPaint();
    }

    public CandidateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        initView();
        setScreenDimen();
        initSpellCheckerSession();
        initPaint();
    }

    public CandidateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ctx = context;
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
    }

    public void initView() {

        this.setBackgroundColor(KeyboardHandler.char_view_dark_color);
        curSuggestions = new ArrayList<String>();
        setWillNotDraw(false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void initPaint() {

        seperatorPaint.setStrokeWidth(5);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(KeyboardHandler.candidate_font_size);
        mPaint.setColor(sharedPref.getInt(TipeSettings.FONT_COLOR,
                Color.WHITE));
        mPaint.setTextAlign(Align.CENTER);

    }


    // Prepare the first 3 Suggestions text before drawing it
    public void prepareSuggestions() {
        curSuggestions.clear();
        if (mSuggestions.size() >= 3) {
            curSuggestions.add(mSuggestions.get(mSuggestions.size() - 1));
            curSuggestions.add(mSuggestions.get(mSuggestions.size() - 3));
            curSuggestions.add(mSuggestions.get(mSuggestions.size() - 2));
        } else
            curSuggestions = new ArrayList<String>(mSuggestions);
    }


    //Draw the suggestions into the view
    public void drawSuggenstionsText(Canvas canvas) {
        prepareSuggestions();
        //Copy current Suggestions and check for oversize
        List<String> tmpSuggstStrs = new ArrayList<String>();
        for (String singleStr : curSuggestions) {
            if (singleStr.length() > maxStrLength) {
                tmpSuggstStrs.add(singleStr.substring(0, maxStrLength - 3) + "...");
            } else
                tmpSuggstStrs.add(singleStr);
        }
        //Draw texts
        int i = 0;
        RectF tmpArea = new RectF();
        for (String string : tmpSuggstStrs) {
            tmpArea.set(suggestionsArea);
            tmpArea.left = screenWidth / tmpSuggstStrs.size() * i;
            tmpArea.right = screenWidth / tmpSuggstStrs.size() * (i + 1);
            if (i == 1) {
                mPaint.setColor(KeyboardHandler.highlight_font_color);
            } else {
                mPaint.setColor(KeyboardHandler.default_font_color);
            }
            PointF tCenter = Util.getTextCenterToDraw(string, tmpArea, mPaint);
            canvas.drawText(string, tCenter.x, tCenter.y, mPaint);
            i++;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mSuggestions == null || mSuggestions.size() == 0) return;


        seperatorPaint.setColor(KeyboardHandler.highlight_font_color);
        canvas.drawLine(getWidth() / 3f, 0, getWidth() / 3f, getHeight(), seperatorPaint);
        canvas.drawLine(2 * getWidth() / 3f, 0, 2 * getWidth() / 3f, getHeight(), seperatorPaint);
        seperatorPaint.setColor(KeyboardHandler.default_font_color);
        canvas.drawLine(0, getHeight(), screenWidth, getHeight(), seperatorPaint);
        //No suggestions nothing to do
        suggestionsArea.set(0, (int) (mHeight - mPaint.getTextSize() * 2),
                screenWidth, (int) mHeight);
        drawSuggenstionsText(canvas);
    }

    //Picks a suggestion on touch
    @Override
    public boolean onTouchEvent(MotionEvent me) {
        int x = (int) me.getX();
        int y = (int) me.getY();
        //Test if suggestion was hit
        if (y > suggestionsArea.bottom || y < suggestionsArea.top || curSuggestions.size() == 0)
            return true;

        switch (curSuggestions.size()) {
            // Calculate corresponding suggestions to x
            // TODO needs testing @jakob
            case 3:

                if (x < suggestionsArea.right / 3)
                    return pickSuggestions(0);
                else if (x < suggestionsArea.right / 3 * 2)
                    return pickSuggestions(1);
                else
                    return pickSuggestions(2);

            case 2:
                if (x > suggestionsArea.right / 2)
                    return pickSuggestions(0);
                else
                    return pickSuggestions(1);

            case 1:
                return pickSuggestions(0);


            default:
                //Something strange happend
                Log.d("Tipe", "onTouchEvent: problem with size => " + curSuggestions.size());
                return true;
        }
    }

    public boolean pickSuggestions(int index) {
        String chosenSuggest = curSuggestions.get(index);
        //Something was found, clear the old stuff
        curSuggestions.clear();
        mSuggestions.clear();
        //Tell Service what was picked
        inputHandler.getSuggestionFromCandView(chosenSuggest);
        return true;
    }



    // Word suggestion from spell checking

    public void initSpellCheckerSession() {
        final TextServicesManager tsm = (TextServicesManager) ctx.getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        //TODO make chosable

        mScs = tsm.newSpellCheckerSession(null, null, this, true);
        mSuggestions = new ArrayList<String>();
    }

    public void stopSpellCheckerSession() {
        if (mScs != null)
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
                for (int c = 0; c < ssi.getSuggestionsInfoAt(j).getSuggestionsCount(); c++) {
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
            for (int c = 0; c < arg0[0].getSuggestionsCount(); c++) {
                mSuggestions.add(arg0[0].getSuggestionAt(c));
                //		Log.d("TAG sentence suggests", mSuggestions.get(mSuggestions.size()-1));
            }
        }
        invalidate();
        requestLayout();
    }

    public void getSuggestionsForWord(String word) {
        //Request new suggestions for the current input

        if (mScs != null) {
            mSuggestions.clear();

            if (!word.isEmpty())
                mScs.getSentenceSuggestions(new TextInfo[]{new TextInfo(
                        word)}, 3);
        }
    }

    public boolean hasSuggestions() {
        return !mSuggestions.isEmpty();
    }

    public int count() {
        return mSuggestions.size();
    }

    @Override
    public int computeHorizontalScrollRange() {

        return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(50, widthMeasureSpec);

        mHeight = ((int) mPaint.getTextSize()) * 2;
        // Maximum possible width and desired height
        setMeasuredDimension(measuredWidth,
                resolveSize((int) mHeight, heightMeasureSpec));

    }


    /**
     * A connection back to the service to communicate with the text field
     */
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void clear() {
        mSuggestions.clear();
        invalidate();
    }

}