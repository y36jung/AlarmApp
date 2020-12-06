package com.midmad1.alarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.midmad1.alarmapp.MainActivity.alarmID;
import static com.midmad1.alarmapp.MainActivity.mDatabase;
import static com.midmad1.alarmapp.MainActivity.positionID;


public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private String TAG = "AlarmAdapter";

    public AlarmAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class AlarmViewHolder extends RecyclerView.ViewHolder {
        public TextView eventText;
        public TextView timeText;
        public TextView dateText;
        public Switch onOff;

        public AlarmViewHolder(@NonNull final View itemView) {
            super(itemView);

            eventText = itemView.findViewById(R.id.eventText);
            timeText = itemView.findViewById(R.id.timeText);
            dateText = itemView.findViewById(R.id.dateText);
            onOff = itemView.findViewById(R.id.switch1);
        }
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.alarm_item_view, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AlarmViewHolder holder, final int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        String eventName = mCursor.getString(mCursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_EVENT));
        long alarmLong = mCursor.getLong(mCursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_ALARM));
        Calendar alarmCalendar = Calendar.getInstance();
        alarmCalendar.setTimeInMillis(alarmLong);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String alarmName = "" + sdf.format(alarmCalendar.getTime());
        String dateName = alarmCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " "
                + alarmCalendar.get(Calendar.DAY_OF_MONTH) + ", " + alarmCalendar.get(Calendar.YEAR);
        long id = mCursor.getLong(mCursor.getColumnIndex(AlarmContract.AlarmEntry._ID));

        holder.eventText.setText(eventName);
        holder.timeText.setText(alarmName);
        holder.dateText.setText(dateName);
        holder.itemView.setTag(id);
        if (!positionID.isEmpty()) {
            holder.onOff.setChecked(positionID.get(id));
        }
        holder.onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                long currId = (long) holder.itemView.getTag();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    positionID.replace(currId, isChecked);
                } else {
                    positionID.remove(currId);
                    positionID.put(currId, isChecked);
                }
                assignToggledAlarm();
                Toast.makeText(mContext, positionID.get(currId) + " " + currId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if(mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    public void startAlarm(long alarmLong, long id) {
        alarmID = id;
        Calendar alarm = Calendar.getInstance();
        alarm.setTimeInMillis(alarmLong);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmLong, pendingIntent);
        }
    }

    public void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public long nextToggledAlarm() {
        Cursor cursor = mDatabase.rawQuery("SELECT " + AlarmContract.AlarmEntry._ID +
                " FROM " + AlarmContract.AlarmEntry.TABLE_NAME, null);
        try {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(AlarmContract.AlarmEntry._ID));
                if (positionID.get(id)) {
                    return id;
                }
            }
        } finally {
            cursor.close();
        }
        return -2;
    }

    public void assignToggledAlarm() {
        long count = getItemCount();
        if (count > 0) {
            Log.e(TAG, "count > 0");
            boolean alarmUp = (PendingIntent.getBroadcast(mContext, 1,
                    new Intent(mContext, AlarmReceiver.class), 0) != null);
            if (alarmUp) {
                cancelAlarm();
                Log.e(TAG, "current alarm cancelled");
            }

            if (mCursor.moveToFirst()) {
                long nextToggled = nextToggledAlarm();
                if (nextToggled != -2) {
                    Cursor cursor = mDatabase.rawQuery("SELECT " + AlarmContract.AlarmEntry.COLUMN_ALARM
                            + " FROM " + AlarmContract.AlarmEntry.TABLE_NAME
                            + " WHERE " + AlarmContract.AlarmEntry._ID + "=" + nextToggled, null);
                    cursor.moveToFirst();
                    long alarmFirst = cursor.getLong(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_ALARM));
                    if (!positionID.isEmpty()) {
                        if (positionID.get(nextToggled)) {
                            startAlarm(alarmFirst, nextToggled);
                            Log.e(TAG, "alarmId = " + nextToggled + " has started");
                        }
                    }
                }
            }
        }
    }
}
