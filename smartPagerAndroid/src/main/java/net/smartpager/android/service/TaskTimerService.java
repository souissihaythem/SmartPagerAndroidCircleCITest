package net.smartpager.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.model.AbstractTaskTimer;
import net.smartpager.android.receivers.TaskTimerReceiver;

/**
 * Created by dmitriy on 3/13/14.
 */
public class TaskTimerService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final AbstractTaskTimer task = (AbstractTaskTimer) intent.getSerializableExtra(BundleKey.taskTimer.name());
        if (task != null) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    doCurrentTask(task);
                }
            });
            thread.start();
        }
        return Service.START_NOT_STICKY;
    }

    private void doCurrentTask(AbstractTaskTimer task) {
        if(task != null)
        {
            task.doAction();
            Intent intent = new Intent(TaskTimerReceiver.TASK_TIMER_ACTION_CALLBACK);
            intent.putExtra(BundleKey.taskTimer.name(), task);
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
