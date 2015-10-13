package net.smartpager.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

public class ListSerilizableContainer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4703296825264319011L;
	private List<IHolderSource> mList;

	public ListSerilizableContainer() {
		mList = new ArrayList<IHolderSource>();
	}

	public ListSerilizableContainer(List<IHolderSource> list) {
		mList = list;
	}
	
	public List<IHolderSource> getList() {
		return mList;
	}

	public void setList(List<IHolderSource> list) {
		mList = list;
	}
}
