package com.broadsense.patron.utils;

import java.io.Closeable;
import java.io.IOException;
/**
 * 项目名称：${守护神}
 * 类描述：工具类
 * 创建人：${jcky}
 * 创建时间：2016/1/8 16:47
 * 修改人：${jcky}
 * 修改时间：2016/1/8 16:47
 * 修改备注：
 */
public class IOUtils {
	/** 关闭流 */
	public static boolean close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				LogUtils.e(e);
			}
		}
		return true;
	}
}
