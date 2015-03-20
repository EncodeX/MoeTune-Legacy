package moetune.moeTuneComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.uexperience.moetune.R;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 15/1/6
 * Project: MoeTune
 * Package: moetune.moeTuneComponents
 */
public class MoeTuneMusicListItem extends RelativeLayout {
	private TextView musicListIndex;
	private TextView musicListTitle;
	private TextView musicListDuration;

	public MoeTuneMusicListItem(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater.from(context).inflate(R.layout.moetune_music_list_item, this, true);
		musicListIndex = (TextView)this.findViewById(R.id.music_list_index);
		musicListTitle = (TextView)this.findViewById(R.id.music_list_title);
		musicListDuration = (TextView)this.findViewById(R.id.music_list_duration);
	}

	public void setIndex(int index){
		musicListIndex.setText(index+"");
	}
}
