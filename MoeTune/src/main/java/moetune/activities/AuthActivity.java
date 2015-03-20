package moetune.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.uexperience.moetune.R;
import moetune.core.MoeTuneConstants;
import moetune.core.MoeTuneUser;
import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.http.util.EncodingUtils;
import util.MoeTuneDBManager;


public class AuthActivity extends Activity {

	/**
	 * Todo: 以后想让loginButton在按下时有缩小的动画效果
	 **/

	private String verifier;

	private CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(util.OAuthKey.OAUTH_CONSUMER_KEY,util.OAuthKey.OAUTH_CONSUMER_SECRET);
	private CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(util.OAuthKey.REQUEST_TOKEN_URL,util.OAuthKey.ACCESS_TOKEN_URL,util.OAuthKey.AUTHORIZE_URL);

	private int counter;
	private static final int SWITCH_TO_MAIN = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

	    final EditText userPasswordInput = (EditText) findViewById(R.id.user_password_input);
	    final EditText userNameInput = (EditText) findViewById(R.id.user_name_input);

	    Button loginButton = (Button) findViewById(R.id.login_button);
	    loginButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    if(userNameInput.getText().length()==0||userPasswordInput.getText().length()==0){
				    Toast.makeText(AuthActivity.this,"用户名/密码没有输完哦",Toast.LENGTH_LONG).show();
			    }else{
				    new GetOAuthRequest().execute();
			    }
		    }
	    });

	    userPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
			    if(i == EditorInfo.IME_ACTION_SEND){
				    if(userNameInput.getText().length()==0||userPasswordInput.getText().length()==0){
					    Toast.makeText(AuthActivity.this,"用户名/密码没有输完哦",Toast.LENGTH_LONG).show();
				    }else{
					    new GetOAuthRequest().execute();
				    }
			    }
			    return false;
		    }
	    });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.auth, menu);
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode==KeyEvent.KEYCODE_BACK){
			AuthActivity.this.setResult(MoeTuneConstants.Actions.LOGIN_FAILED);
			AuthActivity.this.finish();
			AuthActivity.this.overridePendingTransition(R.anim.alpha_gradient_in,R.anim.alpha_gradient_out);
		}

		return super.onKeyDown(keyCode, event);
	}

	private class GetOAuthRequest extends AsyncTask<String,Integer,Integer>{

		@Override
		protected Integer doInBackground(String... strings) {
			//Log.v("OAuth", "===== Now start OAuth test. =====");

			//Log.v("OAuth","Fetching request token...");

			try {
				String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

				//Log.v("OAuth", "Request token: " + consumer.getToken());
				//Log.v("OAuth","Request secret: "+consumer.getTokenSecret());
				//Log.v("OAuth", authUrl);

			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
				return 1;
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
				return 2;
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
				return 3;
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
				return 4;
			}
			return 0;
		}

		@Override
		protected void onPreExecute() {
			Toast.makeText(AuthActivity.this,"开始获取验证网址...",Toast.LENGTH_LONG).show();
			counter=0;
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result){
				case 0:
					Toast.makeText(AuthActivity.this,"获取成功！打开网页...",Toast.LENGTH_LONG).show();
					WebView authWebView = (WebView)findViewById(R.id.auth_web_view);
					authWebView.setWebViewClient(new AuthWebViewClient());
					authWebView.getSettings().setUseWideViewPort(true);
					authWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
					CookieSyncManager.createInstance(AuthActivity.this);
					CookieSyncManager.getInstance().startSync();
					CookieManager.getInstance().removeSessionCookie();
					CookieManager.getInstance().removeAllCookie();

					EditText userNameInput = (EditText) findViewById(R.id.user_name_input);
					EditText userPasswordInput = (EditText) findViewById(R.id.user_password_input);

					String post = "oauth_token=" +
							consumer.getToken() +
							"&oauth_callback=oob&submit=1&login[account]=" +
							userNameInput.getText() +            //用户名
							"&login[password]=" +
							userPasswordInput.getText() +                //用户密码
							"&login[expire]=1&allow=%E7%99%BB%E5%BD%95%E5%B9%B6%E6%8E%88%E6%9D%83";

					authWebView.postUrl("http://api.moefou.org/oauth/authorize", EncodingUtils.getBytes(post,"BASE64"));
					break;
				case 1:
					Toast.makeText(AuthActivity.this,"连接不上网络呢",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Communication Exception");
					break;
				case 2:
					Toast.makeText(AuthActivity.this,"服务器不响应呢",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Expectation Failed Exception");
					break;
				case 3:
					Toast.makeText(AuthActivity.this,"验证失败",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Not Authorized Exception");
					break;
				case 4:
					Toast.makeText(AuthActivity.this,"未知错误",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Message Signer Exception");
					break;
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}

	final class AuthWebViewClient extends WebViewClient{
		public boolean shouldOverrideUrlLoading(WebView webView,String url){
			webView.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Toast.makeText(AuthActivity.this,"网页加载完成",Toast.LENGTH_LONG).show();
			counter++;
			WebView authWebView = (WebView)findViewById(R.id.auth_web_view);
			if(counter==2){
				verifier = authWebView.getUrl();
				//Log.v("WebView Debug","url = "+verifier);
				verifier = verifier.substring(verifier.indexOf("=")+1,verifier.indexOf("&"));
				//Log.v("WebView Debug","verifier = "+verifier);
				new GetOAuthAccess().execute("do");
			}

			super.onPageFinished(view, url);
		}

	}

	private class GetOAuthAccess extends AsyncTask<String,Integer,Integer>{

		@Override
		protected Integer doInBackground(String... strings) {
			try {
				provider.retrieveAccessToken(consumer,verifier);
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
				return 1;
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
				return 2;
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
				return 3;
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
				return 4;
			}
			//Log.v("OAuth","Access Token : "+consumer.getToken());
			//Log.v("OAuth","Access Token Secret : "+consumer.getTokenSecret());
			return 0;
		}

		@Override
		protected void onPreExecute() {
			Toast.makeText(AuthActivity.this,"开始获取Access Token...",Toast.LENGTH_LONG).show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result){
				case 0:
					Toast.makeText(AuthActivity.this,"获取Access Token成功\n即将进入主界面",Toast.LENGTH_LONG).show();
					MoeTuneDBManager moeTuneDBManager = MoeTuneDBManager.getInstance(AuthActivity.this);
					moeTuneDBManager.openDatabase();

					MoeTuneUser moeTuneUser = moeTuneDBManager.query();
					if(moeTuneUser!=null){
						moeTuneDBManager.delete(moeTuneUser.accessToken);
					}

					moeTuneDBManager.add(new MoeTuneUser(consumer.getToken(), consumer.getTokenSecret()));
					moeTuneDBManager.closeDatabase();
					mHandler.sendEmptyMessageDelayed(SWITCH_TO_MAIN, 3000);
					break;
				case 1:
					Toast.makeText(AuthActivity.this,"连接不上网络呢",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Communication Exception");
					break;
				case 2:
					Toast.makeText(AuthActivity.this,"服务器不响应呢",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Expectation Failed Exception");
					break;
				case 3:
					Toast.makeText(AuthActivity.this,"用户名/密码填错了？",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Not Authorized Exception");
					break;
				case 4:
					Toast.makeText(AuthActivity.this,"未知错误",Toast.LENGTH_LONG).show();
					//Log.v("OAuth","First Step Error : Message Signer Exception");
					break;
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case SWITCH_TO_MAIN:
//					Intent intent = new Intent();
//					intent.setClass(AuthActivity.this, MainActivity.class);
//					AuthActivity.this.startActivity(intent);
					AuthActivity.this.setResult(MoeTuneConstants.Actions.LOGIN_SUCCESS);
					AuthActivity.this.overridePendingTransition(R.anim.alpha_gradient_in,R.anim.alpha_gradient_out);
					AuthActivity.this.finish();
					break;
			}
			super.handleMessage(msg);
		}
	};
}
