package net.smartpager.android.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.TaskTimerAction;
import net.smartpager.android.receivers.TaskTimerReceiver;

import java.util.Date;

/**
 * Created by dmitriy on 3/13/14.
 */
public class CreateTimerTask extends AbstractTaskTimer {

    private static final long serialVersionUID = -3244929255674789252L;

    private AbstractTaskTimer m_currTask = null;

    public CreateTimerTask(AbstractTaskTimer task)
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
        return TaskTimerAction.createTask;
    }

    @Override
    public void doAction() {
        startTask(m_currTask);
    }
}
