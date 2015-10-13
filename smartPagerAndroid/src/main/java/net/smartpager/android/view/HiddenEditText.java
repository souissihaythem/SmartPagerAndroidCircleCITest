package net.smartpager.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Used if EditText has to be hidden.<br>
 * The Inheritor of EditText class could have <i>layout_width = 0</i> and <i>layout_height = 0.</i>
 * It's only purpose for this class.
 * @author Roman
 *
 */
public class HiddenEditText extends EditText {

	public HiddenEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

}
