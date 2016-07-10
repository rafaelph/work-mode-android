package com.rafaelkarlo.workmode.mainscreen.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Calendar;

import javax.inject.Inject;

import static android.app.AlarmManager.INTERVAL_DAY;
import static android.app.AlarmManager.RTC_WAKEUP;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.*;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.createIntentWithIdentifierAndTime;
import static com.rafaelkarlo.workmode.mainscreen.service.WorkModeAlarmUtils.createPendingIntentWithIntent;
import static org.joda.time.LocalTime.now;

public class WorkModeAlarmImpl implements WorkModeAlarm {

    private final AlarmManager alarmManager;

    private Context context;

    @Inject
    public WorkModeAlarmImpl(Context context, AlarmManager alarmManager) {
        this.context = context;
        this.alarmManager = alarmManager;
    }

    @Override
    public void startAlarm(LocalTime workStartTime, LocalTime workEndTime) {
        setDailyAlarmForAnAction(workStartTime, WORK_START_ACTION);
        setDailyAlarmForAnAction(workEndTime, WORK_END_ACTION);
    }

    @Override
    public void cancelAlarm() {
        alarmManager.cancel(createPendingIntentWithIntent(context, createIntentWithIdentifierAndTime(context, WORK_START_ACTION, 0)));
        alarmManager.cancel(createPendingIntentWithIntent(context, createIntentWithIdentifierAndTime(context, WORK_END_ACTION, 0)));
    }

    private void setDailyAlarmForAnAction(LocalTime triggerTime, String actionIdentifier) {
        long timeInMillis = getTimeInMillisFromLocalTime(triggerTime);
        Intent workStartIntent = createIntentWithIdentifierAndTime(context, actionIdentifier, timeInMillis);

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.KITKAT) {
            setAlarmForApi19AndAbove(timeInMillis, workStartIntent);
        } else {
            setAlarmForApiBelow19(timeInMillis, workStartIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setAlarmForApi19AndAbove(long triggerTimeInMillis, Intent workStartIntent) {
        triggerTimeInMillis = getNextTriggerInMillis(triggerTimeInMillis);
        alarmManager.setExact(
                RTC_WAKEUP,
                triggerTimeInMillis,
                createPendingIntentWithIntent(context, workStartIntent)
        );
    }

    private void setAlarmForApiBelow19(long triggerTimeInMillis, Intent workStartIntent) {
        alarmManager.setRepeating(
                RTC_WAKEUP,
                triggerTimeInMillis,
                INTERVAL_DAY,
                createPendingIntentWithIntent(context, workStartIntent)
        );
    }

    private long getTimeInMillisFromLocalTime(LocalTime triggerTime) {
        Calendar calendarSilentInstance = Calendar.getInstance();
        calendarSilentInstance.setTime(triggerTime.toDateTimeToday().toDate());
        return calendarSilentInstance.getTimeInMillis();
    }

    private long getNextTriggerInMillis(long triggerTimeInMillis) {
        int triggerTimeInMillisOfDay = new DateTime(triggerTimeInMillis).getMillisOfDay();
        if (triggerTimeInMillisOfDay < now().getMillisOfDay()) {
            triggerTimeInMillis = triggerTimeInMillis + ONE_DAY_IN_MILLIS;
        }
        return triggerTimeInMillis;
    }
}
