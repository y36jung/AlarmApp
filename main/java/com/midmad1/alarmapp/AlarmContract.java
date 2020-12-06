package com.midmad1.alarmapp;

import android.provider.BaseColumns;

public class AlarmContract {

    private AlarmContract() {

    }

    public static final class AlarmEntry implements BaseColumns {
        public static final String TABLE_NAME = "alarmList";
        public static final String COLUMN_EVENT  = "event";
        public static final String COLUMN_ALARM = "alarm";
        public static final String COLUMN_VIBRATION = "vibration";
        public static final String COLUMN_SOUND = "sound";
    }
}
