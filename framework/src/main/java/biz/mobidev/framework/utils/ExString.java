package biz.mobidev.framework.utils;

/**
 * 
 * Class provide same external functionality fork with string  
 * @author poul
 *
 */
public class ExString {
	
	/**
	 * 
	 * Make first symbol of string to Upper case
	 * @param stringValue source string 
	 * @return string where symbol in upper case
	 */
	public static String upFirstChar(String stringValue){
		String result = stringValue.substring(0,1).toUpperCase();
		result = result + stringValue.substring(1);
		return result;		
	}
}
