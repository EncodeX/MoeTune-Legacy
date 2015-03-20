package moetune.core;

import android.graphics.Bitmap;

/**
 * Created by Encode_X on 14/11/16.
 * Project MoeTune
 * Package moetune.core
 */
public interface OnMusicStateChangedListener {
	public void onMusicStateChanged();
	public void onMusicInfoChanged(String streamTime, String wikiTitle, String subTitle, String artist);
	public void onMusicPreparing();
	public void onMusicPrepared(int index);
	public void onCoverPrepared(Bitmap cover);
}
