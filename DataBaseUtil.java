package com.broadsense.httpserver.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Formatter;
import android.util.Log;

import com.yysky.sdk.PhoneIdentifier;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * 查询记录仪数据库
 * time : 2016/1/18
 */
public class DataBaseUtil {
    public static final String DB_MEDIAFILE_TABLE_NAME = "MediaFile";
    public static final String AUTHORITY = "com.deepwits.patron.StorageManager";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_MEDIAFILE_TABLE_NAME);

    private static final String NORMAL_VIDEO = "media_type = 1 and event_type = 1";
    private static final String EVENT_VIDEO = "media_type = 1 and event_type = 2";
    private static final String PHOTO = "media_type = 2";


	/*public static String getOBD(){
        String obd = "{\"function\":\"0200\",\"deviceid\":\"H201508120001\",\"messagenum\":2854,\"date\":{\"time\":\"16年1月6日14时38分28秒\"" +
				",\"warningid\":\"0x0000\",\"carid\":\"0101\",\"secure\":{\"acc\":-1,\"defense\":1,\"footbrake\":-1,\"accelerator\":0," +
				"\"handbrake\":1,\"mainseatbelt\":-1,\"secondseatbelt\":-1},\"door\":{\"lf\":-1,\"rf\":-1,\"lb\":-1,\"rb\":0,\"trunk\"" +
				":-1,\"hood\":1},\"lock\":{\"lf\":0,\"rf\":0,\"lb\":0,\"rb\":-1},\"window\":{\"lf\":-1,\"rf\":-1,\"lb\":-1,\"rb\":-1," +
				"\"sky\":-1},\"light\":{\"leftsteer\":1,\"rightsteer\":-1,\"read\":-1,\"dippedheadlight\":-1,\"fullheadlight\":-1," +
				"\"frontfoglamp\":-1,\"rearfoglamp\":-1,\"hazardlight\":-1,\"backup\":-1,\"auto\":-1,\"widthlamp\":1},\"aswitch\":" +
				"{\"engineoil\":0,\"fueloil\":-1,\"windshieldwiper\":-1,\"trumpet\":0,\"aircondition\":0,\"rearviewmirror\":-1,\"" +
				"gears\":\"1\"},\"stream\":{\"voltage\":0,\"totaldistance\":0,\"totalburnoff\":0,\"rev\":0,\"speed\":0,\"flow\":0," +
				"\"airintaketemp\":-40,\"manifoldabsolutepressure\":0,\"troublelightstate\":true,\"troublelightnum\":141," +
				"\"coolanttemperature\":211,\"vehicleambienttemperature\":198,\"fuelpressure\":0,\"barometricpressure\":0,\"" +
				"valvepositionsensing\":0,\"throttlepedalposition\":0,\"engineruntime\":0,\"troublemileage\":0,\"remainl\":51,\"" +
				"engineload\":3,\"longtimefueltrim\":0,\"sparkadvanceangle\":192,\"instrumenttotalmileage\":0,\"vehiclerunningtotaltime\":0}}}";
		return obd;
	}*/

    /**
     * @return 返回配置信息
     */
    public static String getConfig(Context context) throws Exception {
        JSONObject config = new JSONObject();
        config.put("hasSdcard", DeviceControlUtil.hasSdcard());
        config.put("totalSize", DeviceControlUtil.getSDTotalSize(context));
        config.put("availSize", DeviceControlUtil.getSDAvailableSize(context));
        config.put("date", System.currentTimeMillis());
        config.put("availMemory", DeviceControlUtil.getAvailMemory(context));
        config.put("totalMemory", DeviceControlUtil.getTotalMemory(context));
        config.put("maxVolume", DeviceControlUtil.getMaxVolume(context));
        config.put("currentVolume", DeviceControlUtil.getcuurentVolume(context));
        config.put("otherVideoSize", getMediaSize(context, NORMAL_VIDEO));
        config.put("photoSize", getMediaSize(context, PHOTO));
        config.put("eventVideoSize", getMediaSize(context, EVENT_VIDEO));
        config.put("otherSize",getOtherSize(context));
        config.put("securityStatus", PatronConfigUtil.getParkStatus(context));
        config.put("security", PatronConfigUtil.getParkLv(context, 0));
        config.put("collision", PatronConfigUtil.getParkLv(context, 1));
        config.put("videoHD", PatronConfigUtil.getVideoHD(context));
        config.put("photoPX", PatronConfigUtil.getPhotoPX(context));
        config.put("repeatStatus", PatronConfigUtil.getIsRepeatRecord(context));
        config.put("videoStatus", PatronConfigUtil.getIsVideoRecord(context));
        config.put("audioStatus", PatronConfigUtil.getIsAudioRecord(context));
        return config.toString();
    }


    /**
     * @return 返回设备信息(UUID)
     * @throws Exception
     */
    public static String getDeviceInfo() throws Exception {
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("uuId", PhoneIdentifier.getInstance().getUuid());
        deviceInfo.put("protocol", "v1");
        deviceInfo.put("version", DeviceControlUtil.getSysVersion());
        String fileName = DeviceControlUtil.getOTAName(DeviceControlUtil.createDir("upload"));
        if (fileName == null) {
            deviceInfo.put("checkOTA", "null");
        } else {
            File file = new File(DeviceControlUtil.createDir("upload") + "/" + fileName);
            if (file.exists() && !file.isDirectory()) {
                deviceInfo.put("checkOTA", fileName + "|" + file.length());
            } else {
                deviceInfo.put("checkOTA", "null");
            }
        }
        Log.i("zx", "deviceInfo.toString()--->" + deviceInfo.toString());
        return deviceInfo.toString();
    }


    /**
     * @param context
     * @param path    文件路径
     * @return 根据路径获取ID
     */
    public static int getID(Context context, String path) {
        ContentResolver resolver = context.getContentResolver();
        StringBuilder where = new StringBuilder();
        String name = path.substring(path.lastIndexOf("/") + 1);
        where.append("path like '/%").append(name).append("'").append(" escape '/'");
        Cursor cursor = resolver.query(CONTENT_URI, new String[]{"id"}, where.toString(), null, null);
        if (cursor == null) return -1;
        int id;
        cursor.moveToFirst();
        id = cursor.getInt(cursor.getColumnIndex("id"));
        cursor.close();
        MyLog.e("Download", "Download Error: Not Found File!!! File Id = " + id);
        return id;
    }

    /**
     * @return 查询数据库媒体数据大小
     */
    public static String getMediaSize(Context context, String where) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(CONTENT_URI, new String[]{"sum(size)"}, where, null, null);
        if (cursor == null) return "0";
        cursor.moveToFirst();
        String size = Formatter.formatFileSize(context, cursor.getLong(0));
        cursor.close();
        return size;
    }

    public static long getAllMediaSize(Context context) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(CONTENT_URI, new String[]{"sum(size)"}, null, null, null);
        if (cursor == null) return 0;
        cursor.moveToFirst();
        long size = cursor.getLong(0);
        cursor.close();
        return size;
    }

    public static String getOtherSize(Context context){
        long size =0;
        try {
            size = DeviceControlUtil.getSDBlockSize(context) - getAllMediaSize(context);
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.e("getOtherSize", e.getMessage());
        }
        if(size < 0){
            size = 0;
        }
        return Formatter.formatFileSize(context, size);
    }

    /**
     * @return 返回查询cursor的总数
     */
    public static int getCursorSize(Context context, String where) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(CONTENT_URI, new String[]{"count(*)"}, where, null, null);
        if (cursor == null) return 0;
        int i = 0;
        try {
            cursor.moveToFirst();
            i = cursor.getInt(0);
        } catch (Exception e) {
            Log.e("MediaList", "error:" + e.getMessage());
        }

        cursor.close();
        return i;
    }

    /**
     * @return 获取媒体列表
     * @throws Exception
     */
    public static String getMediaList(Context context, String where, int max, int pageNub, int pageSize) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        JSONArray jsonArray = new JSONArray();
        JSONObject media = new JSONObject();
        Log.i("zx", "数据总数max---->" + max);
        int pageMax = max / pageSize;
        StringBuilder ORDER = new StringBuilder();
        if (pageNub <= pageMax) {
            ORDER.append("date desc LIMIT ")
                    .append(pageSize)
                    .append(" OFFSET ")
                    .append(pageSize * (pageNub - 1));
        } else {
            int endPageSize = max - (pageMax * pageSize);
            Log.i("zx", "endPageSize------>" + endPageSize);
            ORDER.append("date desc LIMIT ")
                    .append(endPageSize)
                    .append(" OFFSET ")
                    .append(pageMax * pageSize);
        }
        Log.i("zx", "ORDER--->" + ORDER);
        Cursor cursor = resolver.query(CONTENT_URI, null, where, null, ORDER.toString());
        if (cursor == null) return null;
        while (cursor.moveToNext()) {
            JSONObject jsonObject = new JSONObject();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String filename = cursor.getString(cursor.getColumnIndex("filename"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            String thumb_path = cursor.getString(cursor.getColumnIndex("thumb_path"));
            long size = cursor.getLong(cursor.getColumnIndex("size"));
            long date = cursor.getLong(cursor.getColumnIndex("date"));
            long duration = cursor.getLong(cursor.getColumnIndex("duration"));
            float start_lat = cursor.getLong(cursor.getColumnIndex("start_latitude"));
            float start_log = cursor.getLong(cursor.getColumnIndex("start_longitude"));
            float end_lat = cursor.getLong(cursor.getColumnIndex("end_latitude"));
            float end_log = cursor.getLong(cursor.getColumnIndex("end_longitude"));
            int width = cursor.getInt(cursor.getColumnIndex("width"));
            int height = cursor.getInt(cursor.getColumnIndex("height"));
            int media_type = cursor.getInt(cursor.getColumnIndex("media_type"));
            int event_type = cursor.getInt(cursor.getColumnIndex("event_type"));
            jsonObject.put("id", id);
            jsonObject.put("fileName", filename);
            jsonObject.put("mediaType", media_type);
            jsonObject.put("path", path);
            jsonObject.put("thumbPath", thumb_path);
            jsonObject.put("duration", duration);
            jsonObject.put("date", date);
            jsonObject.put("size", size);
            jsonObject.put("width", width);
            jsonObject.put("height", height);
            jsonObject.put("start_lat", start_lat);
            jsonObject.put("start_log", start_log);
            jsonObject.put("end_lat", end_lat);
            jsonObject.put("end_log", end_log);
            jsonObject.put("eventType", event_type);
            jsonArray.put(jsonObject);
        }
        cursor.close();
        if (jsonArray.length() == 0) return null;
        media.put("media", jsonArray);
        media.put("maxCount", max);
        return media.toString();
    }
}
