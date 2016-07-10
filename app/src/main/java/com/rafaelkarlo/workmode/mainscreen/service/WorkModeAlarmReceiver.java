package com.rafaelkarlo.workmode.mainscreen.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.rafaelkarlo.workmode.MainApplication;

import javax.inject.Inject;

import static android.app.AlarmManager.RTC_WAKEUP;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.ONE_DAY_IN_MILLIS;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.WORK_END_ACTION;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.WORK_START_ACTION;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.createIntentWithIdentifierAndTime;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.createPendingIntentWithIntent;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.getActionIdentifierWithTimeKey;

public class WorkModeAlarmReceiver extends BroadcastReceiver{

    private WorkModeService workModeService;

    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        injectDependencies(context);

        String actionIdentifier = intent.getAction();
        scheduleNextAlarmIfVersionIsKitkatOrAbove(context, intent, actionIdentifier);

        if(actionIdentifier.equals(WORK_START_ACTION)) {
            workModeService.setToSilentMode();
        } else if (actionIdentifier.equals(WORK_END_ACTION)){
            workModeService.setToNormalMode();
        }
    }

    @Inject
    public void setWorkModeService(WorkModeService workModeService) {
        this.workModeService = workModeService;
    }

    @Inject
    public void setAlarmManager(AlarmManager alarmManager) {
        this.alarmManager = alarmManager;
    }


    private void injectDependencies(Context context) {
        ((MainApplication) context.getApplicationContext()).getMainActivityComponent().inject(this);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void scheduleNextAlarmIfVersionIsKitkatOrAbove(Context context, Intent intent, String actionIdentifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            long nextTriggerTime = intent.getLongExtra(getActionIdentifierWithTimeKey(actionIdentifier), 0) + ONE_DAY_IN_MILLIS;
            alarmManager.setExact(
                    RTC_WAKEUP,
                    nextTriggerTime,
                    createPendingIntentWithIntent(context, createIntentWithIdentifierAndTime(context, actionIdentifier, nextTriggerTime))
            );
        }
    }
}
