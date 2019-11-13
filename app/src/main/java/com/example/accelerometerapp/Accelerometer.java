package com.example.accelerometerapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Accelerometer {
    public static final String TAG = "AccelerometerApp";

    private final int FRAC_2d1 = 5000;
    private final int FRAC_2d2 = 2500;
    private final int FRAC_2d3 = 1250;
    private final int FRAC_2d4 = 625;
    private final int FRAC_2d5 = 313;
    private final int FRAC_2d6 = 156;
    private final int FRAC_2d7 = 78;
    private final int FRAC_2d8 = 39;
    private final int FRAC_2d9 = 20;
    private final int FRAC_2d10 = 10;
    private final int FRAC_2d11 = 5;
    private final int FRAC_2d12 = 2;

    public float accelData[] = new float[]{0.0f, 0.0f, 0.0f};
    public boolean zAxisStictionIssue = false;

    public Accelerometer() {
        this.accelData[0] = 0.0f;
        this.accelData[1] = 0.0f;
        this.accelData[2] = 0.0f;

        readAccelData();
    }

    void readAccelData(){
        int bufferSize = 16;
        byte[] accel_data = new byte[bufferSize];
        short accel_temp;
        zAxisStictionIssue = false;

        try {
            InputStream inputStream = new FileInputStream("/dev/vaccel");

            while ((inputStream.read(accel_data)) != -1) {
                break;
            }

            // Devices with z-axis failure usually report 0x8000 0x7ff0
            if ((accel_data[13] == (byte)0x80 && accel_data[12] == (byte)0x0) || (accel_data[13] == (byte)0x7f && accel_data[12] == (byte)0xf0)) {
                Log.e(TAG, "Z-Axis Stiction Issue!!!");
                zAxisStictionIssue = true;
            }

            if(accel_data[14] != 0 || accel_data[15] != 0) {
                Log.e(TAG, "accel_data doesn't end in zeroes");
            }

            accel_temp = (short)(((short)accel_data[9]<<8 | ((short)accel_data[8] & 0x00ff)));
            this.accelData[0] = get_g_val(accel_temp);

            accel_temp = (short)(((short)accel_data[11]<<8) | ((short)accel_data[10] & 0x00ff));
            this.accelData[1] = get_g_val(accel_temp);

            accel_temp = (short)(((short)accel_data[13]<<8) | ((short)accel_data[12] & 0x00ff));
            this.accelData[2] = get_g_val(accel_temp);

        } catch (FileNotFoundException e) {
            Log.e(TAG, " File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, " Cannot read file: " + e.toString());
        }
    }

    String[] getAccelerometerRegisters() {
        String[] registers = new String[3];

        registers[0] = checkOffset(new String[] {"mctl", "api", "02182F"});
        registers[1] = checkOffset(new String[] {"mctl", "api", "021830"});
        registers[2] = checkOffset(new String[] {"mctl", "api", "021831"});

        return registers;
    }

    private String checkOffset(String[] command) {
        String offsetValue = "";

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = in.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            in.close();

            // Waits for the command to finish.
            process.waitFor();

            // Handle string
            String outputString = output.toString();
            Log.d(TAG, "Output from command: " + output.toString());

            String[] strArr = outputString.replace(" ", "").split("(:)|(,)");
            offsetValue = strArr[2];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offsetValue;
    }

    private float get_fraction(short val) {
        int fraction = 0;
        float SCALE = 10000;

        if((val & 0x8000) == 0x8000) fraction += FRAC_2d1;
        if((val & 0x4000) == 0x4000) fraction += FRAC_2d2;
        if((val & 0x2000) == 0x2000) fraction += FRAC_2d3;
        if((val & 0x1000) == 0x1000) fraction += FRAC_2d4;

        if((val & 0x0800) == 0x0800) fraction += FRAC_2d5;
        if((val & 0x0400) == 0x0400) fraction += FRAC_2d6;
        if((val & 0x0200) == 0x0200) fraction += FRAC_2d7;
        if((val & 0x0100) == 0x0100) fraction += FRAC_2d8;

        if((val & 0x0080) == 0x0080) fraction += FRAC_2d9;
        if((val & 0x0040) == 0x0040) fraction += FRAC_2d10;

        return ((float) fraction / SCALE);
    }

    private float get_g_val(short val) {
        int hi_byte;
        short temp;
        float gVal;

        hi_byte = ((val&0xfffc) & 0xff00) >> 8;
        temp = val;
        if(hi_byte > 0x7f) {
            temp = (short) ((~temp & 0xffff)+1);
            hi_byte = (temp & 0xff00) >> 8;
            gVal = (hi_byte & 0x70) >> 4;
            gVal += get_fraction((short) (temp << 4));
            gVal *= -1;
        }
        else {
            gVal = (hi_byte & 0x70) >> 4;
            gVal += get_fraction((short) (temp << 4));
        }

        return gVal;
    }
}
