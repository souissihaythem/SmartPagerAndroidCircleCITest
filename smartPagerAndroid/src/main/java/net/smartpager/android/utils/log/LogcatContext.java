package net.smartpager.android.utils.log;

/**
 * Simple class to help keep context across multiple logcat invocations (between onStop and onStart). Similar to the
 * way that the unidiff patch format works to keep context.
 */
public class LogcatContext {
	int mPrec;
	int mPos;
	String[] mLastLines;
	int mLastLineCount;

	public LogcatContext(int precision) {
		mPrec = precision;
		mPos = mLastLineCount = 0;
		mLastLines = new String[precision * 2];
	}

	public void addLine(String line) {

	}
}