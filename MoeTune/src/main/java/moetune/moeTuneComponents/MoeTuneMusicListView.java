package moetune.moeTuneComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import com.nineoldandroids.view.ViewHelper;
import moetune.core.MoeTuneMusic;
import util.MusicListAdapter;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 15/1/6
 * Project: MoeTune
 * Package: moetune.moeTuneComponents
 */
public class MoeTuneMusicListView extends ListView {

	private static final int TOUCH_UP = 0;
	private static final int TOUCH_DOWN = 1;
	private static final int TOUCH_MOVE_HORIZONTAL = 2;
	private static final int TOUCH_MOVE_VERTICAL = 3;

	private MusicListAdapter musicListAdapter;

	private float lastTouchPosX;
	private float lastTouchPosY;
	private float headerOriginPosY;
	private float lastLayoutPosY = 0;
	private float originalPosY;
	private float verticalOffset = 0;
	private float halfImageHeight;

	private int touchState = TOUCH_UP;
	private boolean isInitialized = false;

	private View headerImage;

	public MoeTuneMusicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		musicListAdapter = new MusicListAdapter(context);
		this.setAdapter(musicListAdapter);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(!isInitialized){
			initView();
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}



	private void initView(){
//		originalPosY = this.getY();
//		lastLayoutPosY = originalPosY;
		this.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int i) {

			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {

				if(getChildCount()==0){
					return;
				}

				float offset = getChildAt(0).getTop()/2;

				if(offset > 0){
					offset = 0;
				}

				if(offset <= -halfImageHeight){
					offset = -halfImageHeight;
				}

				if(headerImage!=null){
					ViewHelper.setY(headerImage,headerOriginPosY+offset);
				}

//				Log.v("Toggle Debug",getChildAt(0).getTop()+"");
			}
		});
		isInitialized = true;
	}

	public void refreshList(ArrayList<MoeTuneMusic> musicList){
		musicListAdapter.refreshList(musicList);
	}

	public void setPlayingIndex(int index){
		musicListAdapter.setPlayingIndex(index);
	}

	public void setHeaderImage(View headerImage) {
		this.headerImage = headerImage;
		headerOriginPosY = headerImage.getY();
		halfImageHeight = headerImage.getHeight()/2;
	}
}
