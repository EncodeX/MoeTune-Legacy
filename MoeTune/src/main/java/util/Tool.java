package util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import moetune.core.MoeTuneConstants;

import java.util.List;

/**
 * Created by Encode_X on 14-9-18.
 */
public class Tool {
	public static String urlReplace(String url){
		return url.replaceAll("\\\\/","/");
	}
	public static String symbolReplace(String str){
		str = str.replaceAll("&quot;","\"");
		str = str.replaceAll("&amp;","&");
		str = str.replaceAll("&lt;","<");
		str = str.replaceAll("&gt;",">");
		str = str.replaceAll("&nbsp;"," ");
		str = str.replaceAll("&#039;","'");

		return str;
	}
	public static int checkNetworkState(Context context){
		ConnectivityManager connectivityManager =
				(ConnectivityManager)
						context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		SharedPreferences sharedPreferences = context.getSharedPreferences(MoeTuneConstants.Config.PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		if (networkInfo != null && networkInfo.isConnected()) {
			if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
				editor.putBoolean(MoeTuneConstants.Config.IS_WIFI_CONNECTED,true);
				editor.apply();
				return MoeTuneConstants.NetworkState.NETWORK_WIFI;
			} else {
				editor.putBoolean(MoeTuneConstants.Config.IS_WIFI_CONNECTED,false);
				editor.apply();
				return MoeTuneConstants.NetworkState.NETWORK_MOBILE;
			}
		} else {
			editor.putBoolean(MoeTuneConstants.Config.IS_WIFI_CONNECTED,false);
			editor.apply();
			return MoeTuneConstants.NetworkState.NETWORK_UNKNOWN;
		}
	}

	public static boolean isServiceRunning(Context context){
		boolean result = false;
		ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceInfoList = activityManager.getRunningServices(100);
		for(ActivityManager.RunningServiceInfo serviceInfo : serviceInfoList){
			if(serviceInfo.service.getClassName().equals("moetune.core.MoeTuneMusicService")){
				result = true;
				break;
			}
		}

		return result;
	}

	public static boolean isApplicationRunning(Context context){
		boolean result = false;
		ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
		for(ActivityManager.RunningTaskInfo taskInfo : taskInfoList){
			if(taskInfo.topActivity.getPackageName().equals("com.uexperience.moetune")
					|| taskInfo.baseActivity.getPackageName().equals("com.uexperience.moetune")){
				result = true;
				break;
			}
		}

		return result;
	}

	public static int dip2px(Context context, float dipValue){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(dipValue * scale + 0.5f);
	}
	public static int px2dip(Context context, float pxValue){
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(pxValue / scale + 0.5f);
	}

}
