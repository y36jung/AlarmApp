package com.midmad1.alarmapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.midmad1.alarmapp.MainActivity.alarmID;
import static com.midmad1.alarmapp.MainActivity.mDatabase;
import static com.midmad1.alarmapp.MainActivity.positionID;

public class AlarmNotification extends AppCompatActivity {

    private TextView alarmText;
    private TextView textView;
    private Button stopAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_receiver);

        alarmText = findViewById(R.id.alarmText);
        textView = findViewById(R.id.textView2);

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + AlarmContract.AlarmEntry.TABLE_NAME +
                " WHERE " + AlarmContract.AlarmEntry._ID + "=" + alarmID, null);
        String eventName = "";
        String alarmName = "";
        int vibrations = 0;

        if (cursor.moveToFirst()) {
            eventName = cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_EVENT));
            long alarmLong = cursor.getLong(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_ALARM));
            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.setTimeInMillis(alarmLong);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            alarmName = "" + sdf.format(alarmCalendar.getTime());
            vibrations = cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_VIBRATION));
            cursor.close();
        }

        alarmText.setText(eventName);
        textView.setText(alarmName);

        if (vibrations == 1) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        }

        stopAlarm = findViewById(R.id.button3);
        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    positionID.replace(alarmID, false);
                } else {
                    positionID.remove(alarmID);
                    positionID.put(alarmID, false);
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}