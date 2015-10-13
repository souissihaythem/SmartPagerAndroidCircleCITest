package net.smartpager.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.smartpager.android.service.TaskTimerService;

/**
 * Created by dmitriy on 3/13/14.
 */
public class TaskTimerReceiver extends BroadcastReceiver {
    public static final String TASK_TIMER_ACTION = "net.smartpager.android.UPDATE_TASK_TIMER";
    public static final String TASK_TIMER_ACTION_CALLBACK = "net.smartpager.android.UPDATE_TASK_TIMER_CALLBACK";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent taskService = new Intent(context, TaskTimerService.class);
        if(intent.getExtras() != null)
            taskService.putExtras(intent.getExtras());
        context.startService(taskService);
    }
}
