package com.kunluiot.iot.dingdingservice;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Calendar;
import java.util.List;

import androidx.annotation.RequiresApi;


public class HelpService extends AccessibilityService {


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        SharedPreferences sharedPreferences = getSharedPreferences("data", 0);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
//        if (isLockScreenOn()) {
//
//            return;
//        }
        try {


            if ("com.alibaba.android.rimet".equals(event.getPackageName())) {
                sharedPreferences.edit().putInt("lastMinute", minute)
                        .putInt("lastHour", hour)
                        .putLong("lastOpen", System.currentTimeMillis())
                        .commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        boolean isCh = sharedPreferences.getBoolean("needStaday", true);
        boolean isSun = sharedPreferences.getBoolean("needSunday", false);
        int startHour = sharedPreferences.getInt("startHour", 9);
        int startMinute = sharedPreferences.getInt("startMinute", 00);
        int endHour = sharedPreferences.getInt("endHour", 18);
        int endMinute = sharedPreferences.getInt("endMinute", 00);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if ((dayOfWeek == 7 && !isCh) || (dayOfWeek == 1 && !isSun)) {

            return;
        }

        long lastOpen = sharedPreferences.getLong("lastOpen", 0);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, startHour);
        c.set(Calendar.MINUTE, startMinute - 10);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() > c.getTimeInMillis() && lastOpen < c.getTimeInMillis()) {

            openDingDing();

        }

        c.set(Calendar.HOUR_OF_DAY, endHour);
        c.set(Calendar.MINUTE, endMinute);

        if (calendar.getTimeInMillis() > c.getTimeInMillis() && lastOpen < c.getTimeInMillis()) {

            openDingDing();

        }


    }

    private void openDingDing() {


        try {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.alibaba.android.rimet", "com.alibaba.android.rimet.biz.LaunchHomeActivity");
            try {
                intent.setComponent(cn);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                //TODO  可以在这里提示用户没有安装应用或找不到指定Activity，或者是做其他的操作
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }



    @Override
    public void onInterrupt() {
    }

    //判断是否是锁屏状态
    public boolean isLockScreenOn() {
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
            return true;
        } else {
            return false;
        }
    }
}
