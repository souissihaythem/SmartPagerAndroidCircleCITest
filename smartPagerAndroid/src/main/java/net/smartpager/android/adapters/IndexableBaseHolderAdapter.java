package net.smartpager.android.adapters;

import android.content.Context;
import android.widget.SectionIndexer;

import java.util.HashMap;
import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

public class IndexableBaseHolderAdapter extends BaseHolderAdapter implements SectionIndexer {

	private static final String SECTIONS = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private HashMap<String, Integer> mAddedCaption;

	public IndexableBaseHolderAdapter(Context context, List<IHolderSource> soursList,
			List<Class<? extends IHolder>> holderClasses, Object... listeners) {
		super(context, soursList, holderClasses, listeners);
		mAddedCaption = new HashMap<String, Integer>();
	}

    private int getCaptionPosition (String key)
    {
        int position = 0;
        if(mAddedCaption == null || mAddedCaption.size() == 0)
            return position;
        if ( mAddedCaption.containsKey(key) ){
            position = mAddedCaption.get(key);
        }
        return position;
    }

	// ---------------------------------------------------------------------
	public void setPositionMap(HashMap<String, Integer> map) {
		mAddedCaption = new HashMap<String, Integer>();
		int position = 0;
		for(char letter : SECTIONS.toCharArray()){
			String key = String.valueOf(letter);
			if ( map.containsKey(key) ){
				position = map.get(key);
			}
			mAddedCaption.put(key, position);
			
		};
	}

	@Override
	public int getPositionForSection(int section) {
		String firstChar = String.valueOf(SECTIONS.charAt(section));
		if (mAddedCaption != null && mAddedCaption.containsKey(firstChar)) {
			return mAddedCaption.get(firstChar);
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		String[] sections = new String[SECTIONS.length()];
		for (int i = 0; i < SECTIONS.length(); i++)
			sections[i] = String.valueOf(SECTIONS.charAt(i));
		return sections;
	}

}
