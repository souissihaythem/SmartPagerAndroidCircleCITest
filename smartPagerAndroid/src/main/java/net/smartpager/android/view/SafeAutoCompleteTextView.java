package net.smartpager.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * This class was created to avoid the random crash while working with cursor
 * Created by dmitriy on 3/17/14.
 */
public class SafeAutoCompleteTextView extends AutoCompleteTextView {

    public SafeAutoCompleteTextView(Context context) {
        super(context);
    }

    public SafeAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onFilterComplete (int count) {
        try {
            super.onFilterComplete(count);
        } catch (IllegalStateException e) {
        }
    }

    @Override
    public void showDropDown() {
        try {
            super.showDropDown();
        } catch (IllegalStateException e) {
        }
    }
}
