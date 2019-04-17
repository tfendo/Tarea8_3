package com.hugoguillin.notificationscheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private JobScheduler mScheduler;
    private static final int JOB_ID = 0;
    private Switch reposo;
    private Switch cargando;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        reposo = findViewById(R.id.idleSwitch);
        cargando = findViewById(R.id.chargingSwitch);
        seekBar = findViewById(R.id.seekBar);
        final TextView seekLabel = findViewById(R.id.seekBarProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress > 0){
                    seekLabel.setText(progress + " S");
                }else{
                    seekLabel.setText("Not set");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void scheduleJob(View view) {
        RadioGroup group = findViewById(R.id.networkOptions);
        int idSeleccionado = group.getCheckedRadioButtonId();
        int opcionSeleccionada = JobInfo.NETWORK_TYPE_NONE;
        int i = seekBar.getProgress();
        boolean seekBol = i > 0;

        switch (idSeleccionado){
            case R.id.noNetwork:
                opcionSeleccionada = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                opcionSeleccionada = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.wifiNetwork:
                opcionSeleccionada = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        ComponentName servicio = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, servicio)
                .setRequiredNetworkType(opcionSeleccionada)
                .setRequiresDeviceIdle(reposo.isChecked())
                .setRequiresCharging(cargando.isChecked());

        if(seekBol){
            builder.setOverrideDeadline(i * 1000);
        }

        boolean constraintSet = opcionSeleccionada != JobInfo.NETWORK_TYPE_NONE
                || reposo.isChecked() || cargando.isChecked() || seekBol;

        if(constraintSet){
            JobInfo myJobInfo = builder.build();
            mScheduler.schedule(myJobInfo);
            Toast.makeText(this, getString(R.string.job_programado),
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, getString(R.string.job_no_programado),
                    Toast.LENGTH_LONG).show();
        }


    }

    public void cancelJobs(View view) {
        if(mScheduler != null){
            mScheduler.cancelAll();
            mScheduler = null;
            Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_LONG).show();
        }
    }
}
