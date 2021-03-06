package com.rafaelkarlo.workmode.mainscreen.service;

import android.content.SharedPreferences;

import com.rafaelkarlo.workmode.mainscreen.service.audio.AudioMode;
import com.rafaelkarlo.workmode.mainscreen.service.audio.AudioModeService;
import com.rafaelkarlo.workmode.mainscreen.service.time.WorkTimeService;
import com.rafaelkarlo.workmode.mainscreen.service.time.WorkDay;

import org.joda.time.LocalTime;

import java.util.Set;

import static com.rafaelkarlo.workmode.mainscreen.service.audio.AudioMode.SILENT;
import static org.joda.time.DateTime.now;

public class WorkModeService {

    private static final String WORK_MODE_ACTIVATED = "WORK_MODE_ACTIVATED";

    private AudioModeService audioModeService;
    private WorkTimeService workTimeService;
    private SharedPreferences sharedPreferences;

    public WorkModeService(AudioModeService audioModeService, WorkTimeService workTimeService, SharedPreferences sharedPreferences) {
        this.audioModeService = audioModeService;
        this.workTimeService = workTimeService;
        this.sharedPreferences = sharedPreferences;
    }

    public boolean setToWorkMode() {
        if (canSetToWorkMode()) {
            saveCurrentRingerMode();
            audioModeService.setModeTo(SILENT);
            return true;
        } else {
            return false;
        }
    }

    public boolean setBackToOffWorkMode() {
        if (canSetToOffWorkMode()) {
            setToPreviousMode();
            return true;
        } else {
            return false;
        }
    }

    public void setToPreviousMode() {
        audioModeService.setModeTo(getPreviousRingerMode());
    }

    public void activate() {
        saveModeActivated();
    }

    public void deactivate() {
        saveModeDeactivated();
    }

    public boolean isActivated() {
        return sharedPreferences.getBoolean(WORK_MODE_ACTIVATED, false);
    }

    public void setStartTime(LocalTime workStartTime) {
        workTimeService.setStartWorkTime(workStartTime);
    }

    public void setEndTime(LocalTime workEndTime) {
        workTimeService.setEndWorkTime(workEndTime);
    }

    public LocalTime getStartTime() {
        return workTimeService.getStartWorkTime();
    }

    public LocalTime getEndTime() {
        return workTimeService.getEndWorkTime();
    }

    public void setWorkDays(Set<WorkDay> workDays) {
        workTimeService.saveWorkDays(workDays);
    }

    public Set<WorkDay> getWorkDays() {
        return workTimeService.getWorkDays();
    }

    private AudioMode getPreviousRingerMode() {
        return audioModeService.getPreviouslySavedMode();
    }

    private boolean canSetToWorkMode() {
        return nowIsWithinWorkHours() && isActivated() && nowIsAWorkDay();
    }

    private boolean canSetToOffWorkMode() {
        return nowIsAfterWorkHours() && isActivated() && nowIsAWorkDay();
    }

    private boolean nowIsWithinWorkHours() {
        LocalTime startWorkTime = workTimeService.getStartWorkTime();
        LocalTime endWorkTime = workTimeService.getEndWorkTime();
        int startTimeInMillisOfDay = startWorkTime == null ? -1 : startWorkTime.getMillisOfDay();
        int endTimeInMillisOfDay = endWorkTime == null ? -1 : endWorkTime.getMillisOfDay();
        int nowInMillisOfDay = now().getMillisOfDay();

        if (endTimeInMillisOfDay < startTimeInMillisOfDay) {
            return isNowBetweenStartAndEndDuringNightShift(startTimeInMillisOfDay,
                    endTimeInMillisOfDay,
                    nowInMillisOfDay);
         } else {
            return startTimeInMillisOfDay <= nowInMillisOfDay
                    && nowInMillisOfDay <= endTimeInMillisOfDay;
        }
    }

    private boolean nowIsAWorkDay() {
        int currentDayInt = now().getDayOfWeek();
        WorkDay currentDay = WorkDay.getDayFromValue(currentDayInt);
        return workTimeService.getWorkDays().contains(currentDay);
    }

    private boolean isNowBetweenStartAndEndDuringNightShift(int startTimeInSecondsOfDay, int endTimeInSecondsOfDay, int nowInMillisOfDay) {
        return isNowAfterStartOfWorkhoursBeforeMidnight(startTimeInSecondsOfDay, nowInMillisOfDay)
                || isNowAfterMidnightBeforeEndOfWorkHours(nowInMillisOfDay, endTimeInSecondsOfDay);
    }

    private boolean isNowAfterMidnightBeforeEndOfWorkHours(int nowInMillisOfDay,
                                                           int endOfDayInMillisOfDay) {
        return new LocalTime(0, 0, 0, 0).getMillisOfDay() <= nowInMillisOfDay
                && nowInMillisOfDay < endOfDayInMillisOfDay;
    }

    private boolean isNowAfterStartOfWorkhoursBeforeMidnight(int startTimeInSecondsOfDay,
                                                             int nowInMillisOfDay) {
        return startTimeInSecondsOfDay <= nowInMillisOfDay
                && nowInMillisOfDay <= new LocalTime(23, 59, 59, 999).getMillisOfDay();
    }

    private boolean nowIsAfterWorkHours() {
        LocalTime endWorkTime = workTimeService.getEndWorkTime();
        int endTimeInSecondsOfDay = endWorkTime == null ? -1 : endWorkTime.getMillisOfDay();

        return now().getMillisOfDay() >= endTimeInSecondsOfDay;
    }

    private void saveCurrentRingerMode() {
        audioModeService.saveCurrentRingerMode(audioModeService.getCurrentMode());
    }

    private void saveModeActivated() {
        saveModeInSharedPreferences(true);
    }

    private void saveModeDeactivated() {
        saveModeInSharedPreferences(false);
    }

    private void saveModeInSharedPreferences(boolean activated) {
        sharedPreferences.edit().putBoolean(WORK_MODE_ACTIVATED, activated).apply();
    }
}
