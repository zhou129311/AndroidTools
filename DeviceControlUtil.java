package com.broadsense.httpserver.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 控制设备工具类
 * time : 2016/1/18
 */
public class DeviceControlUtil {


	/**
	 * @param dirPath
	 * @return 查看目录下面是否有OTA包
	 */
	public static String getOTAName(String dirPath){
		File [] files = getFillList(dirPath);
		if(files == null) return null;
		if(files.length == 1) return files[0].getName();
		else return null;
	}

	/**
	 * @return 返回一个目录下面所有文件
	 */
	private static File[] getFillList(String dirPath) {
		File file = new File(dirPath);
		if(file.exists() && file.isDirectory()) return file.listFiles();
		else return null;
	}

	/**
	 * 系统升级
	 * prams file  升级所需的文件
	 */
	public static void recoverySystem(Context context,File file){
		try {
			RecoverySystem.installPackage(context, file);
		} catch (IOException e) {
			e.printStackTrace();
			MyLog.i("zx","系统升级失败!" + e.getMessage());
		}
	}


	/**
	 * @param dir 指定目录
	 * @return 创建指定目录路径
	 */
	public static String createDir(String dir) {
		String fullPath = getDirBasePath(dir);
		File f = new File(fullPath);
		if (!f.exists()) {
			f.mkdir();
		}
		return fullPath;
	}

	/**
	 * @param dirName 目录名
	 * @return 返回SD卡中创建的目录路径
	 */
	private static String getDirBasePath(String dirName) {
		if (hasSdcard()) {
			return Environment.getExternalStorageDirectory().getPath() + File.separator + dirName;
		} else {
			return File.separator + dirName;
		}
	}

	/**
	 * 获取应用版本号
	 * @return 当前应用的版本号
	 */
	public String getVersion(Context context,String packageName) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(packageName, 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.e("zx","APP getVersion error :" + e.getMessage());
			return "找不到该应用";
		}
	}

	/**
	 * 恢复出厂设置 默认不格式化SD卡
	 * @param
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void masterClear(Context context) {
		//Intent intent = new Intent("com.android.internal.os.storage.FORMAT_AND_FACTORY_RESET");
		Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
		intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
		intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		/*ComponentName COMPONENT_NAME
				= new ComponentName("android", "com.android.internal.os.storage.ExternalStorageFormatter");*/
		//intent.setComponent(COMPONENT_NAME);
		//context.startService(intent);
		Log.d("zx", "send broadcast ACTION_MASTER_CLEAR");
		context.sendBroadcast(intent);
	}


	/**
	 * @param context 启动系统Server格式化SD卡
	 */
	public static void formatSDCard(Context context){
		Intent intent = new Intent("com.android.internal.os.storage.FORMAT_ONLY");
		ComponentName COMPONENT_NAME
				= new ComponentName("android", "com.android.internal.os.storage.ExternalStorageFormatter");
		intent.setComponent(COMPONENT_NAME);

		// Transfer the storage volume to the new intent
		/*final StorageVolume storageVolume = getIntent().getParcelableExtra(
				StorageVolume.EXTRA_STORAGE_VOLUME);
		intent.putExtra(StorageVolume.EXTRA_STORAGE_VOLUME, storageVolume);*/
		context.startService(intent);
	}


	/**
	 * @return 获取SD卡路径数组
	 */
	public static String[] getVolumePaths(Context context) {
		String[] paths = null;
		StorageManager mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
		try {
			Method mMethodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths");
			paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
		} catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return paths;
	}

	/*public static int formatStorage(String path) {
		IMountService iMountService= getMountService();
		int a = -1;
		try {
			iMountService.unmountVolume(path, true, false);
			SystemClock.sleep(4000);
			a=iMountService.formatVolume(path);
			SystemClock.sleep(30000);
			iMountService.mountVolume(path);
			SystemClock.sleep(4000);
			Log.d("bucuo", "Mount Success!");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return a;

	}*/

	/*public static IMountService getMountService() {

		try {
			Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
			IBinder binder = (IBinder) method.invoke(null, "mount");
			IMountService iMountService = IMountService.Stub.asInterface(binder);
			return iMountService;
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("zx","IMountService not found");
		}
	}*/

	/**
	 * 直接调用短信接口发短信
	 * @param phoneNumber
	 * @param message
	 */
	public static void sendSMS(Context context,String phoneNumber,String message){
		//处理返回的发送状态
		Intent sentIntent = new Intent("android.broadsense.patron.SENT_SMS_ACTION");
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, 0);
		//处理返回的接收状态
		// create the deilverIntent parameter
		Intent deliverIntent = new Intent("android.broadsense.patron.DELIVERED_SMS_ACTION");
		PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0, deliverIntent, 0);
		//获取短信管理器
		android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
	}

	/**
	 * @param context
	 * @param trafficStats 保存当前使用流量信息
	 */
	public static void saveTrafficStats(Context context,String trafficStats){
		SharedPreferences preferences = context.getSharedPreferences("trafficStats", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("trafficStats", trafficStats);
		editor.apply();
	}


	/**
	 * @return 系统版本和时间
	 */
	public static String getSysVersion(){
		return Build.DISPLAY + "|" + Build.TIME;
	}


	/**
	 * @param pkgName 使用su命令杀掉进程
	 */
	public static void forceStopAPK(List<String> pkgName){
		Process sh = null;
		DataOutputStream os = null;

		try {
			for(int i = 0;i < pkgName.size();++i){
				sh = Runtime.getRuntime().exec("su");
				os = new DataOutputStream(sh.getOutputStream());
				final String Command = "am force-stop " + pkgName.get(i) + "\n";
				os.writeBytes(Command);
				os.flush();
				sh.waitFor();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			Log.i("zx", "forceStopAPK error:" + e);
		}
	}

	/**
	 * 清理缓存，杀掉无用进程
	 */
	private static void forceStopPackage(Context context, String packageName) throws Exception{
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		try{
			Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
			method.invoke(am, packageName);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("zx", "error : " + e);
		}
	}


	/**
	 * @return 杀掉系统当前正在运行的进程, 排除白名单之中的进程
	 */
	public static void killRunningProcess(Context context,List<String> packageName){
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		//获取正在运行的应用
		List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
		for(ActivityManager.RunningAppProcessInfo ra : run){
			if(ra.processName.equals("system") || ra.processName.equals("com.android.phone")){
				continue;
			}
			Log.i("zx", "-->" + ra.processName);
			for(int i = 0;i < (packageName != null ? packageName.size() : 0);++i){
				if(!ra.processName.equals(packageName.get(i))){
					try {
						forceStopPackage(context, ra.processName);
					} catch (Exception e) {
						e.printStackTrace();
						MyLog.e("zx", "error : " + e);
					}
				}else{
					Log.i("zx", "白名单进程-->" + ra.processName);
				}
			}
		}
	}

	/**
	 * @param currenttime 修改系统时间
	 */
	public static boolean setSystemTime(long currenttime){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = new Date(currenttime);
		String s = dateFormat.format(date);
		Log.i("time","------>" + currenttime + "\n" + s);
		return  SystemClock.setCurrentTimeMillis(currenttime);
	}

	/**
	 * @return 系统总内存 API-15
	 */
	 public static String getTotalMemory(Context context) throws Exception{
		 String str1 = "/proc/meminfo";// 系统内存信息文件
		 String str2;
		 String[] arrayOfString;
		 long initial_memory = 0;
		 try {
			 FileReader localFileReader = new FileReader(str1);
			 BufferedReader localBufferedReader = new BufferedReader(
					 localFileReader, 8192);
			 str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
			 arrayOfString = str2.split("\\s+");
			 for (String num : arrayOfString) {
				 Log.e(str2, num + "\t");
			 }
			 initial_memory = Integer.valueOf(arrayOfString[1]) << 10;// 获得系统总内存，单位是KB，转换成Byte
			 localBufferedReader.close();
		 } catch (IOException e) {
			 Log.e("zx", "error: " + e);
		 }
		 return Formatter.formatFileSize(context, initial_memory);
	 }

	/**
	 * @return 系统可用内存
	 */
	public static String getAvailMemory(Context context) throws Exception{
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		return Formatter.formatFileSize(context, mi.availMem);//mi.availMem 系统可用内存
	}

	
	/**
	 * @param path 当前文件夹
	 * @return 获取当前文件夹大小
	 */
	public static String getFileSize(Context context,String path){
		File file = new File(path);
        long fileSize = 0;
		if(file.isDirectory()&&file.exists()){
            try {
                fileSize = getFileSize(file);
            } catch (Exception e) {
				MyLog.i("zx","error: " + e.getMessage());
                e.printStackTrace();
            }
            return Formatter.formatFileSize(context,fileSize);
        }
		return "文件不存在或者非目录";
	}

    public static long getFileSize(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
		for (File aFlist : flist) {if (aFlist.isDirectory()) {size += getFileSize(aFlist);} else {size += aFlist.length();}}
        return size;
    }

	/**
	 * @return 最大音量
	 */
	public static int getMaxVolume(Context context) {
		AudioManager audio = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		return audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * @return 当前音量
	 */
	public static int getcuurentVolume(Context context) {
		AudioManager audio = (AudioManager) context
				.getSystemService(Service.AUDIO_SERVICE);
		return audio.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * @param lv 设置音量
	 */
	public static void setVolume(Context context,int lv){
		AudioManager audio = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
		int maxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int maxsVol = audio.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		int maxrVol = audio.getStreamMaxVolume(AudioManager.STREAM_RING);
		int maxvVol = audio.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		int maxaVol = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		int maxnVol = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
//		audio.setMicrophoneMute(false); //关闭麦克风音量
		float system = maxsVol*((float)lv/maxVol);
		float ring = maxrVol*((float)lv/maxVol);
		float voiCall = maxvVol*((float)lv/maxVol);
		float alarm = maxaVol*((float)lv/maxVol);
		float notif = maxnVol*((float)lv/maxVol);
		Log.i("zx","Maxsystem = " +maxsVol + ",Maxring = " +maxrVol + ",MaxvoiCall = " +maxvVol + ",Maxalarm = " + maxaVol+ ",Maxnotif = " +maxnVol );
		Log.i("zx","system = " +floatToInt(system) + ",ring = " +floatToInt(ring) + ",voiCall = " +floatToInt(voiCall) + ",alarm = " + floatToInt(alarm)+ ",notif = " +floatToInt(notif));
		audio.setStreamVolume(AudioManager.STREAM_MUSIC,lv,AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_SYSTEM, floatToInt(system),AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_RING, floatToInt(ring),AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_RING, floatToInt(voiCall),AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_RING,floatToInt(alarm),AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_RING, floatToInt(notif),AudioManager.FLAG_PLAY_SOUND);
		Log.i("zx", "设置音量 : " + lv);
	}


	/**
	 * @return float四舍五入转int
	 */
	public static int floatToInt(float f){
		int i;
		if(f>0) //正数
			i = (int) ((f*10 + 5)/10);
		else if(f<0) //负数
			i = (int) ((f*10 - 5)/10);
		else i = 0;
		return i;
	}

	/**
	 * @return SD卡可用容量
	 */
	@SuppressLint("NewApi")
	public static String getSDAvailableSize(Context context) {
		if (hasSdcard()) {
			String path = null;
			String [] s = getVolumePaths(context);
			if(s.length > 1){
				path = s[1];
			} else {
				path = s[0];
			}
			StatFs stat = new StatFs(path);
			return Formatter.formatFileSize(context, stat.getAvailableBytes());
		}
		return "无SD卡";
	}

	/**
	 * @return SD卡总容量
	 */
	@SuppressLint("NewApi")
	public static String getSDTotalSize(Context context) {
		if (hasSdcard()) {
			String path = null;
			String [] s = getVolumePaths(context);
			if(s.length > 1){
				path = s[1];
			}else {
				path = s[0];
			}
			StatFs stat = new StatFs(path);
			return Formatter.formatFileSize(context, stat.getTotalBytes());
		}
		return "无SD卡";
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static long getSDBlockSize(Context context){
		if(hasSdcard()){
			String path = null;
			String [] s = getVolumePaths(context);
			if(s.length > 1){
				path = s[1];
			}else {
				path = s[0];
			}
			StatFs stat = new StatFs(path);
			return stat.getTotalBytes() - stat.getAvailableBytes();
		}
		return 0;
	}

	/**
	 * @return 判断是否有SD卡
	 */
	public static boolean hasSdcard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

}
