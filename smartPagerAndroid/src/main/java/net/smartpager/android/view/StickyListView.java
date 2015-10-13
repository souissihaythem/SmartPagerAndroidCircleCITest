package net.smartpager.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ListView;

import net.smartpager.android.model.StickyListViewItem;

import java.util.Iterator;
import java.util.List;

import biz.mobidev.framework.utils.Log;

/**
 * Created by dmitriy on 1/8/14.
 */
public class StickyListView extends ListView {
    public interface StickiListViewDrawer {
        public static final int DRAWER_POSITION_EQUALS = 0;
        public static final int DRAWER_POSITION_OVER = 1;
        public static final int DRAWER_POSITION_UNDER = 2;

        public void draw (Canvas canvas);
        public void onSizeChanged (int w, int h, int oldw, int oldh);
        public void update (boolean isActive);
        public void setBackgroundDrawable (Bitmap backgroundDrawable);
        public void setItemPositionInList (int pos);
        public void setText (String text);
        public int getItemPosition ();
        public boolean isActive ();
        public int getRelativePosition (int currListPosition);
    }
    private List<StickyListViewItem> m_stickyItems = null;

    public StickyListView(Context context) {
        super(context);
    }

    public StickyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    if(m_stickyItems != null && m_stickyItems.size() > 0)
            for(StickiListViewDrawer drawer : m_stickyItems)
               drawer.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(m_stickyItems != null && m_stickyItems.size() > 0)
            for(StickiListViewDrawer drawer : m_stickyItems)
                drawer.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        updateStickyItems(getFirstVisiblePosition());
    }

    private StickiListViewDrawer m_currDrawer = null;
    private void updateStickyItems (int currListPosition)
    {
        if(m_stickyItems != null && m_stickyItems.size() > 0)
        {
            if(m_currDrawer != null && m_currDrawer.getRelativePosition(currListPosition) == StickiListViewDrawer.DRAWER_POSITION_UNDER)
            {
                m_currDrawer.update(false);
                m_currDrawer = null;
            }
            for(StickiListViewDrawer drawer : m_stickyItems)
            {
                if(drawer.getRelativePosition(currListPosition) == StickiListViewDrawer.DRAWER_POSITION_OVER || drawer.getRelativePosition(currListPosition) == StickiListViewDrawer.DRAWER_POSITION_EQUALS)
                {
                    if(m_currDrawer == null)
                    {
//                        drawer.setBackgroundDrawable(getChildAt(currListPosition).getDrawingCache(true));
                        drawer.update(true);
                        m_currDrawer = drawer;
                        return;
                    }else
                    {
                        if(!drawer.isActive() && drawer.getItemPosition() > m_currDrawer.getItemPosition())
                        {
                            m_currDrawer.update(false);
//                            drawer.setBackgroundDrawable(getChildAt(currListPosition).getDrawingCache(true));
                            drawer.update(true);
                            m_currDrawer = drawer;
                            return;
                        }
                    }
                }
            }
        }
    }

    public void setStickyItems (List<StickyListViewItem> items)
    {
        this.m_stickyItems = items;
    }
}
