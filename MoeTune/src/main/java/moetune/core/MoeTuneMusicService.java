package moetune.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.os.*;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MoeTuneMusicService extends Service implements AudioManager.OnAudioFocusChangeListener{

	private static final Class<?>[] mSetForegroundSignature = new Class[] {
			boolean.class};
	private static final Class<?>[] mStartForegroundSignature = new Class[] {
			int.class, Notification.class};
	private static final Class<?>[] mStopForegroundSignature = new Class[] {
			boolean.class};

	private NotificationManager mNM;
	private Method mSetForeground;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mSetForegroundArgs = new Object[1];
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	private AudioManager audioManager;
	private RemoteControlClient remoteControlClient;

	private MoeTuneMusicListHandler moeTuneMusicListHandler;
	private MediaPlayer moeTuneMediaPlayer;
	private Timer musicTimer = new Timer();
	private Boolean isSeeking = false;
	private Boolean isPreparing = true;
	private Boolean isNextPrepared = false;
	private Boolean isPaused = true;
	private Boolean isComplete = false;
	private Boolean isAlbumGetError = false;
	private int musicDuration;
	private int currentNetworkState = MoeTuneConstants.NetworkState.NETWORK_UNKNOWN;
	private int userType = MoeTuneConstants.Config.USER_TYPE_GUEST;
	private Bitmap currentCover;
	private Bitmap nextCover;
	private BroadcastReceiver broadcastReceiver;
	private MoeTuneNotificationManager moeTuneNotificationManager;
	private OnMusicProgressUpdateListener onMusicProgressUpdateListener;
	private OnMusicStateChangedListener onMusicStateChangedListener;
	private OnMusicErrorListener onMusicErrorListener;
	private OnMusicListLoadedListener onMusicListLoadedListener;

	private TimerTask musicTimerTask;

	private Handler progressHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			//在此回调接口更新UI
			if(!isPreparing && moeTuneMediaPlayer!=null){
				double currentPosition = moeTuneMediaPlayer.getCurrentPosition();
				double duration = moeTuneMediaPlayer.getDuration();

				onMusicProgressUpdateListener.onProgressUpdate(
						(int)Math.round(currentPosition/duration*10000),
						(int)Math.floor(currentPosition/duration*musicDuration)
				);
			}

		}
	};

	public MoeTuneMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
	    //Log.v("Service Debug", "on bind");
        return new MessageBinder();
    }

	@Override
	public void onRebind(Intent intent) {
		//Log.v("Service Debug", "on rebind");
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		//Log.v("Service Debug", "on unbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//Log.v("Service Debug", "on create");

		// Set up music list.
		moeTuneMusicListHandler = new MoeTuneMusicListHandler(this);

		initReceiver();
		initNotificationManager();
	}

	@Override
	public void onDestroy() {
		//Log.v("Service Debug","On Destroy");
		if(moeTuneMediaPlayer!=null){
			if(isPlaying()){
				moeTuneMediaPlayer.stop();
			}
			moeTuneMediaPlayer.release();
			moeTuneMediaPlayer = null;
		}
		musicTimer.cancel();
		this.unregisterReceiver(broadcastReceiver);
		stopForegroundCompat(MoeTuneConstants.Config.NOTIFICATION_ID);
		audioManager.abandonAudioFocus(this);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.v("Service Debug","Entered onStartCommand");

		userType = intent.getExtras().getInt("user_type");

		//Set up timer
		if(musicTimerTask!=null){
			musicTimerTask.cancel();
		}
		musicTimerTask  = new TimerTask() {
			@Override
			public void run() {
				if(moeTuneMediaPlayer == null){
					return;
				}
				try {
					if(moeTuneMediaPlayer.isPlaying() && !isSeeking){
						progressHandler.sendEmptyMessage(0);
					}
				}catch (Exception e){
					//Log.v("moetune debug","ignore this error");
				}
			}
		};
		musicTimer.cancel();
		musicTimer = new Timer();
		musicTimer.schedule(musicTimerTask, 0, 100);

		moeTuneMusicListHandler.setOnMusicListLoadedListener(new OnMusicListLoadedListener() {
			@Override
			public void onMusicListLoaded(ArrayList<MoeTuneMusic> musicList) throws IOException {
				//Log.v("Music Debug", "正在加载音乐");
				prepareMusic();
				onMusicListLoadedListener.onMusicListLoaded(musicList);
			}
		});
		moeTuneMusicListHandler.setOnMusicListErrorListener(new OnMusicListErrorListener() {
			@Override
			public void onMusicListError(int errorCode) {
				onMusicErrorListener.onMusicError(MoeTuneConstants.Error.PLAYER_CANNOT_CONNECT_NETWORK);
			}
		});
		moeTuneMediaPlayer = new MediaPlayer();
		moeTuneMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				//Log.v("Music Debug", "准备完成");
				MoeTuneMusic currentSong = moeTuneMusicListHandler.getSong();

				onMusicStateChangedListener.onMusicPrepared(moeTuneMusicListHandler.getNowPlayingIndex());
				moeTuneNotificationManager.setNewNotification(
						currentSong,
						moeTuneMusicListHandler.getNowPlayingIndex());
				isPreparing = false;
				isComplete = false;

				if(Build.VERSION.SDK_INT >= 14){
					RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(false);
					metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, currentSong.getWikiTitle());
					metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentSong.getArtist());
					metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentSong.getSubTitle());
					metadataEditor.apply();
				}
			}
		});

		moeTuneMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer){
				//Log.v("Debug","播放结束");
				isComplete = true;
				onMusicStateChangedListener.onMusicStateChanged();
				changeNotificationState();
				moeTuneMusicListHandler.signListened();
				next();
			}
		});

		setPlayerErrorHandler();

		//在此首次读取歌曲列表
		if(moeTuneMusicListHandler.musicList.size() == 0){
			moeTuneMusicListHandler.loadMusicList(30, userType);
		}else{
			prepareMusic();
		}

		return START_REDELIVER_INTENT;
	}

	private void prepareMusic(){
		isPreparing = true;
		onMusicStateChangedListener.onMusicPreparing();
		MoeTuneMusic music = moeTuneMusicListHandler.getSong();
		onMusicStateChangedListener.onMusicInfoChanged(music.getStreamTime(),
				music.getWikiTitle(), music.getSubTitle(), music.getArtist());
		if(!isNextPrepared){
			new GetAlbum().execute(moeTuneMusicListHandler.getSong().getCoverUrl().getLarge());
		}else{
			onMusicStateChangedListener.onCoverPrepared(nextCover);
		}
		musicDuration = music.getStreamLength();
		readyForPlay(music.getUrl());
	}

	private void prepareNextMusic(){
		isNextPrepared = false;
		moeTuneMusicListHandler.setNowPlayingIndex(moeTuneMusicListHandler.getNowPlayingIndex() + 1);
		new GetNextAlbum().execute(moeTuneMusicListHandler.getSong().getCoverUrl().getLarge());
		moeTuneMusicListHandler.setNowPlayingIndex(moeTuneMusicListHandler.getNowPlayingIndex() - 1);
	}

	private void readyForPlay(String url){
		try {
			moeTuneMediaPlayer.reset();
			moeTuneMediaPlayer.setDataSource(url);
			moeTuneMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			moeTuneMediaPlayer.prepareAsync();
		} catch (IOException e) {
			onMusicErrorListener.onMusicError(MoeTuneConstants.Error.PLAYER_CANNOT_CONNECT_NETWORK);
			//Log.v("Error Debug","Can't prepare for music");
		}
	}

	public MoeTuneMusic getCurrentSong(){
		return moeTuneMusicListHandler.getSong();
	}

	public Bitmap getCurrentCover(){
		return currentCover;
	}

	public void setIsSeekingState(Boolean state){
		this.isSeeking = state;
	}

	public void setIsNextPrepared(Boolean value){
		isNextPrepared = value;
	}

	public void setOnMusicProgressUpdateListener(OnMusicProgressUpdateListener onMusicProgressUpdateListener){
		this.onMusicProgressUpdateListener = onMusicProgressUpdateListener;
	}

	public void setOnMusicStateChangedListener(OnMusicStateChangedListener onMusicStateChangedListener) {
		this.onMusicStateChangedListener = onMusicStateChangedListener;
	}

	public void setOnMusicErrorListener(OnMusicErrorListener onMusicErrorListener) {
		this.onMusicErrorListener = onMusicErrorListener;
	}

	public void setOnMusicListLoadedListener(OnMusicListLoadedListener onMusicListLoadedListener) {
		this.onMusicListLoadedListener = onMusicListLoadedListener;
	}

	public boolean isPlaying(){
		return moeTuneMediaPlayer.isPlaying();
	}

	public boolean isComplete(){
		return isComplete;
	}

	public void play(){
		moeTuneMediaPlayer.start();
		isPaused = false;
		if(isComplete){
			isComplete = false;
		}
		onMusicStateChangedListener.onMusicStateChanged();
		changeNotificationState();
		if(Build.VERSION.SDK_INT >= 14){
			remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		}
	}

	public void pause(){
		moeTuneMediaPlayer.pause();
		isPaused = true;
		onMusicStateChangedListener.onMusicStateChanged();
		changeNotificationState();
		if(Build.VERSION.SDK_INT >= 14){
			remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
		}
	}

	public void next(){
		if(isPlaying()){
			moeTuneMediaPlayer.stop();
			isComplete = true;
			onMusicStateChangedListener.onMusicStateChanged();
			changeNotificationState();
		}
		if(currentNetworkState != MoeTuneConstants.NetworkState.NETWORK_WIFI) {
			//Log.v("Error Debug","没联网所以不能用");
			onMusicErrorListener.onMusicError(MoeTuneConstants.Error.PLAYER_CANNOT_CONNECT_NETWORK);
			return;
		}
		if(!moeTuneMusicListHandler.isErrorOccurred()){
			moeTuneMusicListHandler.setNowPlayingIndex(moeTuneMusicListHandler.getNowPlayingIndex() + 1);
		}
		changeNotificationState();
		prepareMusic();
		if(Build.VERSION.SDK_INT >= 14){
			remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
		}
	}

	public void fromBegin(){
		moeTuneMediaPlayer.seekTo(0);
	}

	private void invokeMethod(Method method, Object[] args) {
		try {
			method.invoke(this, args);
		} catch (InvocationTargetException e) {
			// Should not happen.
//			Log.w("ApiDemos", "Unable to invoke method", e);
		} catch (IllegalAccessException e) {
			// Should not happen.
//			Log.w("ApiDemos", "Unable to invoke method", e);
		}
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	private void startForegroundCompat(int id, Notification notification) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = id;
			mStartForegroundArgs[1] = notification;
			invokeMethod(mStartForeground, mStartForegroundArgs);
			//Log.v("Notification Debug", "通知成功？");
			return;
		}

		// Fall back on the old API.
		mSetForegroundArgs[0] = Boolean.TRUE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
		mNM.notify(id, notification);
		//Log.v("Notification Debug", "通知成功？");
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	private void stopForegroundCompat(int id) {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			invokeMethod(mStopForeground, mStopForegroundArgs);
			return;
		}

		// Fall back on the old API.  Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		mNM.cancel(id);
		mSetForegroundArgs[0] = Boolean.FALSE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
	}

	private void setPlayerErrorHandler(){
		moeTuneMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

				switch (extra){
					case MediaPlayer.MEDIA_ERROR_IO:
						if(currentNetworkState == MoeTuneConstants.NetworkState.NETWORK_WIFI){
							networkStateHandler(util.Tool.checkNetworkState(MoeTuneMusicService.this));
						}
						return true;
					case MediaPlayer.MEDIA_ERROR_MALFORMED:
					case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
						//Log.v("Error Debug","Can't handle these error go to next song");
						next();
						return true;
					case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
						prepareMusic();
						return true;
				}

				switch (what){
					case MediaPlayer.MEDIA_ERROR_UNKNOWN:
						//Log.v("Error Debug","Can't handle these error go to next song");
						next();
						return true;
					case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
						if(currentNetworkState == MoeTuneConstants.NetworkState.NETWORK_WIFI){
							networkStateHandler(util.Tool.checkNetworkState(MoeTuneMusicService.this));
						}
						return true;
				}

				return false;
			}
		});
	}

	private void initReceiver(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("moetune_next_song");
		intentFilter.addAction("moetune_from_begin");
		intentFilter.addAction("moetune_change_play_state");
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);

		broadcastReceiver = new MediaButtonReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				if(intent.getAction().equals("moetune_next_song")){
					if(!isPreparing){
						next();
					}
					return;
				}
				if(intent.getAction().equals("moetune_from_begin")){
					if(!isPreparing){
						fromBegin();
					}
					return;
				}
				if(intent.getAction().equals("moetune_change_play_state")){
					if(!isPreparing){
						if(isPlaying()){
							pause();
						}else{
							play();
						}
						changeNotificationState();
					}
				}
				if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
					networkStateHandler(util.Tool.checkNetworkState(MoeTuneMusicService.this));
				}
				if(intent.getAction().equals(
						android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
					Log.v("Noisy Debug","Become Noisy");
				}
//				abortBroadcast();
			}
		};

		this.registerReceiver(broadcastReceiver,intentFilter);

		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener(){
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
					case TelephonyManager.CALL_STATE_IDLE: // 挂机状态
						if(isPreparing){
							break;
						}
						if(!isPlaying()){
							play();
						}
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:   //通话状态
					case TelephonyManager.CALL_STATE_RINGING:   //响铃状态
						if(isPreparing){
							break;
						}
						if(isPlaying()){
							pause();
						}
						break;
					default:
						break;
				}
			}
		},PhoneStateListener.LISTEN_CALL_STATE);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		ComponentName myEventReceiver = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
		audioManager.registerMediaButtonEventReceiver(myEventReceiver);

		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(myEventReceiver);
		PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

		if(Build.VERSION.SDK_INT >= 14){
			remoteControlClient = new RemoteControlClient(mediaPendingIntent);
			audioManager.registerRemoteControlClient(remoteControlClient);
			int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
					| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
					| RemoteControlClient.FLAG_KEY_MEDIA_PLAY
					| RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
					| RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
					| RemoteControlClient.FLAG_KEY_MEDIA_STOP;
			remoteControlClient.setTransportControlFlags(flags);
		}
	}

	private void initNotificationManager(){
		//Log.v("Notification Debug","初始化Notification Manager");
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		try {
			mStartForeground = getClass().getMethod("startForeground",
					mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground",
					mStopForegroundSignature);
//			return;
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			mStartForeground = mStopForeground = null;
			try {
				mSetForeground = getClass().getMethod("setForeground",
						mSetForegroundSignature);
			} catch (NoSuchMethodException ex) {
				throw new IllegalStateException(
						"OS doesn't have Service.startForeground OR Service.setForeground!");
			}
		}

		moeTuneNotificationManager = new MoeTuneNotificationManager(this);
		moeTuneNotificationManager.setOnNotificationChangedListener(new OnNotificationChangedListener() {
			@Override
			public void notificationChanged(Notification notification) {
				startForegroundCompat(MoeTuneConstants.Config.NOTIFICATION_ID, notification);
			}
		});
		moeTuneNotificationManager.setNewNotification();
	}

	public void changeNotificationState(){
		moeTuneNotificationManager.changeNotificationState(isPlaying());
	}

	private void networkStateHandler(int networkState){
		switch (networkState){
			case MoeTuneConstants.NetworkState.NETWORK_WIFI:
				if(currentNetworkState == MoeTuneConstants.NetworkState.NETWORK_WIFI){break;}
				currentNetworkState = MoeTuneConstants.NetworkState.NETWORK_WIFI;
				//Log.v("Network Debug","Get it. Network State Changed to wifi.");
				if(moeTuneMusicListHandler.musicList.size()!=0){
					if(isAlbumGetError){
						//Log.v("Network Debug","Getting Album...");
						new GetAlbum().execute(moeTuneMusicListHandler.getSong().getCoverUrl().getLarge());
					}
					if(moeTuneMusicListHandler.isErrorOccurred()){
						switch (moeTuneMusicListHandler.getErrorCode()){
							case MoeTuneConstants.Error.LIST_GET_ERROR:
								moeTuneMusicListHandler.loadMusicListCheck();
								return;
							case MoeTuneConstants.Error.LIST_SIGN_ERROR:
								moeTuneMusicListHandler.signListened();
								break;
						}
					}
					if(isComplete){
						next();
						return;
					}
					if(isPaused){
						play();
					}
				}else if(moeTuneMusicListHandler.isErrorOccurred()){
					//Log.v("Network Debug","Found error is occurred");
					switch (moeTuneMusicListHandler.getErrorCode()){
						case MoeTuneConstants.Error.LIST_SIGN_ERROR:
							moeTuneMusicListHandler.signListened();
							break;
						case MoeTuneConstants.Error.LIST_GET_ERROR:
							if(moeTuneMusicListHandler.musicList.size() == 0){
								moeTuneMusicListHandler.loadMusicList(30, userType);
							}
							break;
					}
				}
				break;
			case MoeTuneConstants.NetworkState.NETWORK_MOBILE:
				if(currentNetworkState == MoeTuneConstants.NetworkState.NETWORK_MOBILE){break;}
				currentNetworkState = MoeTuneConstants.NetworkState.NETWORK_MOBILE;
				//Log.v("Network Debug","Get it. Network State Changed to mobile.");
				break;
			case MoeTuneConstants.NetworkState.NETWORK_UNKNOWN:
				if(currentNetworkState == MoeTuneConstants.NetworkState.NETWORK_UNKNOWN){break;}
				currentNetworkState = MoeTuneConstants.NetworkState.NETWORK_UNKNOWN;
				//Log.v("Network Debug","Get it. Network State Changed to unknown.");
				break;
		}
	}

	@Override
	public void onAudioFocusChange(int i) {
		switch (i){
			case AudioManager.AUDIOFOCUS_GAIN:
				Log.v("Audio Focus","Get Focus");
				// resume playback
				break;

			case AudioManager.AUDIOFOCUS_LOSS:
				Log.v("Audio Focus","Lose Focus");
				// Lost focus for an unbounded amount of time: stop playback and release media player
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				Log.v("Audio Focus","Lose transient");
				// Lost focus for a short time, but we have to stop
				// playback. We don't release the media player because playback
				// is likely to resume
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				Log.v("Audio Focus","Lose transient can duck");
				// Lost focus for a short time, but it's ok to keep playing
				// at an attenuated level
				break;
		}
	}

	public class MessageBinder extends Binder{
		/**
		 * 获取当前Service的实例
		 * @return Service
		 */
		public MoeTuneMusicService getService(){
			return MoeTuneMusicService.this;
		}
	}

	private class GetAlbum extends AsyncTask<String,Integer,Bitmap>{

		@Override
		protected Bitmap doInBackground(String... strings) {
			Bitmap tmpBitmap = null;
			try {
				//Log.v("Network Debug", "Getting Current Album...");
				InputStream inputStream = new java.net.URL(strings[0]).openStream();
				tmpBitmap = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
			} catch (IOException e) {
				isAlbumGetError = true;
				//Log.v("Network Debug", "Get album failed.");
			}
			return tmpBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			onMusicStateChangedListener.onCoverPrepared(bitmap);
			currentCover = bitmap;
			moeTuneNotificationManager.setNewNotification(moeTuneMusicListHandler.getSong(),
					currentCover,moeTuneMusicListHandler.getNowPlayingIndex());
			if(Build.VERSION.SDK_INT >= 14){
				RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(false);
				metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap);
				metadataEditor.apply();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}

	private class GetNextAlbum extends AsyncTask<String,Integer,Bitmap>{

		@Override
		protected Bitmap doInBackground(String... strings) {
			Bitmap tmpBitmap = null;
			try {
				InputStream inputStream = new java.net.URL(strings[0]).openStream();
				tmpBitmap = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return tmpBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			onMusicStateChangedListener.onCoverPrepared(bitmap);
			nextCover = bitmap;
			isNextPrepared = true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}
}
