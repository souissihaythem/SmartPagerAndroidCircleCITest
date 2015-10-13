package net.smartpager.android.model;

import net.smartpager.android.consts.TaskTimerAction;

/**
 * Created by dmitriy on 3/13/14.
 */
public class CancelTimerTask extends AbstractTaskTimer {

    private static final long serialVersionUID = -4423929238870199228L;

    private AbstractTaskTimer m_currTask = null;

    public CancelTimerTask(AbstractTaskTimer task)
    {
        m_currTask = task;
    }

    @Override
    public boolean isRepeated() {
        return false;
    }

    @Override
    public long getTaskRepeatDelay() {
        return 0;
    }

    @Override
    public TaskTimerAction getTaskAction() {
        return TaskTimerAction.cancelTask;
    }

    @Override
    public void doAction() {
        if(m_currTask != null)
            cancelTask(m_currTask);
    }
}
