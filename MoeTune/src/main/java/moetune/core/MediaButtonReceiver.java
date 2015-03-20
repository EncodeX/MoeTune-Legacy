package moetune.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 15/1/4
 * Project: MoeTune
 * Package: moetune.core
 */
public class MediaButtonReceiver extends BroadcastReceiver {
	private static String TAG = "Debug";
	@Override
	public void onReceive(Context context, Intent intent) {
		// 获得Action
		String intentAction = intent.getAction();
		Log.v("Broadcast",intentAction);
		// 获得KeyEvent对象
		KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

		if(keyEvent != null){
			switch (keyEvent.getAction()){
				case KeyEvent.ACTION_DOWN:
					Log.v("Debug","Received: Action Down KeyCode: "+keyEvent.getKeyCode());
					break;
				case KeyEvent.ACTION_UP:
					Log.v("Debug","Received: Action Up KeyCode: "+keyEvent.getKeyCode());
					break;
			}
		}

		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			int keyCode = keyEvent.getKeyCode();
			int keyAction = keyEvent.getAction();

			StringBuilder sb = new StringBuilder();

			Intent moetuneAction = new Intent();

			if(keyAction == KeyEvent.ACTION_DOWN){
				switch (keyCode){
					case KeyEvent.KEYCODE_MEDIA_NEXT:
						sb.append("KEYCODE_MEDIA_NEXT");
						moetuneAction.setAction("moetune_next_song");
						context.sendBroadcast(moetuneAction);
						break;
					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					case KeyEvent.KEYCODE_HEADSETHOOK:
						sb.append("KEYCODE_HEADSETHOOK");
						moetuneAction.setAction("moetune_change_play_state");
						context.sendBroadcast(moetuneAction);
						break;
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
						sb.append("KEYCODE_MEDIA_PREVIOUS");
						moetuneAction.setAction("moetune_from_begin");
						context.sendBroadcast(moetuneAction);
						break;
					case KeyEvent.KEYCODE_MEDIA_STOP:
						sb.append("KEYCODE_MEDIA_STOP");
						break;
				}
				Log.v(TAG, sb.toString());
			}
		}
	}
}
