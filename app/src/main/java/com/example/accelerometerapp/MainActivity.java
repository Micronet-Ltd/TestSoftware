package com.example.accelerometerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "AccelerometerApp";
    private static final long DELAY = 500;

    ConstraintLayout background;
    TextView xValue;
    TextView yValue;
    TextView zValue;
    TextView xOffsetValue;
    TextView yOffsetValue;
    TextView zOffsetValue;
    TextView infoLabel;

    Accelerometer accelerometer;
    boolean continueRunning = true;
    Handler handler;
    boolean updateRegisterValues = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        background = findViewById(R.id.background);
        xValue = findViewById(R.id.xValue);
        yValue = findViewById(R.id.yValue);
        zValue = findViewById(R.id.zValue);
        xOffsetValue = findViewById(R.id.xOffset);
        yOffsetValue = findViewById(R.id.yOffset);
        zOffsetValue = findViewById(R.id.zOffset);
        infoLabel = findViewById(R.id.infoLabel);

        setTitle("Micronet Accelerometer App v" + BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        accelerometer = new Accelerometer();

        continueRunning = true;
        handler = new Handler();
        handler.post(updateDisplay);
    }

    @Override
    protected void onPause() {
        super.onPause();

        continueRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    Runnable updateDisplay = new Runnable() {
        @Override
        public void run() {
            updateDisplayedAccelerometerValues(accelerometer);

            if (continueRunning) {
                handler.postDelayed(updateDisplay, DELAY);
            }
        }
    };

    private void updateDisplayedAccelerometerValues(Accelerometer accelerometer) {
        accelerometer.readAccelData();

        xValue.setText(String.valueOf(accelerometer.accelData[0]));
        yValue.setText(String.valueOf(accelerometer.accelData[1]));
        zValue.setText(String.valueOf(accelerometer.accelData[2]));

        if (accelerometer.zAxisStictionIssue) {
            background.setBackgroundColor(Color.RED);
            infoLabel.setText("Z Axis Stiction Issue!!!!!");
        } else {
            background.setBackgroundColor(Color.WHITE);
            infoLabel.setText("");
        }

        // Update the register values every other time.
        if (updateRegisterValues) {
            String[] registers = accelerometer.getAccelerometerRegisters();
            xOffsetValue.setText(registers[0]);
            yOffsetValue.setText(registers[1]);
            zOffsetValue.setText(registers[2]);

            if (!registers[0].equals("0x0") || !registers[1].equals("0x0") || !registers[2].equals("0x0")) {
                background.setBackgroundColor(Color.RED);
                infoLabel.setText(infoLabel.getText() + "\nOffset Registers Should Be 0x0!!!");
            }
        }
        updateRegisterValues = !updateRegisterValues;

        Log.d(TAG, "Updated displayed accelerometer data.");
    }
}
