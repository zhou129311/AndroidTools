package com.deepwits.patron.vehiclemountedwifi.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/6/12 0012.
 */
public class SetPhoneSettings {
    private static final String TAG="SetPhoneSettings";
    static int network2GSelect = 1;
    static int network3GSelect = 0;

    static int NETWORK_MODE_WCDMA_PREF = 0; /* GSM/WCDMA (WCDMA preferred) */
    static int NETWORK_MODE_GSM_ONLY = 1; /* GSM only */
    static int NETWORK_MODE_WCDMA_ONLY = 2; /* WCDMA only */
    static int NETWORK_MODE_GSM_UMTS = 3; /*
                                           * GSM/WCDMA (auto mode, according to
                                           * PRL) AVAILABLE Application Settings
                                           * menu
                                           */
    static int NETWORK_MODE_CDMA = 4; /*
                                       * CDMA and EvDo (auto mode, according to
                                       * PRL) AVAILABLE Application Settings
                                       * menu
                                       */
    static int NETWORK_MODE_CDMA_NO_EVDO = 5; /* CDMA only */
    static int NETWORK_MODE_EVDO_NO_CDMA = 6; /* EvDo only */
    static int NETWORK_MODE_GLOBAL = 7; /*
                                         * GSM/WCDMA, CDMA, and EvDo (auto mode,
                                         * according to PRL) AVAILABLE
                                         * Application Settings menu
                                         */
    static int NETWORK_MODE_LTE_CDMA_EVDO = 8; // LTE, CDMA and EvDo
    static int NETWORK_MODE_LTE_GSM_WCDMA = 9; // LTE, GSM/WCDMA
    static int NETWORK_MODE_LTE_CMDA_EVDO_GSM_WCDMA = 10; // LTE, CDMA, EvDo,
    // GSM/WCDMA
    static int NETWORK_MODE_LTE_ONLY = 11; // LTE Only mode.

    private static final int MESSAGE_GET_PREFERRED_NETWORK_TYPE = 5;
    private static final int MESSAGE_SET_2G = 1;
    private static final int MESSAGE_SET_3G = 0;
    private static final int MESSAGE_SET_4G = 9;
    private static final int MESSAGE_SET_AFTER_GET_4G = 2;
    private static final int MESSAGE_SET_AFTER_GET_2G = 3;
    private static final int MESSAGE_SET_AFTER_GET_3G = 4;

    private static final int MESSAGE_RESTORE_DATA_OFF_MONITORING = 7;

    private static final int MESSAGE_CHANGE_LOCK_SIM = 100;
    private static final int MESSAGE_CHANGE_PASSWORD_PIN = 101;
    private static final int MESSAGE_SUPPLY_PIN = 103;
    private static final int MESSAGE_SUPPLY_PUK = 104;

    SetHandler setHandler = new SetHandler();
    TelephonyManager telephonyManager;

    Object mPhone;
    Object mIccCard;

    Method setPreferredNetworkType;
    Method getPreferredNetworkType;

    Method setIccLockEnabled;
    Method changeIccLockPassword;
    Method getIccLockEnabled;
    Method supplyPin;
    Method supplyPuk;

    int currentNetwork = -1;
    int customNetwork = 9;

    setG settingG = null;
    String reason2g = null;
    String reason3g = null;
    String reason4g = null;
    boolean mTurnDataOff = false;
    Boolean mCurrentDataSetting = null;

    Context context;

    enum setG {
        set2g, set3g, set4g
    }

    public SetPhoneSettings(Context context){
        this.context = context;
        getDefaultNetwork();
        mPhone = loadPhoneObject();
        loadPinMethod();
        try {
            setPreferredNetworkType = mPhone.getClass().getMethod("setPreferredNetworkType", new Class[] { int.class, Message.class });
            getPreferredNetworkType = mPhone.getClass().getMethod("getPreferredNetworkType", new Class[] { Message.class });
            getNetwork();
        }
        catch (Exception e) {
            Log.e(TAG, "Error!", e);
        }

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    static Object loadPhoneObject() {
        try {
            Class<?> forName = Class.forName("com.android.internal.telephony.PhoneFactory");
            Method getDefaultPhone = forName.getMethod("getDefaultPhone", new Class[] {});
            return getDefaultPhone.invoke(null, new Object[] {});
        }
        catch (Exception e) {
            Log.e(TAG, "Error!", e);
        }
        return null;
    }

    Object loadIccCardObject(){
        try{
            Method getIccCard = mPhone.getClass().getMethod("getIccCard" ,new Class[] {});
            return getIccCard.invoke(mPhone,new Object[] {});
        }catch (Exception e){
            Log.e(TAG, "Error!", e);
        }
        return null;
    }

    public void loadPinMethod(){
        try {
            mIccCard = loadIccCardObject();
            setIccLockEnabled = mIccCard.getClass().getMethod("setIccLockEnabled",new Class[] { boolean.class, String.class, Message.class });
            changeIccLockPassword = mIccCard.getClass().getMethod("changeIccLockPassword",new Class[] { String.class, String.class, Message.class });
            getIccLockEnabled = mIccCard.getClass().getMethod("getIccLockEnabled",new Class[]{});
            supplyPin = mIccCard.getClass().getMethod("supplyPin",new Class[]{ String.class, Message.class});
            supplyPuk = mIccCard.getClass().getMethod("supplyPuk",new Class[]{String.class ,String.class, Message.class});
        }catch (Exception e){
            Log.e(TAG, "Error!", e);
        }
    }

    public void setIccLockEnabled(boolean isLock,String password){
        try {
            setIccLockEnabled.invoke(mIccCard,new Object[] {isLock, password,setHandler.obtainMessage(MESSAGE_CHANGE_LOCK_SIM) });
        }catch (Exception e){
            Log.e(TAG, "Error!", e);
        }
    }

    public void supplyPin(String pin){
        try {
            supplyPin.invoke(mIccCard,new Object[] {pin, setHandler.obtainMessage(MESSAGE_SUPPLY_PIN) });
        }catch (Exception e){
            Log.e(TAG, "Error!", e);
        }
    }

    public void supplyPuk(String puk,String newPin){
        try {
            supplyPuk.invoke(mIccCard,new Object[] {puk, newPin,setHandler.obtainMessage(MESSAGE_SUPPLY_PUK) });
        }catch (Exception e){
            Log.e(TAG, "Error!", e);
        }
    }


    public void changeIccLockPassword(String oldPassWord,String newPassword){
        try {
            changeIccLockPassword.invoke(mIccCard,new Object[] {oldPassWord, newPassword,setHandler.obtainMessage(MESSAGE_CHANGE_PASSWORD_PIN) });
        }catch (Exception e){
            Log.e(TAG, "Error!", e);
        }
    }

    /**
     * @return 返回当前SIM锁定状态
     */
    public boolean getIccLockEnabled(){
        try {
            return (Boolean)getIccLockEnabled.invoke(mIccCard,new Object[] {});
        }catch (Exception e){
            Log.e(TAG, "Error!", e);
        }
        return false;
    }

    static Integer getDefaultNetwork() {
        try {
            Object phone = loadPhoneObject();
            Field field = phone.getClass().getField("PREFERRED_NT_MODE");
            field.setAccessible(true);
            return field.getInt(phone);
        } catch (Exception e) {
            Log.e(TAG, "Error!", e);
        }
        return null;
    }




    void getNetwork() {
        try {
            getPreferredNetworkType.invoke(mPhone, new Object[] { setHandler.obtainMessage(MESSAGE_GET_PREFERRED_NETWORK_TYPE) });
        }
        catch (Exception e) {
            Log.e(TAG, "Error!", e);
        }
    }

    public void set2g(String reason, boolean dataOff) {
        if (currentNetwork != network2GSelect && settingG != setG.set2g) {
            mTurnDataOff = dataOff;
            reason2g = reason;
            settingG = setG.set2g;
            Looper.myQueue().addIdleHandler(setHandler);
        }
    }

    public void set3g(String reason, boolean dataOff) {
        if (currentNetwork != network3GSelect && settingG != setG.set3g) {
            mTurnDataOff = dataOff;
            reason3g = reason;
            settingG = setG.set3g;
            Looper.myQueue().addIdleHandler(setHandler);
        }
    }

    public void set2gNow(String reason, boolean dataOff) {
        currentNetwork = -1;
        // reason2g = reason;
        set2g(reason, dataOff);
    }

    public void set3gNow(String reason, boolean dataOff) {
        currentNetwork = -1;
        set3g(reason, dataOff);
    }

    public void set2gNow() {
        if (currentNetwork != 1) {
            Log.i(TAG, "set2g because " + reason2g);
            if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                try {
                    getPreferredNetworkType.invoke(mPhone, new Object[] { setHandler.obtainMessage(MESSAGE_SET_AFTER_GET_2G) });
                }
                catch (Exception e) {
                    Log.e(TAG, "Error!", e);
                }
            }
            else {
                Log.i(TAG, "2g not set, phone in use");
            }
        }
    }


    public void set3gNow() {
        // Log.i(Toggle2G.TOGGLE2G, "unlock is2g=" + is2g +
        // ", telephonyManager.getCallState()=" +
        // telephonyManager.getCallState());
        if (currentNetwork != 0) {
            Log.i(TAG, "set3g because " + reason3g);
            if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                try {
                    getPreferredNetworkType.invoke(mPhone, new Object[] { setHandler.obtainMessage(MESSAGE_SET_AFTER_GET_3G) });
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Error!", e);
                }
            }
            else {
                Log.i(TAG, "3g not set, phone in use");
            }
        }
    }

    public void set4GNow() {
        // Log.i(Toggle2G.TOGGLE2G, "unlock is2g=" + is2g +
        // ", telephonyManager.getCallState()=" +
        // telephonyManager.getCallState());
        if (currentNetwork != customNetwork) {

            if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                try {
                    Log.i(TAG, "set 4g because " + reason4g);
                    getPreferredNetworkType.invoke(mPhone, new Object[] { setHandler.obtainMessage(MESSAGE_SET_AFTER_GET_4G) });
                }
                catch (Exception e) {
                    Log.e(TAG, "Error!", e);
                }
            }
            else
            {
                Log.i(TAG, "custom not set, phone in use");
            }
        }
    }


    private class SetHandler extends Handler implements MessageQueue.IdleHandler {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_GET_PREFERRED_NETWORK_TYPE:
                    handleGetPreferredNetworkTypeResponse(msg);
                    break;
                case MESSAGE_SET_AFTER_GET_2G:
                    handleGetPreferredNetworkTypeResponse(msg);
                    if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                        try {
                            int delay = 500;
                            if ( mCurrentDataSetting == null && mTurnDataOff ) {
                                mCurrentDataSetting = getMobileData(context);
                                if( mCurrentDataSetting ) {
                                    delay=5000;
                                    setMobileDataEnabled( context, false );
                                    long timeout = System.currentTimeMillis() + 5000;
                                    while ( getMobileData(context) && System.currentTimeMillis() < timeout) {
                                        Thread.sleep(100);
                                    }
                                    Log.i(TAG, "Data Setting is now " + getMobileData(context) );
                                }
                            }
                            int timeout = (int) ((SystemClock.uptimeMillis() + delay ) / 1000);
                            Log.e(TAG, "start timeout = " + timeout );

                            setPreferredNetworkType.invoke(mPhone, new Object[] { network2GSelect, setHandler.obtainMessage(MESSAGE_SET_2G, network2GSelect, timeout) });
                            this.sendEmptyMessageDelayed(MESSAGE_RESTORE_DATA_OFF_MONITORING, delay);
                            break;
                        }
                        catch (Exception e)
                        {
                            Log.e(TAG, "Error!", e);
                        }
                    } else {
                        Log.i(TAG, "2g not set, phone in use");
                    }
                    if (settingG == setG.set2g) {
                        settingG = null;
                    }
                    break;
                case MESSAGE_SET_AFTER_GET_3G:
                    handleGetPreferredNetworkTypeResponse(msg);
                    if (currentNetwork != network3GSelect) {
                        Log.i(TAG, "switching from " + currentNetwork + " to " + network3GSelect);
                        if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                            try {
                                // Log.i(Toggle2G.TOGGLE2G,
                                // "setPreferredNetworkType=3g");
                                // setPreferredNetworkType.invoke(mPhone, new
                                // Object[] { 0,
                                // setHandler.obtainMessage(MESSAGE_SET_3G) });
                                int delay = 500;
                                if ( mCurrentDataSetting == null && mTurnDataOff ) {
                                    mCurrentDataSetting = getMobileData(context);
                                    if( mCurrentDataSetting ) {
                                        delay=5000;
                                        setMobileDataEnabled( context, false );
                                        long timeout = System.currentTimeMillis() + 5000;
                                        while ( getMobileData(context) && System.currentTimeMillis() < timeout) {
                                            Thread.sleep(100);
                                        }
                                        Log.i(TAG, "Data Setting is now " + getMobileData(context) );
                                    }
                                }
                                int timeout = (int) ((SystemClock.uptimeMillis() + delay ) / 1000);
                                Log.e(TAG, "start timeout = " + timeout );

                                setPreferredNetworkType.invoke(mPhone, new Object[] { network3GSelect, setHandler.obtainMessage(MESSAGE_SET_3G, network3GSelect, timeout) });
                                this.sendEmptyMessageDelayed(MESSAGE_RESTORE_DATA_OFF_MONITORING, delay);
                                break;
                            } catch (Exception e) {
                                Log.e(TAG, "Error!", e);
                            }
                        } else {
                            Log.i(TAG, "3g not set, phone in use");
                        }

                    }
                    if (settingG == setG.set3g) {
                        settingG = null;
                    }
                    break;
                case MESSAGE_SET_AFTER_GET_4G:
                    handleGetPreferredNetworkTypeResponse(msg);
                    Log.i(TAG, "switching from " + currentNetwork + " to " + customNetwork);
                    if (currentNetwork != customNetwork) {
                        Log.i(TAG, "switching from " + currentNetwork + " to " + customNetwork);
                        if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                            try {
                                // Log.i(Toggle2G.TOGGLE2G,
                                // "setPreferredNetworkType=CUSTOM");
                                int delay = 500;
                                if ( mCurrentDataSetting == null && mTurnDataOff ) {
                                    mCurrentDataSetting = getMobileData(context);
                                    if( mCurrentDataSetting ) {
                                        delay=5000;
                                        setMobileDataEnabled( context, false );
                                        long timeout = System.currentTimeMillis() + 5000;
                                        while ( getMobileData(context) && System.currentTimeMillis() < timeout) {
                                            Thread.sleep(100);
                                        }
                                        Log.i(TAG, "Data Setting is now " + getMobileData(context) );
                                    }
                                }
                                int timeout = (int) ((SystemClock.uptimeMillis() + delay ) / 1000);
                                Log.e(TAG, "start timeout = " + timeout );

                                setPreferredNetworkType.invoke(mPhone, new Object[] { customNetwork, setHandler.obtainMessage(MESSAGE_SET_4G, customNetwork, timeout) });
                                this.sendEmptyMessageDelayed(MESSAGE_RESTORE_DATA_OFF_MONITORING, delay);
                                break;
                            } catch (Exception e) {
                                Log.e(TAG, "Error!", e);
                            }
                        } else {
                            Log.i(TAG, "custom not set, phone in use");
                        }
                    }
                    if (settingG == setG.set4g) {
                        settingG = null;
                    }
                    break;
                case MESSAGE_SET_2G:
                    if ( handleSetPreferredNetworkTypeResponse(msg, true, setG.set2g)) {
                        if (settingG == setG.set2g) {
                            settingG = null;
                        }
                    }
                    break;
                case MESSAGE_SET_3G:
                    if (handleSetPreferredNetworkTypeResponse(msg, false, setG.set3g)){
                        if (settingG == setG.set3g) {
                            settingG = null;
                        }
                    }
                    break;
                case MESSAGE_SET_4G:
                    if (handleSetPreferredNetworkTypeResponse(msg, false, setG.set4g)) {
                        if (settingG == setG.set4g) {
                            settingG = null;
                        }
                    }
                    break;
                case MESSAGE_RESTORE_DATA_OFF_MONITORING:
                    checkToRestoreData(5000);
                    break;

                case MESSAGE_CHANGE_LOCK_SIM:
                    iccLockChanged(ar.exception == null);
                    break;
                case MESSAGE_CHANGE_PASSWORD_PIN:
                    iccPinChanged(ar.exception == null);
                    break;
                case MESSAGE_SUPPLY_PIN:
                    supplyPin(ar.exception == null);
                    break;
                case MESSAGE_SUPPLY_PUK:
                    supplyPuk(ar.exception == null);
                    break;
            }
        }


        private void handleGetPreferredNetworkTypeResponse(Message msg) {
            try {
                Field declaredField = msg.obj.getClass().getDeclaredField("exception");
                Object exception = declaredField.get(msg.obj);
                if (exception != null) {
                    Log.e(TAG, "Error Setting: " + declaredField.get(msg.obj));
                }
                else {
                    declaredField = msg.obj.getClass().getDeclaredField("result");
                    Object result = declaredField.get(msg.obj);
                    int type = ((int[]) result)[0];
                    currentNetwork = type;
                    Log.i(TAG, "2g=" + is2g() + " (" + type + ")");

                    //Toggle2GService.showNotification(context, is2g());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error!", e);
            }
        }

        private boolean handleSetPreferredNetworkTypeResponse(Message msg, boolean set2g, setG set) {
            try {
                Field declaredField = msg.obj.getClass().getDeclaredField("exception");
                Object exception = declaredField.get(msg.obj);

                if (exception != null) {
                    Log.e(TAG, "Error Setting: " + exception);

                    // try again!
                    long timeout = msg.arg2 * 1000;

                    Thread.sleep(500);
                    if ( ( settingG == null || set == settingG ) && SystemClock.uptimeMillis() < timeout) {
                        Log.i(TAG, "retry timeout left = " + (timeout - SystemClock.uptimeMillis()));
                        setPreferredNetworkType.invoke(mPhone, new Object[] { msg.arg1, setHandler.obtainMessage(msg.what, msg.arg1, msg.arg2) });
                    }
                    else {
                        Log.i(TAG, "retry timeout over, giving up");
                    }
                } else {
                    getNetwork();
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error!", e);
            }
            return false;
        }


        @Override
        public boolean queueIdle() {
            Log.e(TAG, "Idle Queue");
            if (settingG == setG.set2g) {
                set2gNow();
            } else if (settingG == setG.set3g) {
                set3gNow();
            } else if (settingG == setG.set4g) {
                set4GNow();
            }
            return false;
        }
    }

    void iccPinChanged(boolean success){
        if(success){
            Log.i(TAG,"PIN码 设置成功");
        }else{
            Log.i(TAG,"PIN码 设置失败");
        }
    }

    void iccLockChanged(boolean success){
        if(success){
            Log.i(TAG,"SIM锁定解锁 设置成功");
        }else{
            Log.i(TAG,"SIM锁定解锁 设置失败");
        }
    }

    void supplyPin(boolean success){
        if(success){
            Log.i(TAG,"pin 校验成功");
        }else{
            Log.i(TAG,"pin 校验失败");
        }
    }

    void supplyPuk(boolean success){
        if(success){
            Log.i(TAG,"Puk 校验成功");
        }else{
            Log.i(TAG,"Puk 校验失败");
        }
    }

    void checkToRestoreData(int tryFor) {
        Boolean lastSetting = mCurrentDataSetting;
        if( lastSetting != null && lastSetting ) {
            Log.i(TAG, "Trying to enable Data Setting" );

            setMobileDataEnabled( context, true );
            long timeout = System.currentTimeMillis() + tryFor;
            while ( !getMobileData(context) && System.currentTimeMillis() < timeout) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG,"error " + e.toString());
                }
                setMobileDataEnabled( context, true );
            }
            Log.i(TAG, "Data Setting is now " + getMobileData(context) );
        }
        mCurrentDataSetting = null;
    }

    public boolean is2g() {
        if (currentNetwork == network2GSelect) {
            return true;
        } else if (currentNetwork == network3GSelect) {
            return false;
        } else if (currentNetwork == NETWORK_MODE_GSM_ONLY) {
            // 2G ONLY
            return true;
        }
        return false;
    }

    public void setNetworkNow(String reason, int net) {
        if (settingG != setG.set4g || customNetwork != net) {
            reason4g = reason;
            settingG = setG.set4g;
            customNetwork = net;
            Looper.myQueue().addIdleHandler(setHandler);
        }
    }

    void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class<?> conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (Exception e) {
            Log.e(TAG, "setMobileDataEnabled Error!", e);
        }
    }

    boolean getMobileData(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Method m = null;
        try {
            m = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean) m.invoke(connectivityManager);
        } catch (Exception e) {
            Log.e(TAG, "getMobileDataEnabled Error!", e);
            return true;
        }

    }

}
