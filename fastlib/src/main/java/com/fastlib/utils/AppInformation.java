package com.fastlib.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.util.Random;

/**
 * 应用信息
 */
public class AppInformation
{

	private AppInformation()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("Class AppUtils cannot be instantiated");
	}

	/**
	 * [获取应用程序版本名称信息]
	 * 
	 * @param context
	 * @return 当前应用的版本名称
	 */
	public static String getVersionName(Context context)
	{
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(),PackageManager.GET_ACTIVITIES);
			return packageInfo.versionName;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * [获取应用程序版本号]
	 * 
	 * @param context
	 * @return 当前应用的版本
	 */
	public static int getVersionCode(Context context)
	{
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(),PackageManager.GET_ACTIVITIES);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
}