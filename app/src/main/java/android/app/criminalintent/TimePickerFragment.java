package android.app.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePickerFragment extends DialogFragment {
    public static final String EXTRA_TIME = "android.app.criminalintent.time";
    private static final String ARG_TIME = "time";
    private TimePicker mTimePicker;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePicker timePicker = (TimePicker) getArguments().getSerializable(ARG_TIME);
        Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);
        mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_picker);
        mTimePicker.setHour(hour);
        mTimePicker.setMinute(minute);
        mTimePicker.setOnTimeChangedListener(null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_of_crime)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int hour = mTimePicker.getHour();
                                int minute = mTimePicker.getMinute();

                                Time time = new Time(hour, minute, 0);
                                sendResult(Activity.RESULT_OK, time);
                            }
                        })
                .create();


    }
    public static TimePickerFragment newInstance(Time time){
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, time);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void sendResult(int resultCode, Time time){
        if(getTargetFragment() == null)
            return;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, time);
        getTargetFragment().onActivityResult(
                getTargetRequestCode(), resultCode, intent);
    }
}
