package com.bontric.tipeKeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TapTapView extends TipeView {

    public TapTapView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        initKeyboardHandler(context);
    }

    public TapTapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initKeyboardHandler(context);
    }

    public TapTapView(Context context) {
        super(context);
        initKeyboardHandler(context);
    }

    @Override
    public void init() {

        ((TapTapCharacterView) findViewById(R.id.character_view)).init();
        ((LowerBarView) findViewById(R.id.lower_bar_view)).init();
        ((UpperBarView) findViewById(R.id.upper_bar_view)).init();
    }


}
