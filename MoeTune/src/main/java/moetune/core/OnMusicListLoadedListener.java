package moetune.core;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Encode_X on 14-10-1.
 */
public interface OnMusicListLoadedListener {
	public void onMusicListLoaded(ArrayList<MoeTuneMusic> musicList) throws IOException;
}