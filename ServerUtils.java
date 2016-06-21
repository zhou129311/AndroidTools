package com.broadsense.testflowandpower.util;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.broadsense.testflowandpower.service.UpDateDBService;
import com.broadsense.testflowandpower.service.UpLoadServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2016/5/5 0005.
 */
public class ServerUtils {

    /**
     * @param context 上下文
     * @param className 服务全类名
     * @return 当前是否运行
     */
    public static boolean isRunningServer(Context context,String className){
        boolean isRunning = false;
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> infos = activityManager.getRunningServices(50);
            if(infos == null || infos.size() == 0){
                return false;
            }
            for(ActivityManager.RunningServiceInfo info : infos){
                if(info.service.getClassName().contains(className)){
                    isRunning = true;
                    break;
                }
            }
        }catch (Exception e){
            Log.e("ServerUtils",e.getMessage());
        }
        return isRunning;
    }

    /**
     * AlarmManager.setWindow + RTC_WAKEUP和setRepeating + RTC_WAKEUP 存在误差延时问题
     * 改用AlarmManager.setExact + RTC_WAKEUP
     *
     * @param context 定时添加一条流量数据
     */
    public static void invokeTimerServer(Context context){
        PendingIntent pendingIntent;
        Intent intent = new Intent(context,UpDateDBService.class);
        intent.setAction(Constant.UPDATE_SERVER_ACTION);
        intent.putExtra("insert", "insert");
        try {
            pendingIntent = PendingIntent.getService(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.i("ServerUtils","invokeTimerServer alarmManager.setExact");
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constant.BROADCAST_UPDATE_TIME_DELAY, pendingIntent);
            }else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constant.BROADCAST_UPDATE_TIME_DELAY, pendingIntent);
                Log.i("ServerUtils", "invokeTimerServer alarmManager.set");
            }
        } catch (Exception e){
            Log.e("ServerUtils","invokeTimerServer ERROR:　" +e.getMessage());
        }
    }

    /**
     * @param context 定时上传任务
     */
    public static void invokeUpLoadServer(Context context){
        PendingIntent pendingIntent;
        Intent intent = new Intent(context,UpLoadServer.class);
        intent.setAction(Constant.UPLOAD_SERVER_ACTION);
        try {
            pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.i("ServerUtils","invokeUpLoadServer alarmManager.setExact");
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constant.TIMER, pendingIntent);
            }else{
                Log.i("ServerUtils","invokeUpLoadServer alarmManager.set");
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constant.TIMER, pendingIntent);
            }
        }catch (Exception e){
            Log.e("ServerUtils", "invokeUpLoadServer ERROR:　" + e.getMessage());
        }
    }

    /**
     *
     * @return 是否处于WIFI网络
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }


    /**
     * 保存耗电量信息
     */
    public static void saveBatteryStats(){
        Process sh = null;
        DataOutputStream os = null;
        try {
            sh = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(sh.getOutputStream());
            final String Command = "dumpsys batterystats > /storage/sdcard0/battery.txt \n";
            os.writeBytes(Command);
            os.flush();
            sh.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("zx", "error:" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("zx", "InterruptedException error:" + e.getMessage());
        }
    }

}
