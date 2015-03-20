package moetune.activities;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.uexperience.moetune.R;
import moetune.core.*;
import moetune.fragments.FragmentHandler;
import moetune.fragments.NavigationDrawerFragment;
import moetune.moeTuneComponents.MoeTuneMusicListView;
import moetune.moeTuneComponents.SquareImageView;
import moetune.moeTuneComponents.ToggleRelativeLayout;
import util.Tool;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Todo: 目前设计思路:
	 * Todo:    01.（优先实现07方式）主界面专辑封面右下角添加圆形按钮“更多”菜单，用于显示收藏专辑、“以后不听”等功能。
	 * Todo:    02.主界面下三个按钮的功能分别为上一曲、播放/暂停、下一曲。上一曲仅限于App播放列表限制内可用。
	 * Todo:    03.以后可考虑添加长按曲目名/专辑名对曲目/专辑进行收藏的功能。
	 * Todo:    04.放弃使用Action Bar
	 * Todo:    05.在正在播放Activity左上角放置Spinner，用于切换播放模式。 现已更换为Floating Button。
	 * Todo:    06.切换曲目时使用位移渐变。专辑封面与文字需要有先后顺序的动感。
	 * Todo:    07.同时也可考虑左划封面/右划封面切换曲目、上划“以后不听”、下划收藏歌曲、长按上一曲/下一曲时快退/快进的功能。
	 * Todo:    08.关于上一曲，暂时考虑播放列表长度为30曲目，当播放至16曲时将第一个曲目推出列表并向服务器请求添加一首新曲目。
	 * Todo:    09.播放列表为预定添加的内容，最初版本暂不考虑。实现方式是双击专辑封面时弹出，覆盖专辑封面。
	 * Todo:    10.Navigation Drawer内条目分别为 用户信息、正在播放、发现、收藏、关于。搜索功能还在考虑中。
	 * Todo:    11.首先实现用户信息、正在播放、收藏、关于Activity。
	 * Todo:    12.除了以上页面外还需添加专辑信息Activity、用户详细信息/操作Activity。
	 * Todo:    13.缓存音频保存到本地？？可以编辑缓存音频？？
	 */

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private static final String ACTIVE_FRAGMENT_INDEX = "active_fragment_index";

    private NavigationDrawerFragment mNavigationDrawerFragment;
	private FragmentHandler fragmentHandler;
	private Bundle fragmentIndexSaver = new Bundle();
	private MoeTuneMusicService musicService;
	private boolean isStartedFromCreate = false;
	private boolean isFirstChangeAlbum = true;

	private View nowPlayingView;
	private ProgressBar musicProgress;
	private SquareImageView albumImage;
	private TextView timeCurrent;
	private TextView timeDuration;
	private TextView songTitle;
	private TextView albumName;
	private TextView artistName;
	private TextView albumMessage;
	private SquareImageView buttonPlayIcon;
	private Button buttonPlay;
	private Button buttonNext;
	private Button buttonPrev;
	private ActionBarDrawerToggle drawerToggle;
	private SquareImageView playlistAlbumImage;
	private Button playlistAlbumImageButton;
	private MoeTuneMusicListView playlistList;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			musicService = ((MoeTuneMusicService.MessageBinder)iBinder).getService();

			nowPlayingView = fragmentHandler.getNowPlayingView();
			if(nowPlayingView != null){
				musicProgress = (ProgressBar)nowPlayingView.findViewById(R.id.music_progress);
				timeCurrent = (TextView)nowPlayingView.findViewById(R.id.time_current);
				timeDuration = (TextView)nowPlayingView.findViewById(R.id.time_duration);
				songTitle = (TextView)nowPlayingView.findViewById(R.id.song_title);
				albumName = (TextView)nowPlayingView.findViewById(R.id.album_name);
				artistName = (TextView)nowPlayingView.findViewById(R.id.artist_name);
				albumMessage = (TextView)nowPlayingView.findViewById(R.id.album_message);
				albumImage = (SquareImageView)nowPlayingView.findViewById(R.id.album_image);
				buttonPlayIcon = (SquareImageView)nowPlayingView.findViewById(R.id.button_play_icon);
				buttonPlay = (Button)nowPlayingView.findViewById(R.id.button_play);
				buttonNext = (Button)nowPlayingView.findViewById(R.id.button_next);
				buttonPrev = (Button)nowPlayingView.findViewById(R.id.button_prev);
				playlistList = (MoeTuneMusicListView)nowPlayingView.findViewById(R.id.playlist_list);

				ToggleRelativeLayout musicInfoCard = (ToggleRelativeLayout) nowPlayingView.findViewById(R.id.music_info_card);
				RelativeLayout musicInfoCardShadow = (RelativeLayout) nowPlayingView.findViewById(R.id.music_info_card_shadow);
				FrameLayout playlistCover = (FrameLayout) nowPlayingView.findViewById(R.id.playlist_cover);
				TextView playlistTitle = (TextView)nowPlayingView.findViewById(R.id.playlist_title);
				TextView playlistSwipeHint = (TextView)nowPlayingView.findViewById(R.id.playlist_swipe_hint);
				View statusBarBackground = nowPlayingView.findViewById(R.id.status_bar_background);
				playlistAlbumImage = (SquareImageView)nowPlayingView.findViewById(R.id.playlist_album_image);

				musicInfoCard.setToolbarHeight(getSupportActionBar().getHeight());
				musicInfoCard.setBottomShadow(musicInfoCardShadow);
				musicInfoCard.setBackgroundDim(playlistCover);
				musicInfoCard.setPlaylistTitle(playlistTitle);
				musicInfoCard.setPlaylistSwipeHint(playlistSwipeHint);
				musicInfoCard.setPlaylistAlbum(playlistAlbumImage);
				musicInfoCard.setStatusBarBackground(statusBarBackground);

				MoeTuneMusicListView playlistList = (MoeTuneMusicListView)nowPlayingView.findViewById(R.id.playlist_list);
				RelativeLayout playlistHeader = (RelativeLayout)nowPlayingView.findViewById(R.id.playlist_header);
				playlistList.setHeaderImage(playlistHeader);

				buttonPlay.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if(musicService.isPlaying()){
							musicService.pause();
							buttonPlayIcon.setImageResource(R.drawable.icon_play);
						}else{
							musicService.play();
							buttonPlayIcon.setImageResource(R.drawable.icon_pause);
						}
					}
				});

				buttonNext.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						musicService.next();
					}
				});

				buttonPrev.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						musicService.fromBegin();
					}
				});

			}

			musicService.setOnMusicProgressUpdateListener(new OnMusicProgressUpdateListener() {
				@Override
				public void onProgressUpdate(int progress, int time) {
					if(musicProgress != null){
						musicProgress.setProgress(progress);
					}
					if(timeCurrent!=null){
						int minute = (int)Math.floor(time/60);
						int second = time % 60;
						if(minute<10) {
							if(second < 10){
								timeCurrent.setText("0"+ minute + ":" + "0" + (time % 60));
							}else{
								timeCurrent.setText("0"+ minute + ":" + (time % 60));
							}
						}else{
							if(second < 10){
								timeCurrent.setText(minute + ":" + "0" + (time % 60));
							}else{
								timeCurrent.setText(minute + ":" + (time % 60));
							}
						}
					}
				}
			});

			musicService.setOnMusicStateChangedListener(new OnMusicStateChangedListener() {
				@Override
				public void onMusicStateChanged() {
					if(musicService.isPlaying()){
						buttonPlayIcon.setImageResource(R.drawable.icon_pause);
					}else{
						buttonPlayIcon.setImageResource(R.drawable.icon_play);
					}
					if(musicService.isComplete()){
						Drawable drawable = albumImage.getDrawable();
						if (drawable != null && !isFirstChangeAlbum) {
							if (drawable instanceof BitmapDrawable) {
								BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
								Bitmap bitmapRecycle = bitmapDrawable.getBitmap();
								if (bitmapRecycle != null) {
									bitmapRecycle.recycle();
									isFirstChangeAlbum = false;
								}
							}
						}
						albumImage.setImageResource(R.drawable.album_default);
						playlistAlbumImage.setImageResource(R.drawable.album_default);
						albumMessage.setVisibility(View.VISIBLE);
					}
					musicService.changeNotificationState();
				}

				@Override
				public void onMusicInfoChanged(String streamTime, String wikiTitle, String subTitle, String artist) {
					timeCurrent.setText("00:00");
					timeDuration.setText(" / " + streamTime);
					songTitle.setText(subTitle);
					albumName.setText(wikiTitle);
					artistName.setText(artist);
					musicProgress.setProgress(0);
				}

				@Override
				public void onMusicPreparing() {
					buttonPlay.setEnabled(false);
					buttonNext.setEnabled(false);
					buttonPrev.setEnabled(false);
					buttonPlayIcon.setImageResource(R.drawable.icon_play);
				}

				@Override
				public void onMusicPrepared(int index) {
					buttonPlay.setEnabled(true);
					buttonNext.setEnabled(true);
					buttonPrev.setEnabled(true);
					albumMessage.setVisibility(View.GONE);
					albumMessage.setText(R.string.album_loading);
					musicService.play();
					buttonPlayIcon.setImageResource(R.drawable.icon_pause);
					playlistList.setPlayingIndex(index);
					System.gc();
				}

				@Override
				public void onCoverPrepared(Bitmap cover) {
					albumImage.setImageBitmap(cover);
					albumImage.invalidate();
					playlistAlbumImage.setImageBitmap(cover);
					playlistAlbumImage.invalidate();
					albumMessage.setVisibility(View.GONE);
					System.gc();
				}
			});

			musicService.setOnMusicErrorListener(new OnMusicErrorListener() {
				@Override
				public void onMusicError(int errorCode) {
					switch (errorCode){
						case MoeTuneConstants.Error.PLAYER_CANNOT_CONNECT_NETWORK:
							buttonPlay.setEnabled(false);
							buttonNext.setEnabled(false);
							buttonPrev.setEnabled(false);
							albumMessage.setText(R.string.album_cannot_connect_wifi);
							buttonPlayIcon.setImageResource(R.drawable.icon_play);
							break;
					}
				}
			});

			musicService.setOnMusicListLoadedListener(new OnMusicListLoadedListener() {
				@Override
				public void onMusicListLoaded(ArrayList<MoeTuneMusic> musicList) throws IOException {
					playlistList.refreshList(musicList);
				}
			});
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {

		}
	};

	@Override
    protected void onCreate(Bundle savedInstanceState) {
	    Log.v("Debug","Activity On Create");
        super.onCreate(savedInstanceState);

	    fragmentHandler = new FragmentHandler();
	    fragmentHandler.initFragments();
	    fragmentIndexSaver.putInt(ACTIVE_FRAGMENT_INDEX,-1);

	    setContentView(R.layout.activity_main);
	    //getActionBar().hide();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
		        (DrawerLayout) findViewById(R.id.drawer_layout));

		mNavigationDrawerFragment.setOnLoginEventCalledListener(new OnLoginEventCalledListener() {
			@Override
			public void onLoginEventCalled() {
				//Log.v("Config Debug", "未绑定，进入Auth Activity。");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AuthActivity.class);
				MainActivity.this.startActivityForResult(intent, MoeTuneConstants.Actions.LOGIN_REQUEST);
				MainActivity.this.overridePendingTransition(R.anim.alpha_gradient_in, R.anim.alpha_gradient_out);
			}
		});

	    /**
	     * Todo:在读取歌曲列表的过程中添加一个简单的读取画面
	     **/

	    if(!Tool.isServiceRunning(this)){
		    //开启服务
		    isStartedFromCreate = true;
		    Intent intent = new Intent();
		    intent.setClass(MainActivity.this, MoeTuneMusicService.class);
		    intent.putExtra("user_type",getIntent().getExtras().getInt("user_type"));
		    startService(intent);
		    bindService(intent, serviceConnection, BIND_AUTO_CREATE);
	    }else{
		    isStartedFromCreate = false;
		    Intent intent = new Intent();
		    intent.setClass(MainActivity.this, MoeTuneMusicService.class);
		    intent.putExtra("user_type",getIntent().getExtras().getInt("user_type"));
		    bindService(intent, serviceConnection, BIND_AUTO_CREATE);
	    }

		initToolbar();

		if (android.os.Build.VERSION.SDK_INT >= 21) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color_default));
		}
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode){
			case MoeTuneConstants.Actions.LOGIN_REQUEST:
				SharedPreferences sharedPreferences = getSharedPreferences(MoeTuneConstants.Config.PREFERENCE_NAME,0);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				switch (resultCode){
					case MoeTuneConstants.Actions.LOGIN_SUCCESS:
						//Log.v("Config Debug", "返回确认为认证成功");
						editor.putBoolean(MoeTuneConstants.Config.IS_MEMBER_REGISTERED,true);
						editor.apply();
						mNavigationDrawerFragment.checkLoginState();
						break;
					case MoeTuneConstants.Actions.LOGIN_FAILED:
						//Log.v("Config Debug", "返回确认为认证失败");
						editor.putBoolean(MoeTuneConstants.Config.IS_MEMBER_REGISTERED,false);
						editor.apply();
						mNavigationDrawerFragment.checkLoginState();
						break;
				}
				break;
		}
	}

	@Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
	    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		int prePos = fragmentIndexSaver.getInt(ACTIVE_FRAGMENT_INDEX);

		if(position == 2){
			MainActivity.this.finish();
			return;
		}

	    if(prePos!=position){
		    if(prePos != -1&&fragmentHandler.getFragments()[position].isAdded()){
			    fragmentTransaction
					    .setCustomAnimations(R.animator.fragment_alpha_in,R.animator.fragment_alpha_out)
					    .hide(fragmentHandler.getFragments()[prePos])
					    .show(fragmentHandler.getFragments()[position])
					    .commit();
		    }else if(prePos == -1){
			    fragmentTransaction
					    .setCustomAnimations(R.animator.fragment_alpha_in,R.animator.fragment_alpha_out)
					    .add(R.id.container, fragmentHandler.getFragments()[position])
					    .commit();
		    }else{
			    fragmentTransaction.hide(fragmentHandler.getFragments()[prePos])
					    .setCustomAnimations(R.animator.fragment_alpha_in,R.animator.fragment_alpha_out)
					    .add(R.id.container, fragmentHandler.getFragments()[position])
					    .commit();
		    }
		    fragmentIndexSaver.putInt(ACTIVE_FRAGMENT_INDEX, position);
	    }
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

	@Override
	protected void onStop() {
		Log.v("Debug", "Activity On Stop");
		//unbindService(serviceConnection);
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v("Debug","Activity On Pause");
	}

	@Override
	protected void onResume() {
		Log.v("Debug", "Activity On Resume");
		if(Tool.isServiceRunning(MainActivity.this) && !isStartedFromCreate){
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, MoeTuneMusicService.class);
			bindService(intent, serviceConnection, 0);
			//musicService.setIsBinded(true);
		}
		super.onResume();
	}

	@Override
	protected void onRestart() {
		Log.v("Debug", "Activity On Restart");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.v("Debug", "Activity On Start");
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		stopService(new Intent().setClass(MainActivity.this, MoeTuneMusicService.class));
		System.exit(0);
		super.onDestroy();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		switch (event.getKeyCode()){
			case KeyEvent.KEYCODE_BACK:
				if(mNavigationDrawerFragment.isDrawerOpen()){
					mNavigationDrawerFragment.getDrawer().closeDrawer(mNavigationDrawerFragment.getFragmentContainer());
					return true;
				}
				if(event.getAction()!=KeyEvent.ACTION_UP){
					Intent intent = new Intent();
					intent.setAction("android.intent.action.MAIN");
					intent.addCategory("android.intent.category.HOME");
					startActivity(intent);
				}
				return true;
			case KeyEvent.KEYCODE_MENU:
				if(mNavigationDrawerFragment.isDrawerOpen()){
					mNavigationDrawerFragment.getDrawer().closeDrawer(mNavigationDrawerFragment.getFragmentContainer());
				}else{
					mNavigationDrawerFragment.getDrawer().openDrawer(mNavigationDrawerFragment.getFragmentContainer());
				}
				return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	private void initToolbar(){
		Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mNavigationDrawerFragment.isDrawerOpen()){
					mNavigationDrawerFragment.getDrawer().closeDrawer(mNavigationDrawerFragment.getFragmentContainer());
				}else{
					mNavigationDrawerFragment.getDrawer().openDrawer(mNavigationDrawerFragment.getFragmentContainer());
				}
			}
		});

		DrawerLayout drawerLayout = mNavigationDrawerFragment.getDrawer();

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

			@Override
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

		};
		drawerLayout.setDrawerListener(drawerToggle);
	}
}
