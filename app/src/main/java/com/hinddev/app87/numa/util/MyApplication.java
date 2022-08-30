package com.hinddev.app87.numa.util;

import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            String fileName = "logcat_" + System.currentTimeMillis() + ".txt";

            File outputFile = new File(this.getExternalCacheDir(), fileName);
           Runtime.getRuntime().exec("logcat -f" + outputFile.getAbsolutePath());



        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
