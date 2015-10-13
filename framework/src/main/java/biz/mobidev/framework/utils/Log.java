package biz.mobidev.framework.utils;

/**
 * Class provide functions for print messages to LogCat
 * 
 * @author Roman
 * @category Utils
 * @version 2.0
 */
public class Log {

	private static boolean mUseLog = true;
	private static String pointMessage = "POINT";
	private static String crashMessage = "CRASH";

	public static void setUseLog(boolean useLog) {
		mUseLog = useLog;
	}

	static public void write(Log.LogType logType, String value) {
		int pos = 0;
		StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
		if (stackTraceElements.length > 1) {
			pos = 1;
		}
		write(logType, stackTraceElements[pos].getMethodName(), value);
	}

	static public void write(Log.LogType logType, int pos, String value) {
		StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
		if (stackTraceElements.length - 1 < pos) {
			pos = stackTraceElements.length;
		}
		String methodName = stackTraceElements[pos].getMethodName();
		String lineNumber = String.valueOf(stackTraceElements[pos].getLineNumber());
		write(logType, "Line:" + lineNumber + " " + methodName, value);
	}

	static public void writeWithLink(Log.LogType logType, int pos, String value) {
		StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
		if (stackTraceElements.length - 1 < pos) {
			pos = stackTraceElements.length;
		}
		String fullClassName = stackTraceElements[pos].getClassName();
		String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		String methodName = stackTraceElements[pos].getMethodName();
		String lineNumber = String.valueOf(stackTraceElements[pos].getLineNumber());
		write(logType, value, "at " + fullClassName + "." + methodName + "(" + className + ".java:" + lineNumber + ")"/* value */);
	}

	static private void write(Log.LogType logType, String tag, String value) {
		if (mUseLog) {
			switch (logType) {
				case Information:
					android.util.Log.i(tag, value);
				break;
				case Message:
					android.util.Log.v(tag, value);
				break;
				case Error:
					android.util.Log.e(tag, value);
				break;
				case Debug:
					android.util.Log.d(tag, value);
				break;
				case Warning:
					android.util.Log.d(tag, value);
				break;
				default:
				break;
			}
		}
	}

	/**
	 * Write values to LogCat. As Tag write name of function which call this function
	 * 
	 * @param values
	 *            which should output to LogCat.</br> If last value is <b>\n</b> then all values will be write from new
	 *            string</br> If last value is <b>\t</b> then odd values will be write as tag and even values will be
	 *            write as value. Each pair <i>(tag - value)</i> will be output from new line
	 */
	static public void v(Object... values) {
		Log.write(LogType.Message, 2, parse(values));
	}

	/**
	 * Write values to LogCat. As Tag write name of function which call this function
	 * 
	 * @param values
	 *            which should output to LogCat.</br> If last value is <b>\n</b> then all values will be write from new
	 *            string</br> If last value is <b>\t</b> then odd values will be write as tag and even values will be
	 *            write as value. Each pair <i>(tag - value)</i> will be output from new line
	 */
	static public void e(Object... values) {
		Log.write(LogType.Error, 2, parse(values));
	}

	/**
	 * Write values to LogCat. As Tag write name of function which call this function
	 * 
	 * @param values
	 *            which should output to LogCat.</br> If last value is <b>\n</b> then all values will be write from new
	 *            string</br> If last value is <b>\t</b> then odd values will be write as tag and even values will be
	 *            write as value. Each pair <i>(tag - value)</i> will be output from new line
	 */
	static public void d(Object... values) {
		Log.write(LogType.Debug, 2, parse(values));
	}

	/**
	 * Write values to LogCat. As Tag write name of function which call this function
	 * 
	 * @param values
	 *            which should output to LogCat.</br> If last value is <b>\n</b> then all values will be write from new
	 *            string</br> If last value is <b>\t</b> then odd values will be write as tag and even values will be
	 *            write as value. Each pair <i>(tag - value)</i> will be output from new line
	 */
	static public void i(Object... values) {
		Log.write(LogType.Information, 2, parse(values));
	}

	/**
	 * Write values to LogCat. As Tag write name of function which call this function
	 * 
	 * @param values
	 *            which should output to LogCat.</br> If last value is <b>\n</b> then all values will be write from new
	 *            string</br> If last value is <b>\t</b> then odd values will be write as tag and even values will be
	 *            write as value. Each pair <i>(tag - value)</i> will be output from new line
	 */
	static public void w(Object... values) {
		Log.write(LogType.Warning, 2, parse(values));
	}

	static private String parse(Object... values) {
		String parseString;
		if (values[values.length - 1] != null) {
			if (values[values.length - 1].toString().contains("\t")) {
				parseString = parseWithTag(values);
			} else if (values[values.length - 1].toString().contains("\n")) {
				parseString = parseWithNewLine(values);
			} else {
				parseString = parseToLine(values);
			}
		} else {
			parseString = parseToLine(values);
		}
		return parseString;
	}

	static private String parseWithTag(Object[] values) {
		StringBuilder parseString = new StringBuilder();
		int length = values.length - 1;
		for (int i = 0; i != length; i++) {

			parseString.append("#param ");
			parseString.append(i + 1);
			parseString.append(": ");

			if (values[i] != null) {
				parseString.append(values[i]);
			} else {
				parseString.append("NULL");
			}
			i++;
			parseString.append(": ");
			if (values[i] != null) {
				parseString.append(values[i]);
			} else {
				parseString.append("NULL");
			}
			parseString.append("\n");
		}
		return parseString.toString();
	}

	static private String parseWithNewLine(Object[] values) {
		StringBuilder parseString = new StringBuilder();
		int length = values.length - 1;
		for (int i = 0; i != length; i++) {

			parseString.append("#param ");
			parseString.append(i + 1);
			parseString.append(": ");

			if (values[i] != null) {
				parseString.append(values[i]);
			} else {
				parseString.append("NULL");
			}
			parseString.append("\n");
		}
		return parseString.toString();
	}

	static private String parseToLine(Object[] values) {
		StringBuilder parseString = new StringBuilder();
		int length = values.length;
		for (int i = 0; i != length; i++) {
			if (length > 1) {
				parseString.append("#param ");
				parseString.append(i + 1);
				parseString.append(": ");
			}
			if (values[i] != null) {
				parseString.append(values[i]);
			} else {
				parseString.append("NULL");
			}
		}
		return parseString.toString();
	}

	/**
	 * Write to LogCat <i>BEGIN</i> tag and link to line in code
	 */
	static public void BEGIN() {
		Log.writeWithLink(LogType.Information, 2, "BEGIN");
	}

	/**
	 * Write to LogCat <i>END</i> tag and link to line in code
	 */
	static public void END() {
		Log.writeWithLink(LogType.Information, 2, "END");
	}

	/**
	 * Write to LogCat <i>POINT</i> tag and link to line in code
	 */
	static public void POINT() {
		Log.writeWithLink(LogType.Information, 2, pointMessage);
	}

	/**
	 * Write to LogCat <i>CRASH</i> tag and link to line in code
	 */
	static public void CRASH() {
		Log.writeWithLink(LogType.Error, 2, crashMessage);
	}

	// =======================================================================================
	public enum LogType {
		Information, Message, Error, Debug, Warning;
	}
}
