package com.midmad1.alarmapp;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView noAlarmsSet;
    private Button button;

    public static String sharedPref = "SHARED_PREFERENCES";

    public static SQLiteDatabase mDatabase;
    public static AlarmAdapter mAdapter;

    public static String editEventName = "editEventName";
    public static String editAlarmLong = "editAlarm";
    public static String editAlarmID = "editAlarmID";
    public static String editAlarmVib = "editAlarmVib";
    public static String editAlarmSound = "editAlarmSound";

    public static HashMap<Long, Boolean> positionID = new HashMap<>();
    public static ArrayList<String> mArrayString = new ArrayList<>();
    public static ArrayList<Boolean> mArrayBool = new ArrayList<>();

    public static long alarmID;

    public static String CHANNEL_ID = "alarmAppChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        setContentView(R.layout.activity_main);

        if (!positionID.isEmpty()) {
            saveHashMap();
        }
        loadHashMap();

        if ((mArrayString.isEmpty()) && (mArrayBool.isEmpty())) {
            setmArrays();
        }



        noAlarmsSet = findViewById(R.id.noAlarmsSet);

        AlarmDBHelper dbHelper = new AlarmDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        changeBackgroundText();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AlarmAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                long id = (long) viewHolder.itemView.getTag();
                if (direction  == ItemTouchHelper.LEFT) {
                    Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + AlarmContract.AlarmEntry.TABLE_NAME +
                            " WHERE " + AlarmContract.AlarmEntry._ID + "=" + id, null);
                    String eventName = "";
                    long alarmLong = 0;
                    int alarmVib = 0;
                    int alarmSound = 0;
                    if (cursor.moveToFirst()) {
                        eventName = cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_EVENT));
                        alarmLong = cursor.getLong(cursor.getColumnIndex((AlarmContract.AlarmEntry.COLUMN_ALARM)));
                        alarmVib = cursor.getInt(cursor.getColumnIndex((AlarmContract.AlarmEntry.COLUMN_VIBRATION)));
                        alarmSound = cursor.getInt(cursor.getColumnIndex((AlarmContract.AlarmEntry.COLUMN_SOUND)));
                        Intent intent = new Intent(getApplicationContext(), SetAlarm.class);
                        intent.putExtra(editEventName, eventName);
                        intent.putExtra(editAlarmLong, alarmLong);
                        intent.putExtra(editAlarmID, id);

                        boolean bool = false;
                        if (alarmVib == 1) {
                            bool = true;
                        }
                        intent.putExtra(editAlarmVib, bool);

                        if (alarmSound == 1) {
                            bool = true;
                        }
                        intent.putExtra(editAlarmSound, bool);

                        cursor.close();
                        startActivity(intent);
                        finish();
                    }
                }

                if (direction == ItemTouchHelper.RIGHT) {
                    AlertDialog box = askDelete(id);
                    box.show();
                }
                mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                changeBackgroundText();
            }
        }).attachToRecyclerView(recyclerView);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlarmManager();
                finish();
            }
        });
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID, "Alarm App Channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    // Opening SetAlarm activity
    public void openAlarmManager() {
        Intent intent = new Intent(this, SetAlarm.class);
        startActivity(intent);
        finish();
    }

    public static Cursor getAllItems() {
        return mDatabase.query(
                AlarmContract.AlarmEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                AlarmContract.AlarmEntry.COLUMN_ALARM + " ASC"
        );
    }

    public static void removeAlarm(long id) {
        mDatabase.delete(AlarmContract.AlarmEntry.TABLE_NAME,
                AlarmContract.AlarmEntry._ID + "=" + id, null);
        mAdapter.swapCursor(getAllItems());
    }

    private void changeBackgroundText() {
        long count = DatabaseUtils.queryNumEntries(mDatabase, AlarmContract.AlarmEntry.TABLE_NAME);
        if (count > 0) {
            noAlarmsSet.setText("");
        } else {
            noAlarmsSet.setText("No Alarms Set");
        }
    }

    private AlertDialog askDelete(final long id) {
        Cursor cursor = mDatabase.rawQuery("SELECT " + AlarmContract.AlarmEntry.COLUMN_EVENT +
                " FROM " + AlarmContract.AlarmEntry.TABLE_NAME +
                " WHERE " + AlarmContract.AlarmEntry._ID + "=" + id, null);
        String eventName = "";
        if (cursor.moveToFirst()) {
            eventName = cursor.getString(
                    cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_EVENT));
        }
        cursor.close();
        AlertDialog deleteDialog = new AlertDialog.Builder(this)
                .setTitle("Delete " + "\"" + eventName + "\"")
                .setMessage("Do you want to delete this alarm?")
                .setIcon(R.drawable.circle)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeAlarm(id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return deleteDialog;
    }

    public void saveHashMap() {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<Long, Boolean>>(){}.getType();
        String json = gson.toJson(positionID, type);

        /*
        Gson gson2 = new Gson();
        Type type2 = new TypeToken<ArrayList<String>>(){}.getType();
        String json2 = gson2.toJson(mArrayString, type2);

        Gson gson3 = new Gson();
        Type type3 = new TypeToken<ArrayList<Boolean>>(){}.getType();
        String json3 = gson3.toJson(mArrayBool, type3);
         */

        SharedPreferences prefs = getSharedPreferences(sharedPref, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putString("positionID", json);

        /*
        prefsEditor.putString("mArrayString", json2);
        prefsEditor.putString("mArrayBool", json3);
         */

        prefsEditor.apply();
    }

    public void loadHashMap() {
        SharedPreferences prefs = getSharedPreferences(sharedPref, MODE_PRIVATE);
        String json = prefs.getString("positionID", null);
        String json2 = prefs.getString("mArrayString", null);
        String json3 = prefs.getString("mArrayBool", null);
        if ((json != null) && (json2 != null) && (json3 != null)) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<Long, Boolean>>(){}.getType();
            positionID = gson.fromJson(json, type);

            /*
            Gson gson2 = new Gson();
            Type type2 = new TypeToken<ArrayList<String>>(){}.getType();
            mArrayString = gson2.fromJson(json2, type2);

            Gson gson3 = new Gson();
            Type type3 = new TypeToken<ArrayList<Boolean>>(){}.getType();
            mArrayBool = gson3.fromJson(json3, type3);
             */
        }
    }

    public void setmArrays() {
        mArrayString.add("Vibrations");
        mArrayString.add("Alarm Sound");

        mArrayBool.add(true);
        mArrayBool.add(true);
    }
}