package moetune.fragments;

/**
 * Created with IntelliJ IDEA.
 * Author: Enex Tapper
 * Date: 14-6-19
 * Project: ${PROJECT_NAME}
 * Package: ${PACKAGE_NAME}
 */

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.uexperience.moetune.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentHandler extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	private FragmentHandler[] fragments = new FragmentHandler[3];
	private ViewHandler viewHandler = new ViewHandler();

	public FragmentHandler[] getFragments(){
		return this.fragments;
	}

	public void setFragments(FragmentHandler[] fragments){
		this.fragments = fragments;
	}


	/**
	 * Returns a new instance of this fragment for the given section
	 * number.
	 */
	public static FragmentHandler newInstance(int sectionNumber) {
		FragmentHandler fragment = new FragmentHandler();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public FragmentHandler() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

	        /*在这里创建修改每个Section的View 返回为rootView*/
	        /*创建不同的类，来实现不同的Section*/

		////Log.v("Test Message",Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

		View rootView=null;TextView textView;

		switch (getArguments().getInt(ARG_SECTION_NUMBER)){
			case 0:
				rootView = viewHandler.getNowPlayingView(inflater, container);
				break;
			case 1:
				rootView = viewHandler.getAboutView(inflater,container);
				break;
			case 2:
				rootView = inflater.inflate(R.layout.fragment_temp, container, false);
				textView = (TextView) rootView.findViewById(R.id.section_label);
				textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
				break;
		}

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
//		((MainActivity) activity).onSectionAttached(
//				getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public void initFragments(){
		for(int i=0;i<fragments.length;i++){
			fragments[i]=newInstance(i);
		}
	}

	public View getNowPlayingView(){
		return viewHandler.getNowPlayingView();
	}
}
