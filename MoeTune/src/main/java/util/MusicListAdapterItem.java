package util;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 15/1/6
 * Project: MoeTune
 * Package: util
 */
public class MusicListAdapterItem {
	public final static int TYPE_SPACE = 0;
	public final static int TYPE_ITEM = 1;

	private int type;
	private String title;
	private String duration;
	private int index;
	private boolean isPlaying;

	public MusicListAdapterItem(String title, String duration, int index) {
		this.type = TYPE_ITEM;
		this.duration = duration;
		this.title = title;
		this.index = index;
		this.isPlaying = false;
	}

	public MusicListAdapterItem(){
		this.type = TYPE_SPACE;
	}

	public String getDuration() {
		return duration;
	}

	public String getTitle() {
		return title;
	}

	public int getIndex() {
		return index;
	}

	public int getType() {
		return type;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
}
