package com.midmad1.alarmapp;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

import static com.midmad1.alarmapp.MainActivity.editAlarmID;
import static com.midmad1.alarmapp.MainActivity.editAlarmLong;
import static com.midmad1.alarmapp.MainActivity.editAlarmSound;
import static com.midmad1.alarmapp.MainActivity.editAlarmVib;
import static com.midmad1.alarmapp.MainActivity.editEventName;
import static com.midmad1.alarmapp.MainActivity.getAllItems;
import static com.midmad1.alarmapp.MainActivity.mAdapter;
import static com.midmad1.alarmapp.MainActivity.mArrayBool;
import static com.midmad1.alarmapp.MainActivity.mDatabase;
import static com.midmad1.alarmapp.MainActivity.positionID;
import static com.midmad1.alarmapp.MainActivity.removeAlarm;

public class SetAlarm extends AppCompatActivity {

    private EditText editText;
    private TimePicker timePicker;
    private TextView textView;
    private Button button;
    private Button setAlarm;
    private Button cancel;

    private DatePickerDialog datePickerDialog;

    private SetAlarmAdapter mSAAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        final Intent intent = getIntent();

        String eventName = intent.getStringExtra(editEventName);
        long alarmLong = intent.getLongExtra(editAlarmLong, 0);
        final long id = intent.getLongExtra(editAlarmID, -1);
        boolean alarmVib = intent.getBooleanExtra(editAlarmVib, true);
        boolean alarmSound = intent.getBooleanExtra(editAlarmSound, true);

        editText = findViewById((R.id.editText));

        if (eventName != "") {
            editText.setText(eventName);
        }

        timePicker = findViewById(R.id.timePicker);
        textView = findViewById(R.id.textView3);

        RecyclerView recyclerView = findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSAAdapter = new SetAlarmAdapter(this);
        recyclerView.setAdapter(mSAAdapter);

        final Calendar c = Calendar.getInstance();
        if (alarmLong != 0) {
            c.setTimeInMillis(alarmLong);
            int aHour = c.get(Calendar.HOUR_OF_DAY);
            int aMinute = c.get(Calendar.MINUTE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(aHour);
                timePicker.setMinute(aMinute);
            }
        }
        final int[] yearC = {c.get(Calendar.YEAR)};
        final int[] monthC = {c.get(Calendar.MONTH)};
        final int[] dayC = {c.get(Calendar.DAY_OF_MONTH)};

        textView.setText(dayC[0] + "/" + (monthC[0] + 1) + "/" + yearC[0]);

        button = findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    datePickerDialog = new DatePickerDialog(SetAlarm.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            textView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                            dayC[0] = dayOfMonth;
                            monthC[0] = month;
                            yearC[0] = year;
                        }
                    }, yearC[0], monthC[0] , dayC[0]);
                    datePickerDialog.show();
                }
            }
        });

        setAlarm = findViewById(R.id.button2);
        setAlarm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String eventName = editText.getText().toString();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                if (past(yearC[0], monthC[0], dayC[0], hour, minute)) {
                    Toast.makeText(SetAlarm.this, "You cannot set an alarm in the past!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Calendar alarmSet = Calendar.getInstance();
                    alarmSet.set(Calendar.YEAR, yearC[0]);
                    alarmSet.set(Calendar.MONTH, monthC[0]);
                    alarmSet.set(Calendar.DAY_OF_MONTH, dayC[0]);
                    alarmSet.set(Calendar.HOUR_OF_DAY, hour);
                    alarmSet.set(Calendar.MINUTE, minute);
                    alarmSet.set(Calendar.SECOND, 0);

                    if (eventName.isEmpty()) {
                        eventName = "Untitled Event";
                    }

                    long alarmLong = alarmSet.getTimeInMillis();
                    ContentValues cv = new ContentValues();

                    cv.put(AlarmContract.AlarmEntry.COLUMN_EVENT, eventName);
                    cv.put(AlarmContract.AlarmEntry.COLUMN_ALARM, alarmLong);
                    int boolInt = 0;
                    if (mArrayBool.get(0)) {
                        boolInt = 1;
                    }
                    cv.put(AlarmContract.AlarmEntry.COLUMN_VIBRATION, boolInt);
                    cv.put(AlarmContract.AlarmEntry.COLUMN_SOUND, 0);

                    mDatabase.insert(AlarmContract.AlarmEntry.TABLE_NAME, null, cv);
                    mAdapter.swapCursor(getAllItems());

                    if (id != -1) {
                        removeAlarm(id);
                        cv.put(AlarmContract.AlarmEntry._ID, id);
                    } else {
                        Cursor cursor = getAllItems();
                        if (cursor.moveToLast()) {
                            long id = cursor.getLong(cursor.getColumnIndex(AlarmContract.AlarmEntry._ID));
                            positionID.put(id, true);
                            Toast.makeText(getApplicationContext(), id + "", Toast.LENGTH_SHORT).show();
                        }
                    }

                    startActivity(intent);
                    finish();
                }
            }
        });

        cancel = findViewById(R.id.cancel_button2);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public boolean past(int year, int month, int day, int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar as = Calendar.getInstance();
        as.set(Calendar.YEAR, year);
        as.set(Calendar.MONTH, month);
        as.set(Calendar.DAY_OF_MONTH, day);
        as.set(Calendar.HOUR_OF_DAY, hour);
        as.set(Calendar.MINUTE, minute);
        as.set(Calendar.SECOND, 0);
        return as.before(now);
    }

}