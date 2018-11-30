package com.example.android.architecture.blueprints.todoapp.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * setTextIfDifferent only sets the text if it would result in a text change, don't know why this is not the default
 */
public class CustomEditText extends android.support.v7.widget.AppCompatEditText {

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setTextIfDifferent(CharSequence newText) {
        if (getText().toString().equals(newText)) {
            return;
        } else {
            setText(newText);
        }
    }

    public void setTextIfDifferent(int newTextRes) {
        setTextIfDifferent(getContext().getResources().getText(newTextRes));
    }
}
