package moetune.moeTuneComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 14-6-21
 * Project: ${PROJECT_NAME}
 * Package: ${PACKAGE_NAME}
 */
public class RectListItemSpace extends FrameLayout{
	public RectListItemSpace(Context context){
		super(context);
	}
	public RectListItemSpace(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	public RectListItemSpace(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth()*9/16, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
