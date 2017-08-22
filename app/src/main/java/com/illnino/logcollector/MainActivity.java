package com.illnino.logcollector;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    Button btnSaveLog;
    TextView tvStatus;
    String pathFile;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSaveLog = (Button) findViewById(R.id.btnSaveLog);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        btnSaveLog.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view == btnSaveLog){
            saveLog();
        }
    }

    private void fakeLog(){
        for (int i = 0; i < 300; i++) {
            Log.d(TAG, "fakeLog: " + i);
        }
    }

    private void saveLog() {

        fakeLog();

        Calendar c = Calendar.getInstance();

        // If you need just only one file per day just change date format to yyyy-MM-dd
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        fileName = df.format(c.getTime()) + "_logcollector_applog.log";
        pathFile = Environment.getExternalStorageDirectory().toString();

        File file = new File(pathFile, fileName);

        if (file.exists()) {
            file.delete();
        }

        int pid = android.os.Process.myPid();
        try {
            String command = String.format("logcat -d -v threadtime *:*");
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String currentLine = null;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine != null && currentLine.contains(String.valueOf(pid))) {
                    result.append(currentLine);
                    result.append("\n");
                }
            }

            FileWriter out = new FileWriter(file);
            out.write(result.toString());
            out.close();

            showStatusSaved(true, null);

        } catch (IOException ex) {
            ex.printStackTrace();
            showStatusSaved(false, ex.getMessage());
        }

        try {
            Runtime.getRuntime().exec("logcat -c");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showStatusSaved(boolean isSaved, String exception){

        String txt = "";

        if (!isSaved){
            txt = " Fail !!, " + (exception == null ? "" : exception);
        }else{
            txt = " Success !!, " +  pathFile + "/ " + fileName;
        }

        tvStatus.setText(txt);
    }
}
