package net.smartpager.android.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import net.smartpager.android.view.StickyListView;

/**
 * Created by dmitriy on 1/8/14.
 */
public abstract class StickyListViewItem implements StickyListView.StickiListViewDrawer {
    private Paint m_stickyItemBGPaint = new Paint();
    private Paint m_stickyItemTextPaint = new Paint();
    private RadialGradient m_stickyItemRadialGradient = null;
    private Rect m_stickyItemBG = new Rect();
    private Bitmap m_stickyItemBGImage = null;

    private String m_sItemText = "";
    private int m_nItemPosition = 0;
    private int m_nTextSize = 25;

    private int m_stickyItemH = 40;
    private int m_listListViewW = 0;
    private int m_listListViewH = 0;

    private boolean m_bIsActive = false;

    @Override
    public void draw(Canvas canvas) {
        if(!m_bIsActive)
            return;
        if(m_stickyItemBGImage != null)
        {
            canvas.drawBitmap(m_stickyItemBGImage, 0, 0, null);
            return;
        }
        m_stickyItemBGPaint.setColor(Color.BLUE);
        m_stickyItemTextPaint.setColor(Color.RED);
        m_stickyItemTextPaint.setAntiAlias(true);
        m_stickyItemBGPaint.setAntiAlias(true);
        m_stickyItemBGPaint.setAlpha(75);
        m_stickyItemBG.set(0, 0, m_listListViewW, m_stickyItemH);
        canvas.drawRect(m_stickyItemBG, m_stickyItemBGPaint);
        m_stickyItemTextPaint.setTextSize(m_nTextSize);
        canvas.drawText(m_sItemText, m_listListViewW / 2, m_stickyItemH / 2, m_stickyItemTextPaint);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        m_listListViewH = h != 0 ? h : oldh;
        m_listListViewW = w != 0 ? w : oldw;
    }

    @Override
    public void setBackgroundDrawable (Bitmap backgroundDrawable)
    {
        if(backgroundDrawable != null)
            m_stickyItemBGImage = Bitmap.createBitmap(backgroundDrawable);
    }

    @Override
    public void setItemPositionInList(int pos) {
        m_nItemPosition = pos;
    }

    @Override
    public int getItemPosition() {
        return m_nItemPosition;
    }

    @Override
    public void setText(String text) {
        m_sItemText = text;
    }

    @Override
    public void update(boolean isActive) {
        m_bIsActive = isActive;
    }

    @Override
    public int getRelativePosition(int currListPosition) {
        return m_nItemPosition == currListPosition ? DRAWER_POSITION_EQUALS : m_nItemPosition < currListPosition ? DRAWER_POSITION_OVER : DRAWER_POSITION_UNDER;
    }

    @Override
    public boolean isActive() {
        return m_bIsActive;
    }
}
