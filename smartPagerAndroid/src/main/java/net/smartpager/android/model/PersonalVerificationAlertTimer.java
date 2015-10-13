package net.smartpager.android.model;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.TaskTimerAction;

/**
 * Created by dmitriy on 3/13/14.
 */
public class PersonalVerificationAlertTimer extends AbstractTaskTimer {

    private static final long serialVersionUID = -3203929183048512252L;

    private static final int TASK_REPEAT_DELAY = 900000; //ms

    @Override
    public boolean isRepeated() {
        return false;
    }

    @Override
    public long getTaskRepeatDelay() {
        return TASK_REPEAT_DELAY;
    }

    @Override
    public TaskTimerAction getTaskAction() {
        return TaskTimerAction.personalVerificationReminder;
    }

    @Override
    public void doAction() {
        if(SmartPagerApplication.getInstance().getPreferences().isPersonalVerificationQuestionsSaved())
        {
            cancelTask(this);
        }else
        {
            startTask(this);
        }
    }
}
