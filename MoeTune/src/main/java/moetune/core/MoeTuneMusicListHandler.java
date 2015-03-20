package moetune.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.MoeTuneDBManager;
import util.Tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 14-9-21
 * Project: ${PROJECT_NAME}
 * Package: ${PACKAGE_NAME}
 */
public class MoeTuneMusicListHandler {
	private final static int TYPE_GUEST = 0;
	private final static int TYPE_MEMBER = 1;

	ArrayList<MoeTuneMusic> musicList;

	private OAuthConsumer consumer = new CommonsHttpOAuthConsumer(util.OAuthKey.OAUTH_CONSUMER_KEY,util.OAuthKey.OAUTH_CONSUMER_SECRET);
	private MoeTuneUser moeTuneUser;
	private OnMusicListLoadedListener onMusicListLoadedListener;
	private OnMusicListErrorListener onMusicListErrorListener;
	private int nowPlayingIndex;
	private Context context;
	private int currentUserType;
	private int errorCode;
	private boolean isErrorOccurred = false;

	public MoeTuneMusicListHandler(Context context) {
		musicList = new ArrayList<MoeTuneMusic>();
		nowPlayingIndex = 0;
		this.context = context;
	}

	public void loadMusicList(int num, int userType){
		new MusicListHandler().execute(num,userType);
		currentUserType = userType;
	}

	private void addMusicFromJSON(JSONArray jsonList) throws JSONException {
		for (int i = 0;i<jsonList.length();i++){
			JSONObject jsonMusic = jsonList.getJSONObject(i);
			JSONObject jsonCoverUrl = jsonMusic.getJSONObject("cover");
			MoeTuneMusic moeTuneMusic = new MoeTuneMusic();

			moeTuneMusic.setUpId(jsonMusic.optString("up_id"));
			moeTuneMusic.setUrl(Tool.urlReplace(jsonMusic.optString("url")));
			moeTuneMusic.setStreamLength(jsonMusic.optInt("stream_length"));
			moeTuneMusic.setStreamTime(jsonMusic.optString("stream_time"));
			moeTuneMusic.setFileSize(jsonMusic.optInt("file_size"));
			moeTuneMusic.setFileType(jsonMusic.optString("file_type"));
			moeTuneMusic.setWikiId(jsonMusic.optInt("wiki_id"));
			moeTuneMusic.setWikiType(jsonMusic.optString("wiki_type"));
			moeTuneMusic.setTitle(Tool.symbolReplace(jsonMusic.optString("title")));
			moeTuneMusic.setWikiTitle(Tool.symbolReplace(jsonMusic.getString("wiki_title")));
			moeTuneMusic.setWikiUrl(Tool.urlReplace(jsonMusic.optString("wiki_url")));
			moeTuneMusic.setSubId(jsonMusic.getInt("sub_id"));
			moeTuneMusic.setSubType(jsonMusic.getString("sub_type"));
			moeTuneMusic.setSubTitle(Tool.symbolReplace(jsonMusic.getString("sub_title")));
			moeTuneMusic.setSubUrl(Tool.urlReplace(jsonMusic.getString("sub_url")));
			moeTuneMusic.setArtist(Tool.symbolReplace(jsonMusic.getString("artist")));

			moeTuneMusic.setCoverUrl(
					new MoeTuneMusicCoverUrl(
							Tool.urlReplace(jsonCoverUrl.getString("small")),
							Tool.urlReplace(jsonCoverUrl.getString("medium")),
							Tool.urlReplace(jsonCoverUrl.getString("square")),
							Tool.urlReplace(jsonCoverUrl.getString("large"))
					)
			);

			if(currentUserType != TYPE_GUEST){
				if(jsonMusic.getString("fav_wiki").equals("null")){
					moeTuneMusic.setFavWiki(false);
				}else{
					moeTuneMusic.setFavWiki(true);
				}
				if(jsonMusic.getString("fav_sub").equals("null")){
					moeTuneMusic.setFavSub(false);
				}else{
					moeTuneMusic.setFavSub(true);
				}
			}else{
				moeTuneMusic.setFavWiki(false);
				moeTuneMusic.setFavSub(false);
			}

			musicList.add(moeTuneMusic);
		}
	}

	public MoeTuneMusic getSong(){
		return musicList.get(nowPlayingIndex);
	}

	public int getNowPlayingIndex() {
		return nowPlayingIndex;
	}

	public boolean isErrorOccurred(){
		return isErrorOccurred;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setNowPlayingIndex(int nowPlayingIndex) {
		this.nowPlayingIndex = nowPlayingIndex;
		loadMusicListCheck();
	}

	public void signListened(){
		if(currentUserType == TYPE_GUEST){
			return;
		}
		new MusicListenedSigner().execute();
	}

	public void loadMusicListCheck(){
		Log.v("Network Debug","Now Playing Index : "+nowPlayingIndex);
		if(nowPlayingIndex>15){
			for(int i=0;i<15;i++){
				musicList.remove(0);
			}
			nowPlayingIndex -= 15;
			loadMusicList(15,currentUserType);
		}
	}

	private class MusicListHandler extends AsyncTask<Integer,Integer,Integer> {
		@Override
		protected Integer doInBackground(Integer... integers) {

			if(Tool.checkNetworkState(context)!=MoeTuneConstants.NetworkState.NETWORK_WIFI){
				return 2;
			}

			if(integers[1] == TYPE_GUEST){
				try {
					HttpGet request = new HttpGet("http://moe.fm/listen/playlist?api=json&perpage="+integers[0]+
							"&api_key=e70358879687af47b1f5842b800dfb6605180b3dd");
					HttpClient client = new DefaultHttpClient();
					//Log.v("Access Debug", "Sending request...");
					HttpResponse response = client.execute(request);
					if(response.getStatusLine().getStatusCode()==401){
						return 1;
					}else{
						StringBuilder stringBuilder = new StringBuilder();
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						for(String s = bufferedReader.readLine();s!=null;s=bufferedReader.readLine()){
							stringBuilder.append(s);
						}
						JSONArray jsonList = new JSONObject(stringBuilder.toString()).getJSONObject("response")
								.getJSONArray("playlist");
						addMusicFromJSON(jsonList);
						return 0;
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					//Log.v("Network Debug","List load Error");
					return 2;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if(integers[1]== TYPE_MEMBER){
				MoeTuneDBManager.getInstance(context).openDatabase();
				moeTuneUser = MoeTuneDBManager.getInstance(context).query();
				if(moeTuneUser != null){
					consumer.setTokenWithSecret(moeTuneUser.accessToken,moeTuneUser.accessTokenSecret);
					try {
						HttpGet request = new HttpGet("http://moe.fm/listen/playlist?api=json&perpage="+integers[0]);
						HttpClient client = new DefaultHttpClient();
						consumer.sign(request);
						//Log.v("Access Debug", "Sending request...");
						HttpResponse response = client.execute(request);
						if(response.getStatusLine().getStatusCode()==401){
							return 1;
						}else{
							StringBuilder stringBuilder = new StringBuilder();
							BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							for(String s = bufferedReader.readLine();s!=null;s=bufferedReader.readLine()){
								stringBuilder.append(s);
							}
							JSONArray jsonList = new JSONObject(stringBuilder.toString()).getJSONObject("response")
									.getJSONArray("playlist");
							addMusicFromJSON(jsonList);
							return 0;
						}
					} catch (MalformedURLException e) {
						//Log.v("Debug","Here I Got You : MalformedURLException");
						e.printStackTrace();
					} catch (IOException e) {
						//Log.v("Network Debug","List load Error");
						MoeTuneDBManager.getInstance(context).closeDatabase();
						return 2;
					} catch (OAuthExpectationFailedException e) {
						//Log.v("Debug","Here I Got You : OAuthExpectationFailedException");
						e.printStackTrace();
					} catch (OAuthCommunicationException e) {
						//Log.v("Debug","Here I Got You : OAuthCommunicationException");
						e.printStackTrace();
					} catch (OAuthMessageSignerException e) {
						//Log.v("Debug","Here I Got You : OAuthMessageSignerException");
						e.printStackTrace();
					} catch (JSONException e) {
						//Log.v("Debug","Here I Got You : JSONException");
						e.printStackTrace();
					}
				}
				MoeTuneDBManager.getInstance(context).closeDatabase();
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
					//Log.v("Music Debug","获取列表成功");
					break;
				case 1:
					//Log.v("Music Debug","用户验证错误");
					break;
				case 2:
					errorCode = MoeTuneConstants.Error.LIST_GET_ERROR;
					isErrorOccurred = true;
					onMusicListErrorListener.onMusicListError(errorCode);
					//Log.v("Music Debug","发生网络错误 停止操作 isErrorOccurred = true");
					return;
			}
			if(onMusicListLoadedListener!=null && !musicList.isEmpty()){
				try {
					onMusicListLoadedListener.onMusicListLoaded(musicList);
				} catch (IOException e) {
					//Log.v("Network debug","On Music List Loaded Error");
				}
			}
			if(isErrorOccurred){
				isErrorOccurred = false;
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}

	private class MusicListenedSigner extends AsyncTask<Integer,Integer,Integer>{

		@Override
		protected Integer doInBackground(Integer... integers) {
			if(context.getSharedPreferences(MoeTuneConstants.Config.PREFERENCE_NAME, 0)
				.getBoolean(MoeTuneConstants.Config.IS_MEMBER_REGISTERED, false)){
				try {
					HttpGet request = new HttpGet("http://moe.fm/ajax/log?log_obj_type=sub&log_type=listen" +
							"&obj_type=song&api=format&obj_id=" + getSong().getSubId());
					HttpClient client = new DefaultHttpClient();
					consumer.sign(request);
					HttpResponse response = client.execute(request);
					if(response.getStatusLine().getStatusCode()==401){
						//Log.v("Debug","用户验证错误");
					}else{
						//Log.v("Debug","标记已听成功 返回码："+response.getStatusLine().getStatusCode());
					}
				} catch (IOException e) {
					return 1;
				} catch (OAuthExpectationFailedException e) {
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					e.printStackTrace();
				} catch (OAuthMessageSignerException e) {
					e.printStackTrace();
				}
				if(isErrorOccurred){
					isErrorOccurred = false;
				}
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer integer) {
			if(integer == 1){
				isErrorOccurred = true;
				errorCode = MoeTuneConstants.Error.LIST_SIGN_ERROR;
				onMusicListErrorListener.onMusicListError(errorCode);
			}
		}
	}

	public void setOnMusicListLoadedListener(OnMusicListLoadedListener onMusicListLoadedListener) {
		this.onMusicListLoadedListener = onMusicListLoadedListener;
	}

	public void setOnMusicListErrorListener(OnMusicListErrorListener onMusicListErrorListener) {
		this.onMusicListErrorListener = onMusicListErrorListener;
	}
}