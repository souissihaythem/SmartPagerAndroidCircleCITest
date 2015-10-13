/*
 * Copyright 2011 woozzu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.smartpager.android.view.indexable;

public class StringMatcher {

	private static boolean mCaseSensitive = false;
	
	public static void setCaseSensitive(boolean sensitive){
		mCaseSensitive = sensitive;
	}
	
	public static boolean isCaseSensitive(){
		return mCaseSensitive;
	}
	
	public static boolean match(String value, String keyword) {
		if (! isCaseSensitive()){
			value = value.toUpperCase();
			keyword = keyword.toUpperCase();
		}
		
		if (value == null || keyword == null)
			return false;
		if (keyword.length() > value.length())
			return false;

		int i = 0, j = 0;
		do {
			if (keyword.charAt(j) == value.charAt(i)) {
				i++;
				j++;
			} else if (j > 0)
				break;
			else
				i++;
		} while (i < value.length() && j < keyword.length());

		return (j == keyword.length()) ? true : false;
	}

}
