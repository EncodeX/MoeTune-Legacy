package moetune.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import com.uexperience.moetune.R;
import moetune.activities.MainActivity;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 15/1/2
 * Project: MoeTune
 * Package: moetune.core
 */
public class MoeTuneNotificationManager {
	private RemoteViews currentRemoteBigViews;
	private RemoteViews currentRemoteViews;
	private int currentIndex = -1;
	private OnNotificationChangedListener onNotificationChangedListener;
	private Context context;

	public MoeTuneNotificationManager(Context context) {
		this.context = context;
	}

	public void setNewNotification(){
		RemoteViews remoteViews = new RemoteViews("com.uexperience.moetune", R.layout.notification_layout);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,new Intent(context, MainActivity.class), 0);

		builder.setContent(remoteViews);
		builder.setContentIntent(contentIntent);
		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setContentTitle("Foreground Service");
		builder.setContentText("Make this service run in the foreground.");
		Notification notification = builder.build();
		if(Build.VERSION.SDK_INT >= 16){
			RemoteViews remoteBigViews = new RemoteViews("com.uexperience.moetune",R.layout.notification_big_layout);
			notification.bigContentView = remoteBigViews;
			currentRemoteBigViews = remoteBigViews;
		}

//		startForegroundCompat(MoeTuneConstants.Config.NOTIFICATION_ID, notification);
		onNotificationChangedListener.notificationChanged(notification);
		currentRemoteViews = remoteViews;
	}

	public void setNewNotification(MoeTuneMusic music, int nowPlayingIndex){
		RemoteViews remoteViews = new RemoteViews("com.uexperience.moetune",R.layout.notification_layout);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,new Intent(context, MainActivity.class), 0);

		builder.setContent(remoteViews);
		builder.setContentIntent(contentIntent);
		builder.setSmallIcon(R.drawable.notification_icon);
		if(currentIndex != nowPlayingIndex){
			builder.setTicker(music.getSubTitle());
		}
		builder.setContentTitle("Foreground Service");
		builder.setContentText("Make this service run in the foreground.");

		remoteViews.setTextViewText(R.id.notification_title,music.getSubTitle());
		remoteViews.setTextViewText(R.id.notification_artist, music.getArtist());

		PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context,0,new Intent("moetune_next_song"),0);
		remoteViews.setOnClickPendingIntent(R.id.notification_next_button,pendingIntentNext);

		Notification notification = builder.build();
		if(Build.VERSION.SDK_INT >= 16){
			RemoteViews remoteBigViews = new RemoteViews("com.uexperience.moetune",R.layout.notification_big_layout);
			remoteBigViews.setTextViewText(R.id.notification_big_title,music.getSubTitle());
			remoteBigViews.setTextViewText(R.id.notification_big_artist,music.getArtist());

			PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(context,0,new Intent("moetune_from_begin"),0);
			PendingIntent pendingIntentChangePlayState = PendingIntent.getBroadcast(context,0,new Intent("moetune_change_play_state"),0);

			remoteBigViews.setOnClickPendingIntent(R.id.notification_big_next_button,pendingIntentNext);
			remoteBigViews.setOnClickPendingIntent(R.id.notification_big_prev_button,pendingIntentPrev);
			remoteBigViews.setOnClickPendingIntent(R.id.notification_big_play_button,pendingIntentChangePlayState);

			notification.bigContentView = remoteBigViews;
			currentRemoteBigViews = remoteBigViews;
		}

//		startForegroundCompat(MoeTuneConstants.Config.NOTIFICATION_ID, notification);
		onNotificationChangedListener.notificationChanged(notification);
		currentIndex = nowPlayingIndex;
		currentRemoteViews = remoteViews;
	}

	public void setNewNotification(MoeTuneMusic music, Bitmap cover, int nowPlayingIndex){
		RemoteViews remoteViews = new RemoteViews("com.uexperience.moetune",R.layout.notification_layout);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,new Intent(context, MainActivity.class), 0);

		builder.setContent(remoteViews);
		builder.setContentIntent(contentIntent);
		builder.setSmallIcon(R.drawable.notification_icon);
		if(currentIndex != nowPlayingIndex){
			builder.setTicker(music.getSubTitle());
		}
		builder.setContentTitle("Foreground Service");
		builder.setContentText("Make this service run in the foreground.");

		remoteViews.setTextViewText(R.id.notification_title,music.getSubTitle());
		remoteViews.setTextViewText(R.id.notification_artist,music.getArtist());
		remoteViews.setImageViewBitmap(R.id.notification_album, cover);

		PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context,0,new Intent("moetune_next_song"),0);
		remoteViews.setOnClickPendingIntent(R.id.notification_next_button, pendingIntentNext);

		Notification notification = builder.build();
		if(Build.VERSION.SDK_INT >= 16){
			RemoteViews remoteBigViews = new RemoteViews("com.uexperience.moetune",R.layout.notification_big_layout);
			remoteBigViews.setTextViewText(R.id.notification_big_title,music.getSubTitle());
			remoteBigViews.setTextViewText(R.id.notification_big_artist,music.getArtist());
			remoteBigViews.setImageViewBitmap(R.id.notification_big_album, cover);

			PendingIntent pendingIntentPrev =
					PendingIntent.getBroadcast(context,0,new Intent("moetune_from_begin"),0);
			PendingIntent pendingIntentChangePlayState =
					PendingIntent.getBroadcast(context,0,new Intent("moetune_change_play_state"),0);

			remoteBigViews.setOnClickPendingIntent(R.id.notification_big_next_button,pendingIntentNext);
			remoteBigViews.setOnClickPendingIntent(R.id.notification_big_prev_button,pendingIntentPrev);
			remoteBigViews.setOnClickPendingIntent(R.id.notification_big_play_button,pendingIntentChangePlayState);

			notification.bigContentView = remoteBigViews;
			currentRemoteBigViews = remoteBigViews;
		}

//		startForegroundCompat(MoeTuneConstants.Config.NOTIFICATION_ID, notification);
		onNotificationChangedListener.notificationChanged(notification);
		currentIndex = nowPlayingIndex;
		currentRemoteViews = remoteViews;
	}

	public void setNewNotification(RemoteViews remoteViews, RemoteViews remoteBigViews){

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,new Intent(context, MainActivity.class), 0);

		builder.setContent(remoteViews);
		builder.setContentIntent(contentIntent);
		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setContentTitle("Foreground Service");
		builder.setContentText("Make this service run in the foreground.");

		Notification notification = builder.build();
		if(Build.VERSION.SDK_INT >= 16){
			notification.bigContentView = remoteBigViews;
			currentRemoteBigViews = remoteBigViews;
		}

//		startForegroundCompat(MoeTuneConstants.Config.NOTIFICATION_ID, notification);
		onNotificationChangedListener.notificationChanged(notification);
		currentRemoteViews = remoteViews;
	}

	public void changeNotificationState(Boolean isPlaying){
		if(isPlaying){
			if(currentRemoteBigViews != null){
				currentRemoteBigViews.setImageViewResource(R.id.notification_big_play_button,R.drawable.icon_pause);
			}
		}else{
			if(currentRemoteBigViews != null){
				currentRemoteBigViews.setImageViewResource(R.id.notification_big_play_button,R.drawable.icon_play);
			}
		}
		setNewNotification(currentRemoteViews, currentRemoteBigViews);
	}

	public void setOnNotificationChangedListener(OnNotificationChangedListener onNotificationChangedListener) {
		this.onNotificationChangedListener = onNotificationChangedListener;
	}
}
