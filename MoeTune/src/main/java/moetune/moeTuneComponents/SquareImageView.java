package moetune.moeTuneComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by Encode_X on 14-6-21.
 */
public class SquareImageView extends ImageView{
	public SquareImageView(Context context){
		super(context);
	}
	public SquareImageView(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	public SquareImageView(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
	}

	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
		setMeasuredDimension(getDefaultSize(0,widthMeasureSpec),getDefaultSize(0,heightMeasureSpec));
		int childWidthSize = getMeasuredWidth();
		int childHeightSize = getMeasuredHeight();
		if(childHeightSize>childWidthSize){
			heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY);
		}else{
			widthMeasureSpec = heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize,MeasureSpec.EXACTLY);
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/*
	 *  1. widthMeasureSpec和heightMeasureSpec这两个值是android:layout_width="200dp" android:layout_height="80dp"
	 *  来定义的，它由两部分构成，可通过int specModeHeight = MeasureSpec.getMode(heightMeasureSpec);
	 *  int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec)来得到各自的值。
		如果android:layout_width="wrap_content"或android:layout_width="fill_parent"，
		哪么得到的specMode为MeasureSpec.AT_MOST，如果为精确的值则为MeasureSpec.EXACTLY。

		另外，specSize要想得到合适的值需要在AndroidManifest.xml中添加<uses-sdk android:minSdkVersion="10" />
		 2.系统默认的onMeasure调用方法是getDefaultSize来实现，有时候在自定义控件的时候多数采用
		 3.MeasureSpec封装了父布局传递给子布局的布局要求，
		 每个MeasureSpec代表了一组宽度和高度的要求。一个MeasureSpec由大小和模式组成。
		 它有三种模式：UNSPECIFIED(未指定),父元素部队自元素施加任何束缚，子元素可以得到任意想要的大小；
		 EXACTLY(完全)，父元素决定自元素的确切大小，子元素将被限定在给定的边界里而忽略它本身大小；
		 AT_MOST(至多)，子元素至多达到指定大小的值。
	 */
}
