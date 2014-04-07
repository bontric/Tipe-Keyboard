package com.bontric.tipeKeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class TipeView extends LinearLayout {

	public TipeView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
		initKeyboardHandler(context);
	}

	public TipeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initKeyboardHandler(context);
	}

	public TipeView(Context context) {
		super(context);
		initKeyboardHandler(context);
	}

	public void init() {
		((CharacterView) findViewById(R.id.character_view)).init();
	}

	private void initKeyboardHandler(Context context) {
		KeyboardHandler.init(context);

	}
}
