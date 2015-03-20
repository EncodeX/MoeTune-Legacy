package util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.uexperience.moetune.R;
import moetune.core.MoeTuneMusic;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Encode_X on 14/10/18.
 * Project LabProject
 * Package neu.labproject.campus.adapters
 */
public class MusicListAdapter extends BaseAdapter {
	private final static int TYPE_SPACE = 0;
	private final static int TYPE_ITEM = 1;
	private final static int VIEW_TYPE_COUNT = 2;
	private Context context;
	private List<MusicListAdapterItem> musicListAdapterItems;
	private TreeSet spaceItemSet;

	public MusicListAdapter(Context context) {
		this.context = context;
		this.musicListAdapterItems = new ArrayList<MusicListAdapterItem>();
		this.spaceItemSet = new TreeSet();
	}

	@Override
	public int getCount() {
		return musicListAdapterItems.size();
	}

	@Override
	public Object getItem(int i) {
		return musicListAdapterItems.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		LayoutInflater inflater = LayoutInflater.from(context);
		MusicListAdapterItem item = musicListAdapterItems.get(i);

//		Log.v("Layout Debug","item view type: "+getItemViewType(i));

//		System.out.println("getView " + i + " " + view + " type = " + getItemViewType(i));

		switch(getItemViewType(i)){
			case TYPE_ITEM:
				ViewHolderItem viewHolderItem;
				if(view == null){
					view = inflater.inflate(R.layout.music_list_item,null);
					viewHolderItem = new ViewHolderItem();
					viewHolderItem.musicListIndex = (TextView)view.findViewById(R.id.music_list_index);
					viewHolderItem.musicListTitle = (TextView)view.findViewById(R.id.music_list_title);
					viewHolderItem.musicListDuration = (TextView)view.findViewById(R.id.music_list_duration);
					viewHolderItem.musicListPlayingIcon = (FrameLayout)view.findViewById(R.id.music_list_playing_icon);
					view.setTag(viewHolderItem);
				}else{
					viewHolderItem = (ViewHolderItem)view.getTag();
				}

				viewHolderItem.musicListTitle.setText(item.getTitle());
				viewHolderItem.musicListDuration.setText(item.getDuration());
				viewHolderItem.musicListIndex.setText(item.getIndex()+"");
				if(item.isPlaying()){
					viewHolderItem.musicListPlayingIcon.setVisibility(View.VISIBLE);
					viewHolderItem.musicListIndex.setVisibility(View.INVISIBLE);
				}else{
					viewHolderItem.musicListPlayingIcon.setVisibility(View.INVISIBLE);
					viewHolderItem.musicListIndex.setVisibility(View.VISIBLE);
				}
				break;
			case TYPE_SPACE:
				ViewHolderSpace viewHolderSpace;
				if(view == null){
					view = inflater.inflate(R.layout.music_list_item_space,null);
					viewHolderSpace = new ViewHolderSpace();
					view.setTag(viewHolderSpace);
				}else{
					viewHolderSpace = (ViewHolderSpace)view.getTag();
				}
				break;
		}

		return view;
	}

	@Override
	public int getItemViewType(int position) {
		return spaceItemSet.contains(position) ? TYPE_SPACE : TYPE_ITEM;
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	public void refreshList(ArrayList<MoeTuneMusic> musicList){
		musicListAdapterItems.clear();
		int i=1;
		addItem(new MusicListAdapterItem());
		for(MoeTuneMusic music:musicList){
			MusicListAdapterItem item = new MusicListAdapterItem(music.getSubTitle(),music.getStreamTime(),i);
			addItem(item);
			i++;
		}
	}

	private void addItem(MusicListAdapterItem item){
		musicListAdapterItems.add(item);
		switch (item.getType()){
			case MusicListAdapterItem.TYPE_ITEM:
				break;
			case MusicListAdapterItem.TYPE_SPACE:
				spaceItemSet.add(musicListAdapterItems.size()-1);
				break;
		}
		notifyDataSetChanged();
	}

	public void setPlayingIndex(int index){
		for(MusicListAdapterItem item:musicListAdapterItems){
			item.setPlaying(false);
			if(item.getIndex()==index+1){
				item.setPlaying(true);
			}
		}
		notifyDataSetChanged();
	}

	private static class ViewHolderItem {
		TextView musicListIndex;
		TextView musicListTitle;
		TextView musicListDuration;
		FrameLayout musicListPlayingIcon;
	}

	private static class ViewHolderSpace{

	}
}
