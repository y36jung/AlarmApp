package com.midmad1.alarmapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.midmad1.alarmapp.MainActivity.mArrayBool;
import static com.midmad1.alarmapp.MainActivity.mArrayString;

public class SetAlarmAdapter extends RecyclerView.Adapter<SetAlarmAdapter.SetAlarmViewHolder> {
    private Context mContext;
    private String TAG = "SetAlarmAdapter";

    public SetAlarmAdapter (Context context) {
        mContext = context;
    }

    public class SetAlarmViewHolder extends RecyclerView.ViewHolder {
        public TextView optionView;
        public Switch onOff;

        public SetAlarmViewHolder(@NonNull View itemView) {
            super(itemView);

            optionView = itemView.findViewById(R.id.optionName);
            onOff = itemView.findViewById(R.id.switch2);
        }
    }

    @NonNull
    @Override
    public SetAlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.custom_alarm_set_view, parent, false);
        return new SetAlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetAlarmViewHolder holder, final int position) {
        if ((mArrayString == null) && (mArrayBool == null)) {
            return;
        }
        String optionName = mArrayString.get(position);
        Boolean optionBool = mArrayBool.get(position);

        holder.optionView.setText(optionName);
        holder.onOff.setChecked(optionBool);
        holder.onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mArrayBool.set(position, isChecked);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArrayString.size();
    }
}
