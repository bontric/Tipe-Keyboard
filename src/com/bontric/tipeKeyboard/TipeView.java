/**
 *@name TapTapCharacterView
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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class TipeView extends LinearLayout{

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

        ((TipeCharacterView) findViewById(R.id.character_view)).init();
        ((LowerBarView) findViewById(R.id.lower_bar_view)).init();
        ((UpperBarView) findViewById(R.id.upper_bar_view)).init();
    }
    public void initKeyboardHandler(Context context) {
        KeyboardHandler.init(context, this);

    }



}
