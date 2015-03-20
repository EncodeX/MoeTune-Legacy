package moetune.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.uexperience.moetune.R;
import moetune.core.MoeTuneConstants;
import moetune.core.MoeTuneUser;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import util.MoeTuneDBManager;
import util.Tool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;


public class BootActivity extends Activity {

	private static final String SHARED_PREFERENCES_NAME = "my_pref";
	private static final String KEY_AUTH_ACTIVITY = "auth_activity";

	private static final int SWITCH_TO_MAIN_GUEST = 1000;
	private static final int SWITCH_TO_MAIN_MEMBER = 1001;
	private static final int SWITCH_TO_AUTH = 1002;

	private OAuthConsumer consumer = new CommonsHttpOAuthConsumer(util.OAuthKey.OAUTH_CONSUMER_KEY,util.OAuthKey.OAUTH_CONSUMER_SECRET);
	private MoeTuneUser moeTuneUser;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);

	    /*此处判断是否为初次进入 以后可能用到*/
	    //boolean mFirst = isFirstEnter(BootActivity.this,BootActivity.this.getClass().getName());

		new OAuthAccessTester().execute();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.boot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
	    return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

	private boolean isFirstEnter(Context context,String className){
		if(context==null||className==null||"".equalsIgnoreCase(className)) return false;
		String mResultStr = context.getSharedPreferences(SHARED_PREFERENCES_NAME,Context.MODE_WORLD_READABLE).getString(KEY_AUTH_ACTIVITY,"");
		return !mResultStr.equalsIgnoreCase("false");
	}

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case SWITCH_TO_MAIN_GUEST:
					Intent intent = new Intent();
					intent.setClass(BootActivity.this, MainActivity.class);
					intent.putExtra("user_type",MoeTuneConstants.Config.USER_TYPE_GUEST);
					BootActivity.this.startActivity(intent);
					BootActivity.this.overridePendingTransition(R.anim.zoom_in_in,R.anim.zoom_in_out);
					BootActivity.this.finish();
					break;
				case SWITCH_TO_MAIN_MEMBER:
					intent = new Intent();
					intent.setClass(BootActivity.this, MainActivity.class);
					intent.putExtra("user_type",MoeTuneConstants.Config.USER_TYPE_MEMBER);
					BootActivity.this.startActivity(intent);
					BootActivity.this.overridePendingTransition(R.anim.zoom_in_in,R.anim.zoom_in_out);
					BootActivity.this.finish();
					break;
			}
			super.handleMessage(msg);
		}
	};
	private class OAuthAccessTester extends AsyncTask<String,Integer,Integer>{
		@Override
		protected Integer doInBackground(String... strings) {
			if(Tool.isServiceRunning(BootActivity.this)){
				return 0;
			}

			if(Tool.checkNetworkState(BootActivity.this) != MoeTuneConstants.NetworkState.NETWORK_WIFI){
				return 2;
			}

			MoeTuneDBManager.getInstance(BootActivity.this).openDatabase();
			moeTuneUser = MoeTuneDBManager.getInstance(BootActivity.this).query();
			SharedPreferences sharedPreferences = getSharedPreferences(MoeTuneConstants.Config.PREFERENCE_NAME,0);
			if(moeTuneUser != null){

				if(sharedPreferences.getBoolean(MoeTuneConstants.Config.IS_MEMBER_REGISTERED,false)){
					//Log.v("Config Debug","检测到用户已绑定，直接进入Main Activity。");
					return 0;
				}

				consumer.setTokenWithSecret(moeTuneUser.accessToken,moeTuneUser.accessTokenSecret);
				try {
					HttpGet request = new HttpGet("http://moe.fm/listen/playlist?api=json&perpage=1");
					HttpClient client = new DefaultHttpClient();
					consumer.sign(request);
					//Log.v("Access Debug", "Sending request...");
					HttpResponse response = client.execute(request);
					if(response.getStatusLine().getStatusCode()==401){
						return 1;
					}else{
						return 0;
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					e.printStackTrace();
				} catch (OAuthMessageSignerException e) {
					e.printStackTrace();
				}
			}
			return 2;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch(result){
				case 0:
					mHandler.sendEmptyMessageDelayed(SWITCH_TO_MAIN_MEMBER,2500);
					break;
				case 1:
					Toast.makeText(BootActivity.this, "自动登录失败 进入游客模式", Toast.LENGTH_LONG).show();
					MoeTuneDBManager.getInstance(BootActivity.this).delete(moeTuneUser.accessToken);
					mHandler.sendEmptyMessageDelayed(SWITCH_TO_MAIN_GUEST,2500);
					break;
				case 2:
					//Log.v("Config Debug","用户验证遇到错误，已登出。");
					mHandler.sendEmptyMessageDelayed(SWITCH_TO_MAIN_GUEST,2500);
					break;
			}
			MoeTuneDBManager.getInstance(BootActivity.this).closeDatabase();
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}
}
