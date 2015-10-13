package biz.mobidev.framework.adapters.holderadapter;

import java.util.ArrayList;
import java.util.List;

import biz.mobidev.framework.utils.TimeChecker;
import biz.mobidev.framework.utils.ViewUtil;
import biz.mobidev.framework.validate.Validator;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class BaseHolderAdapter extends BaseAdapter {

	protected List<IHolderSource> mSourceList;
	private List<Class<? extends IHolder>> mHolderClasses;
	private Context mContext;
	private Object[] mListeners;
	private Bundle mPreSetDataParams;

	private boolean mUseTimeLog;

	public BaseHolderAdapter(Context context, List<IHolderSource> soursList, Class<? extends IHolder> holderClass) {
		this(context, soursList, holderClass, null);
	}

	public BaseHolderAdapter(Context context, List<IHolderSource> soursList, Class<? extends IHolder> holderClass,
			Object... listeners) {
		this.mHolderClasses = new ArrayList<Class<? extends IHolder>>();
		this.mHolderClasses.add(holderClass);
		this.mContext = context;
		this.mUseTimeLog = false;
		this.mListeners = listeners;
		this.mSourceList = soursList;
	}
	
	public BaseHolderAdapter(Context context, List<IHolderSource> soursList,
			List<Class<? extends IHolder>> holderClasses) {
		this(context, soursList, holderClasses, null);
	}

	public BaseHolderAdapter(Context context, List<IHolderSource> soursList,
			List<Class<? extends IHolder>> holderClasses, Object... listeners) {
		this.mSourceList = soursList;
		this.mContext = context;
		this.mHolderClasses = holderClasses;
		this.mListeners = listeners;
		this.mUseTimeLog = false;
	}

	public List<IHolderSource> getSourceList(){
		return mSourceList;
	}
	
	public Bundle getPreSetDataParams(){
		return mPreSetDataParams;
	}
	
	public void setPreSetDataParams(Bundle params){
		mPreSetDataParams = params;
	}
	
	public void updateSours(List<IHolderSource> newSource) {
		mSourceList.clear();
		mSourceList.addAll(newSource);
		notifyDataSetChanged();
	}

	public int getCount() {
		return mSourceList.size();
	}

	public Object getItem(int position) {
		return mSourceList.get(position);
	}

	public long getItemId(int position) {
		return mSourceList.get(position).getHolderID();
	}

	public IHolder getHolder(int position) {
		IHolder curentHolder = null;
		try {
			curentHolder = mHolderClasses.get((int) getItemId(position)).newInstance();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		}
		return curentHolder;
	}

	@Override
	public int getItemViewType(int position) {
		return mSourceList.get(position).getHolderID();
	}

	@Override
	public int getViewTypeCount() {
		return mHolderClasses.size();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (isUseTimeLog()) {
			TimeChecker.SetStartPoint();
		}
		ViewGroup viewGroup = null;
		IHolderSource currentSoursObject = (IHolderSource) getItem(position);
		IHolder curentHolder = null;
		if (convertView == null) {
			curentHolder = getHolder(position);
			viewGroup = ViewUtil.inflate(mContext, curentHolder);
			curentHolder.createHolder(viewGroup);
		} else {
			viewGroup = (ViewGroup) convertView;
			curentHolder = (IHolder) viewGroup.getTag();
			if (!Validator.notNull(curentHolder)) {
				curentHolder = getHolder(position);
				curentHolder.createHolder(viewGroup);
			}
		}
		curentHolder.preSetData(mPreSetDataParams);
		curentHolder.setData(currentSoursObject);
		curentHolder.setListeners(mListeners);
		viewGroup.setTag(curentHolder);
		if (isUseTimeLog()) {
			TimeChecker.WriteTimeSpendInMicroSeconds();
		}
		return viewGroup;
	}

	public boolean isUseTimeLog() {
		return mUseTimeLog;
	}

	public void setUseTimeLog(boolean useTimeLog) {
		mUseTimeLog = useTimeLog;
	}

}
