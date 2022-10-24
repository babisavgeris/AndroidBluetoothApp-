package com.babis.bluetoothclient;

import android.app.job.JobParameters;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class JobService extends android.app.job.JobService {

    private static final String TAG = "JobService";
    private boolean jobCancelled = false; //is false automatically

    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job Started");
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                myBluetoothAdapter.startDiscovery();
                Log.d(TAG, "Bluetooth discovery started ");

                Log.d(TAG, "Job finished, reschedule = false");
                jobFinished(params, false);

            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }
}
