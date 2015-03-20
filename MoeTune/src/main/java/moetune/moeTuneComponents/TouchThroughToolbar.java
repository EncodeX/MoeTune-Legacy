package moetune.moeTuneComponents;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 15/1/5
 * Project: MoeTune
 * Package: moetune.moeTuneComponents
 */
public class TouchThroughToolbar extends Toolbar{
	private float viewWidth;
	private float viewHeight;
	private boolean isInitialized = false;

	public TouchThroughToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(!isInitialized){initView();}

		float currentX = ev.getX();
		float currentY = ev.getY();

		if(currentX > viewHeight || currentY > viewHeight){
//			Log.v("Action Debug","Toolbar Touch Through!!!");
			return false;
		}

		return super.dispatchTouchEvent(ev);
	}

	private void initView(){
		viewWidth = Double.valueOf(this.getWidth()).floatValue();
		viewHeight = Double.valueOf(this.getHeight()).floatValue();
		isInitialized = true;
	}
}
