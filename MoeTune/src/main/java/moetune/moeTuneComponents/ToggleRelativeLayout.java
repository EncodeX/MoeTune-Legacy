package moetune.moeTuneComponents;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Encode_X on 14-10-12.
 * Project LabProject
 * Package neu.labproject.campus.views
 */
public class ToggleRelativeLayout extends RelativeLayout {

	/**
	 * 轻量级可拖拽Layout View ToggleRelativeLayout
	 * Todo:若想加入缩放效果的话将pivot设置在0X和0.5Y处
	 */

	private static final int TOUCH_UP = 0;
	private static final int TOUCH_DOWN = 1;
	private static final int TOUCH_MOVE_HORIZONTAL = 2;
	private static final int TOUCH_MOVE_VERTICAL = 3;

	/**临时变量**/
	private float lastTouchPosX;
	private float lastTouchPosY;
	private float lastLayoutPosX=0;
	private float lastLayoutPosY=0;
	private float viewWidth;
	private float viewHeight;
	private float toolbarHeight = 0;
	private float statusBarHeight = 0;
	/**状态**/
	private int touchState = TOUCH_UP;
	private boolean isMenuOpened = false;
	private boolean isInitialized = false;
	private boolean isInIgnoredViews = false;
//	private boolean isFirst
	private boolean toggleSwitch = true;
	private boolean isStatusBarSolid = false;
	/**瞬间动作判断**/
	private VelocityTracker velocityTracker;

	private View contentMask;
	private View backgroundDim;
	private View bottomShadow;
	private View playlistTitle;
	private View playlistSwipeHint;
	private View playlistAlbum;
	private View statusBarBackground;
	private List<View> ignoredViews;
	private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(4.0f);

	/**Constructors**/
	public ToggleRelativeLayout(Context context) {
		super(context);
	}

	public ToggleRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ToggleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 修改dispatchTouchEvent 使view本身跟随手指横向运动并在手指离开屏幕时
	 * 作出判断从而显示/遮盖view后的内容
	 * @param ev
	 * @return
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if(!isInitialized){
			initView();
		}

		createVelocityTracker(ev);
		switch (ev.getAction()){
			case MotionEvent.ACTION_DOWN:
				lastTouchPosX = ev.getRawX();
				lastTouchPosY = ev.getRawY();
				touchState = TOUCH_DOWN;
				isInIgnoredViews = isInIgnoredViews(ev) && !isMenuOpened;
				break;
			case MotionEvent.ACTION_MOVE:

				if(!toggleSwitch){break;}

				if(touchState != TOUCH_DOWN && touchState != TOUCH_MOVE_VERTICAL){break;}

				if(isInIgnoredViews){break;}

				float xOffset = ev.getRawX() - lastTouchPosX;
				float yOffset = ev.getRawY() - lastTouchPosY;

				switch(touchState){
					case TOUCH_DOWN:
						//判断方向
						if(Math.abs(xOffset) > 25){
							touchState = TOUCH_MOVE_HORIZONTAL;
							break;
						}else if(Math.abs(yOffset) > 50){
							touchState = TOUCH_MOVE_VERTICAL;
							ev.setAction(MotionEvent.ACTION_CANCEL);
						}
						break;
					case TOUCH_MOVE_VERTICAL:
						if(android.os.Build.VERSION.SDK_INT>=21){
							if(lastLayoutPosY+yOffset >= 0){
								ViewHelper.setY(this, 0);
								if(bottomShadow!=null){
									ViewHelper.setY(bottomShadow, viewHeight);
								}
								if(backgroundDim!=null){
									ViewHelper.setAlpha(backgroundDim, 1.0f);
								}
								if(statusBarBackground!=null){
									ViewHelper.setAlpha(statusBarBackground,0.0f);
								}
								return true;
							}
							if(lastLayoutPosY+yOffset<=-viewHeight + toolbarHeight + statusBarHeight) {
								ViewHelper.setY(this, -viewHeight + toolbarHeight + statusBarHeight);
								if(bottomShadow!=null){
									ViewHelper.setY(bottomShadow, toolbarHeight + statusBarHeight);
								}
								if(backgroundDim!=null) {
									ViewHelper.setAlpha(backgroundDim, 0.0f);
								}
								if(statusBarBackground!=null){
									ViewHelper.setAlpha(statusBarBackground,1.0f);
								}
								isStatusBarSolid = true;
								return true;
							}
							float alpha = 1.0f+(lastLayoutPosY+yOffset)/(viewHeight-toolbarHeight);
							ViewHelper.setY(this,lastLayoutPosY+yOffset);
							if(bottomShadow!=null){
								ViewHelper.setY(bottomShadow, lastLayoutPosY + yOffset + viewHeight);
							}
							if(backgroundDim!=null){
								ViewHelper.setAlpha(backgroundDim, alpha);
							}
							if(statusBarBackground!=null){
								ViewHelper.setAlpha(statusBarBackground,1.0f-alpha);
							}
							if(isStatusBarSolid) {
								isStatusBarSolid = false;
							}
							return true;
						}else{
							if(lastLayoutPosY+yOffset >= 0){
								ViewHelper.setY(this, 0);
								if(bottomShadow!=null){
									ViewHelper.setY(bottomShadow, viewHeight);
								}
								if(backgroundDim!=null){
									ViewHelper.setAlpha(backgroundDim, 1.0f);
								}
								return true;
							}
							if(lastLayoutPosY+yOffset<=-viewHeight + toolbarHeight) {
								ViewHelper.setY(this, -viewHeight + toolbarHeight);
								if(bottomShadow!=null){
									ViewHelper.setY(bottomShadow, toolbarHeight);
								}
								if(backgroundDim!=null){
									ViewHelper.setAlpha(backgroundDim, 0.0f);
								}
								return true;
							}
							float alpha = 1.0f+(lastLayoutPosY+yOffset)/(viewHeight-toolbarHeight);
							ViewHelper.setY(this,lastLayoutPosY+yOffset);
							if(bottomShadow!=null){
								ViewHelper.setY(bottomShadow, lastLayoutPosY + yOffset + viewHeight);
							}
							if(backgroundDim!=null){
								ViewHelper.setAlpha(backgroundDim, alpha);
							}
							return true;
						}
				}
				break;
			case MotionEvent.ACTION_UP:
				if(touchState==TOUCH_DOWN){touchState = TOUCH_UP;break;}
				if(touchState != TOUCH_MOVE_VERTICAL){break;}
				touchState = TOUCH_UP;
				lastLayoutPosY+=ev.getRawY() - lastTouchPosY;

				//判断瞬时移动速度 优先手指动作判断来进行view拉开/收起动作
				if(getScrollVelocity()>200){
					//回收Tracker
					recycleVelocityTracker();
					if((ev.getRawY() - lastTouchPosY)<0){
						openMenu();
						return true;
					}else {
						closeMenu();
						return true;
					}
				}

				//手指慢速放开 则判断终止位置
				if(isMenuOpened){
					if(lastLayoutPosY>-viewHeight*0.5){
						closeMenu();
					}else{
						openMenu();
					}
					recycleVelocityTracker();
					return true;
				}else{
					if(lastLayoutPosY<-viewHeight*0.3){
						openMenu();
					}else{
						closeMenu();
					}
					recycleVelocityTracker();
					return true;
				}
		}
		return super.dispatchTouchEvent(ev);
	}

	private void initView(){
		viewWidth = new Double(this.getWidth()).floatValue();
		viewHeight = new Double(this.getHeight()).floatValue();

		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}

		isInitialized = true;
	}

	/**
	 * 拉开View
	 */
	public void openMenu(){
		if(!isInitialized){
			initView();
		}
		isMenuOpened=true;
		AnimatorSet slideToOpen;
		if(android.os.Build.VERSION.SDK_INT>=21){
			slideToOpen = buildSlideAnimation(this,-viewHeight+toolbarHeight+statusBarHeight,1.0f,1.0f);
			if(bottomShadow!=null){
				AnimatorSet shadowSlideOpen = buildSlideAnimation(bottomShadow, toolbarHeight + statusBarHeight, 1.0f, 1.0f);
				slideToOpen.playTogether(shadowSlideOpen);
			}
		}else{
			slideToOpen = buildSlideAnimation(this,-viewHeight+toolbarHeight,1.0f,1.0f);
			if(bottomShadow!=null){
				AnimatorSet shadowSlideOpen = buildSlideAnimation(bottomShadow, toolbarHeight, 1.0f, 1.0f);
				slideToOpen.playTogether(shadowSlideOpen);
			}
		}
		slideToOpen.addListener(animationListener);
		if(backgroundDim!=null){
			AnimatorSet dimOpen = buildAlphaAnimation(backgroundDim, 0.0f);
			slideToOpen.playTogether(dimOpen);
		}
		if(playlistTitle!=null){
			playlistTitle.setVisibility(VISIBLE);
			ViewHelper.setAlpha(playlistTitle,0.0f);
			AnimatorSet open = buildAlphaAnimation(playlistTitle, 1.0f);
			slideToOpen.playTogether(open);
		}
		if(playlistSwipeHint!=null){
			playlistSwipeHint.setVisibility(GONE);
		}
		if(playlistAlbum!=null){
			playlistAlbum.setVisibility(VISIBLE);
			ViewHelper.setAlpha(playlistAlbum,0.0f);
			AnimatorSet open = buildAlphaAnimation(playlistAlbum, 1.0f);
			slideToOpen.playTogether(open);
		}

		if(android.os.Build.VERSION.SDK_INT>=21){
			if(statusBarBackground!=null){
				AnimatorSet statusBarOpen = buildAlphaAnimation(statusBarBackground, 1.0f);
				slideToOpen.playTogether(statusBarOpen);
			}
			lastLayoutPosY=-viewHeight+toolbarHeight+statusBarHeight;
		}else{
			lastLayoutPosY=-viewHeight+toolbarHeight;
		}
		slideToOpen.start();
		if(contentMask!=null){
			contentMask.setVisibility(VISIBLE);
		}
	}

	/**
	 * 闭合View
	 */
	public void closeMenu(){
		isMenuOpened=false;
		AnimatorSet slideToClose = buildSlideAnimation(this,0,1.0f,1.0f);
		slideToClose.addListener(animationListener);
		if(bottomShadow!=null){
			AnimatorSet shadowSlideClose = buildSlideAnimation(bottomShadow, viewHeight, 1.0f, 1.0f);
			slideToClose.playTogether(shadowSlideClose);
		}
		if(backgroundDim!=null){
			AnimatorSet dimClose = buildAlphaAnimation(backgroundDim, 1.0f);
			slideToClose.playTogether(dimClose);
		}
		if(android.os.Build.VERSION.SDK_INT>=21){
			if(statusBarBackground!=null){
				AnimatorSet statusBarClose = buildAlphaAnimation(statusBarBackground, 0.0f);
				slideToClose.playTogether(statusBarClose);
			}
		}

		slideToClose.start();
		if(contentMask!=null){
			contentMask.setVisibility(GONE);
		}
		lastLayoutPosY=0;
	}

	/**
	 * 滑动动画
	 * @param target
	 * @param targetPosY
	 * @return
	 */
	private AnimatorSet buildSlideAnimation(View target, float targetPosY , float scaleX , float scaleY){

		AnimatorSet slideAnimation = new AnimatorSet();
		slideAnimation.playTogether(
				ObjectAnimator.ofFloat(target, "translationY",targetPosY),
				ObjectAnimator.ofFloat(target, "scaleX", scaleX),
				ObjectAnimator.ofFloat(target, "scaleY", scaleY)
		);
		slideAnimation.setInterpolator(decelerateInterpolator);

		slideAnimation.setDuration(500);
		return slideAnimation;
	}

	private AnimatorSet buildAlphaAnimation(View target, float alpha){

		AnimatorSet menuSlideAnimation = new AnimatorSet();
		menuSlideAnimation.playTogether(
				ObjectAnimator.ofFloat(target, "alpha", alpha)
		);
		menuSlideAnimation.setInterpolator(decelerateInterpolator);

		menuSlideAnimation.setDuration(500);
		return menuSlideAnimation;
	}

	/**
	 * 创建Tracker
	 * @param ev
	 */
	private void createVelocityTracker(MotionEvent ev){
		if(velocityTracker==null){
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(ev);
	}

	/**
	 * 获得瞬时速度
	 * @return
	 */
	private int getScrollVelocity() {
		velocityTracker.computeCurrentVelocity(1000);
		return (int) Math.abs(velocityTracker.getXVelocity());
	}

	/**
	 * 回收Tracker
	 */
	private void recycleVelocityTracker() {
		velocityTracker.recycle();
		velocityTracker = null;
	}

	/**
	 * 菜单打开时在整体上添加Listener使打开后主界面无法操作
	 */
	private OnClickListener thisOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (isMenuOpened) closeMenu();
		}
	};

	/**
	 * 以Animation结束触发的OnClickListener
	 */
	private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if(contentMask!=null){
				if(isMenuOpened){
					contentMask.setOnClickListener(thisOnClickListener);
				}else{
					contentMask.setOnClickListener(null);
				}
			}
			if(playlistTitle!=null){
				if(!isMenuOpened)
					playlistTitle.setVisibility(GONE);
			}
			if(playlistSwipeHint!=null){
				if(!isMenuOpened)
					playlistSwipeHint.setVisibility(VISIBLE);
			}
			if(playlistAlbum!=null){
				if(!isMenuOpened)
					playlistAlbum.setVisibility(GONE);
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	public void ignoreView(View view){
		//Todo:若要加入需要忽略的View编辑此方法(暂时没想到会用到的可能性) 创建View的List将需要忽略的View加入 事件发生时判断
		if(ignoredViews == null){
			ignoredViews = new ArrayList<View>();
		}
		ignoredViews.add(view);
	}

	private boolean isInIgnoredViews(MotionEvent event){
		Rect rect = new Rect();
		if(ignoredViews == null){
			return false;
		}
		for (View v : ignoredViews) {
			v.getGlobalVisibleRect(rect);
			if (rect.contains((int) event.getX(), (int) event.getY()))
				return true;
		}
		return false;
	}

	/**
	 * 设置遮罩元素
	 * @param contentMask
	 */
	public void setContentMask(View contentMask){
		this.contentMask = contentMask;
	}

	/**
	 * 设置背部菜单
	 * @param backgroundDim
	 */
	public void setBackgroundDim(View backgroundDim){
		this.backgroundDim = backgroundDim;
	}

	public boolean getMenuState(){
		return isMenuOpened;
	}

	public void setToggleSwitch(boolean toggleSwitch) {
		this.toggleSwitch = toggleSwitch;
	}

	public void setToolbarHeight(float toolbarHeight) {
		this.toolbarHeight = toolbarHeight;
	}

	public void setStatusBarHeight(float statusBarHeight) {
		this.statusBarHeight = statusBarHeight;
	}

	public void setBottomShadow(View bottomShadow) {
		this.bottomShadow = bottomShadow;
	}

	public void setPlaylistAlbum(View playlistAlbum) {
		this.playlistAlbum = playlistAlbum;
	}

	public void setPlaylistSwipeHint(View playlistSwipeHint) {
		this.playlistSwipeHint = playlistSwipeHint;
	}

	public void setPlaylistTitle(View playlistTitle) {
		this.playlistTitle = playlistTitle;
	}

	public void setStatusBarBackground(View statusBarBackground) {
		this.statusBarBackground = statusBarBackground;
	}
}
