package com.broadsense.patron.utils;

import android.content.Context;
import android.widget.Toast;

import com.broadsense.patron.R;
import com.broadsense.patron.view.base.BaseApplication;

/**
 * Toast tool class
 * Created by Administrator on 2016/1/26 0026.
 */
public class ToastUtils {
    private static Toast mToast;
    private static Context mContext=UIUtils.getContext();
    /**
     * 显示短时间的Toast
     *
     */
    public static void showShortToast(Context context, String text) {
        mToast = BaseApplication.getmToast();
        if(mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            BaseApplication.setmToast(mToast);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    /**
     * 显示长时间的Toast
     *
     */
    public static void showLongToast(Context context, String text) {
        mToast = BaseApplication.getmToast();
        if(mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            BaseApplication.setmToast(mToast);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }

    /**
     * 显示长时间的Toast
     */
    public static void showErrorToast(Context context) {
        mToast = BaseApplication.getmToast();
        String error="error";
        if(mToast == null) {
            mToast = Toast.makeText(context, error, Toast.LENGTH_SHORT);
            BaseApplication.setmToast(mToast);
        } else {
            mToast.setText(error);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }

    /**
     * 显示长时间的Toast
     */
    public static void showNetErrorToast() {
        mToast = BaseApplication.getmToast();
        String error= UIUtils.getResource().getString(R.string.net_error);
        if(mToast == null) {
            mToast = Toast.makeText(mContext, error, Toast.LENGTH_SHORT);
            BaseApplication.setmToast(mToast);
        } else {
            mToast.setText(error);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }
}
