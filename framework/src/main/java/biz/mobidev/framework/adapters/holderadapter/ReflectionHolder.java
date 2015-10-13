package biz.mobidev.framework.adapters.holderadapter;

import java.util.ArrayList;

import biz.mobidev.framework.validate.Validator;
import biz.mobidev.framework.viewdataprovider.DataViewDependency;
import biz.mobidev.framework.viewdataprovider.ViewDataProvider;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;


public abstract class ReflectionHolder implements IHolder {

	ArrayList<DataViewDependency> dependencies;
	View view;
	public IHolder createHolder(View view) {
		this.view = view;
		return this;
	}
	
	public void setData(Object sours) {
		if(!Validator.notNull(dependencies)){
			dependencies = ViewDataProvider.findGetMethodList(view, sours);
		}
		for (DataViewDependency dependency : dependencies) {
			dependency.fillView(sours);
		}
			
	}

	public abstract ViewGroup getInflateView(Context context);

	public abstract int getLayoutResorsID();

}
