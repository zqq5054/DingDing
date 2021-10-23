package com.kunluiot.iot.dingdingservice;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String SERVICE_NAME = "com.kunluiot.iot.dingdingservice/.HelpService";
    private AccessibilityManager mAccessibilityManager;
    private CheckBox checkBox;
    private CheckBox sunDay;
    private ImageView imSta;
    private ImageView imSun;
    private TextView startTime;
    private TextView endTime;
    private int startHour = 0;
    private int startMinute = 0;
    private int endHour = 0;
    private int endMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAccessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        findViewById(R.id.start_service).setOnClickListener((view) -> {

            checkEnabledAccessibilityService();
        });
        imSta = findViewById(R.id.imv_sta);
        imSun = findViewById(R.id.imv_sun);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        SharedPreferences sharedPreferences = getSharedPreferences("data", 0);
        long lastOpen = sharedPreferences.getLong("lastOpen", -1);
        if (lastOpen == -1) {
            sharedPreferences.edit().putLong("lastOpen", System.currentTimeMillis()).commit();
        }
        boolean isCh = sharedPreferences.getBoolean("needStaday", true);
        boolean isSun = sharedPreferences.getBoolean("needSunday", false);
        checkBox = findViewById(R.id.needStaday);
        sunDay = findViewById(R.id.needSunday);
        checkBox.setChecked(isCh);
        sunDay.setChecked(isSun);

        if (isCh) {
            imSta.setColorFilter(Color.parseColor("#ffffff"));
        } else {
            imSta.setColorFilter(Color.parseColor("#888888"));
        }

        if (isSun) {
            imSun.setColorFilter(Color.parseColor("#ffffff"));
        } else {
            imSun.setColorFilter(Color.parseColor("#888888"));
        }

        imSta.setOnClickListener(v -> {
            boolean isNeed = sharedPreferences.getBoolean("needStaday", true);
            if (isNeed) {
                imSta.setColorFilter(Color.parseColor("#888888"));
                Toast.makeText(this, "周六打卡已关闭", Toast.LENGTH_SHORT).show();
            } else {
                imSta.setColorFilter(Color.parseColor("#ffffff"));
                Toast.makeText(this, "周六打卡已启用", Toast.LENGTH_SHORT).show();
            }
            isNeed = !isNeed;
            sharedPreferences.edit()
                    .remove("lastHour")
                    .remove("lastMinute")
                    .putBoolean("needStaday", isNeed).commit();

        });
        imSun.setOnClickListener(v -> {
            boolean isNeed = sharedPreferences.getBoolean("needSunday", true);
            if (isNeed) {
                imSun.setColorFilter(Color.parseColor("#888888"));
                Toast.makeText(this, "周日打卡已关闭", Toast.LENGTH_SHORT).show();
            } else {
                imSun.setColorFilter(Color.parseColor("#ffffff"));
                Toast.makeText(this, "周日打卡已启用", Toast.LENGTH_SHORT).show();
            }
            isNeed = !isNeed;

            sharedPreferences.edit()
                    .remove("lastHour")
                    .remove("lastMinute")
                    .putBoolean("needSunday", isNeed)
                    .commit();

        });

        startHour = sharedPreferences.getInt("startHour", 9);
        startMinute = sharedPreferences.getInt("startMinute", 00);
        endHour = sharedPreferences.getInt("endHour", 18);
        endMinute = sharedPreferences.getInt("endMinute", 00);
        startTime.setText(String.format("%02d", startHour) + ":" + String.format("%02d", startMinute));
        endTime.setText(String.format("%02d", endHour) + ":" + String.format("%02d", endMinute));
        findViewById(R.id.linearLayout).setOnClickListener(v -> {
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


                    sharedPreferences.edit()
                            .putInt("startHour", hourOfDay)
                            .putInt("startMinute", minute)
                            .remove("lastHour")
                            .remove("lastMinute")
                            .commit();
                    startHour = hourOfDay;
                    startMinute = minute;
                    startTime.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));

                }
            }, startHour, startMinute, true).show();
        });


        findViewById(R.id.linearLayout2).setOnClickListener(v -> {
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    endHour = hourOfDay;
                    endMinute = minute;
                    sharedPreferences.edit()
                            .putInt("endHour", hourOfDay)
                            .putInt("endMinute", minute)
                            .remove("lastHour")
                            .remove("lastMinute")
                            .commit();
                    endTime.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));

                }
            }, endHour, endMinute, true).show();
        });

        checkEnabledAccessibilityService();
    }

    private boolean checkEnabledAccessibilityService() {

        List<AccessibilityServiceInfo> accessibilityServices =
                mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);

        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(SERVICE_NAME)) {

                findViewById(R.id.start_service).setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "服务正在运行中", Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        Toast.makeText(this, "请在无障碍服务中勾选本应用", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        return false;
    }


}