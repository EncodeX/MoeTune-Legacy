package moetune.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.nineoldandroids.view.ViewHelper;
import com.uexperience.moetune.R;
import moetune.moeTuneComponents.SquareImageView;
import moetune.moeTuneComponents.ToggleRelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Encode_X on 14-6-19.
 */
public class ViewHandler {
	private static View nowPlayingView = null;

	public ViewHandler() {}

	public View getNowPlayingView(){
		if(nowPlayingView != null){
			return nowPlayingView;
		}
		return null;
	}

	public View getNowPlayingView(final LayoutInflater inflater, ViewGroup container){

		if(nowPlayingView != null){
			return nowPlayingView;
		}

		nowPlayingView = inflater.inflate(R.layout.fragment_now_playing, container, false);

		final SquareImageView buttonPlayIcon = (SquareImageView) nowPlayingView.findViewById(R.id.button_play_icon);
		final SquareImageView buttonNextIcon = (SquareImageView) nowPlayingView.findViewById(R.id.button_next_icon);
		final SquareImageView buttonPrevIcon = (SquareImageView) nowPlayingView.findViewById(R.id.button_prev_icon);

		Button buttonPlay = (Button) nowPlayingView.findViewById(R.id.button_play);
		Button buttonNext = (Button) nowPlayingView.findViewById(R.id.button_next);
		final Button buttonPrev = (Button) nowPlayingView.findViewById(R.id.button_prev);
		Button openPlaylistButton = (Button)nowPlayingView.findViewById(R.id.open_playlist_button);
		TextView songTitle = (TextView)nowPlayingView.findViewById(R.id.song_title);
		final ToggleRelativeLayout musicInfoCard = (ToggleRelativeLayout)nowPlayingView.findViewById(R.id.music_info_card);
		SquareImageView playlistAlbumImage = (SquareImageView)nowPlayingView.findViewById(R.id.playlist_album_image);
		View whatButton = nowPlayingView.findViewById(R.id.what_button);

		whatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.v("Touch test", "what button clicked");
			}
		});

		buttonPlay.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()){
					case MotionEvent.ACTION_DOWN:
						buttonPlayIcon.setScaleX(0.85f);
						buttonPlayIcon.setScaleY(0.85f);
						break;
					case MotionEvent.ACTION_UP:
						buttonPlayIcon.setScaleX(1.0f);
						buttonPlayIcon.setScaleY(1.0f);
						break;
				}
				return false;
			}
		});
		buttonPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
		buttonNext.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()){
					case MotionEvent.ACTION_DOWN:
						buttonNextIcon.setScaleX(0.85f);
						buttonNextIcon.setScaleY(0.85f);
						break;
					case MotionEvent.ACTION_UP:
						buttonNextIcon.setScaleX(1.0f);
						buttonNextIcon.setScaleY(1.0f);
						break;
				}
				return false;
			}
		});
		buttonPrev.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					case MotionEvent.ACTION_DOWN:
						buttonPrevIcon.setScaleX(0.85f);
						buttonPrevIcon.setScaleY(0.85f);
						break;
					case MotionEvent.ACTION_UP:
						buttonPrevIcon.setScaleX(1.0f);
						buttonPrevIcon.setScaleY(1.0f);
						break;
				}
				return false;
			}
		});

		songTitle.setMovementMethod(ScrollingMovementMethod.getInstance());

		openPlaylistButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				musicInfoCard.openMenu();
			}
		});

		playlistAlbumImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.v("Touch test","touched");
				musicInfoCard.closeMenu();
			}
		});

		return nowPlayingView;
	}

	public View getAboutView(final LayoutInflater inflater, ViewGroup container){
		View aboutView = inflater.inflate(R.layout.fragment_about, container, false);

		Button aboutRateButton = (Button)aboutView.findViewById(R.id.about_rate_button);

		aboutRateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// 建立一個Intent - 在這個Intent 上使用 Google Play Store 的連結
				// E.G. market://details?id=
				// 之後用 getPackageName 這個功能來取後這個程式的 Namespace.
				Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + inflater.getContext().getPackageName()));

				try {
					// 之後開始一個新的Activity 去這個Intent
					inflater.getContext().startActivity(goToMarket);
				} catch (ActivityNotFoundException e) {
					// 如果有錯誤的話 使用正常的網址來連接到 Google Play Store的網頁
					inflater.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + inflater.getContext().getPackageName())));
				}
			}
		});

		return aboutView;
	}
}
