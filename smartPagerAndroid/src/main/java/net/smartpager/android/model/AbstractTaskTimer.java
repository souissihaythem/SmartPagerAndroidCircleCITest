package net.smartpager.android.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.TaskTimerAction;
import net.smartpager.android.receivers.TaskTimerReceiver;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by dmitriy on 3/13/14.
 */
public abstract class AbstractTaskTimer implements Serializable {

    private static final long serialVersionUID = -3203929238874612252L;

    protected final void cancelTask(AbstractTaskTimer task)
    {
        Intent alarmIntent = new Intent(TaskTimerReceiver.TASK_TIMER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SmartPagerApplication.getInstance(),
                task.getTaskAction().ordinal(), alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) SmartPagerApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    protected final void startTask(AbstractTaskTimer task)
    {
        Intent alarmIntent = new Intent(TaskTimerReceiver.TASK_TIMER_ACTION);
        alarmIntent.putExtra(BundleKey.taskTimer.name(), task);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SmartPagerApplication.getInstance(),
                task.getTaskAction().ordinal(), alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) SmartPagerApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        Date date = new Date();
        date.setTime(date.getTime() + task.getTaskRepeatDelay());
        if(task.isRepeated())
            alarmManager.setInexactRepeating(AlarmManager.RTC, date.getTime(), task.getTaskRepeatDelay(), pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC, date.getTime(), pendingIntent);
    }

    public abstract boolean isRepeated();
    public abstract long getTaskRepeatDelay();
    public abstract TaskTimerAction getTaskAction();
    public abstract void doAction();
}
