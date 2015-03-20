package moetune.fragments;


import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Color;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.uexperience.moetune.R;
import moetune.core.MoeTuneConstants;
import moetune.core.OnLoginEventCalledListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
	private RelativeLayout mDrawerMainLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
	private Button userInfoButton;
	private TextView userName;

	private SimpleAdapter mListAdapter;
	private String[] mListTitle;
	private int[] mListImage = new int[]{
            R.drawable.drawer_icon_play,
            R.drawable.drawer_icon_about,
            R.drawable.drawer_icon_exit
    };

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

	private OnLoginEventCalledListener onLoginEventCalledListener;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);


        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.

        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

	    mDrawerMainLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_navigation_drawer,container,false);
	    mDrawerListView = (ListView) mDrawerMainLayout.findViewById(R.id.menu_list);
	    userInfoButton = (Button)mDrawerMainLayout.findViewById(R.id.user_info_button);
	    userName = (TextView)mDrawerMainLayout.findViewById(R.id.user_name);

		final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MoeTuneConstants.Config.PREFERENCE_NAME,0);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mListTitle = new String[]{
                inflater.getContext().getResources().getString(R.string.title_section1),
                inflater.getContext().getResources().getString(R.string.title_section2),
                inflater.getContext().getResources().getString(R.string.title_section3)
        };
	    List<Map<String,Object>> listItems = new ArrayList<Map<String,Object>>();
	    for(int i = 0;i< mListTitle.length;i++){
		    Map<String,Object> map = new HashMap<String, Object>();
		    map.put("image", mListImage[i]);
		    map.put("title", mListTitle[i]);
		    listItems.add(map);
	    }

	    mListAdapter = new SimpleAdapter(getActivity(),listItems,
			    R.layout.drawer_list_item,new String[]{"title","image"},new int[]{R.id.title,R.id.image});

	    mDrawerListView.setAdapter(mListAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

	    userInfoButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    //Log.v("Config Debug", "检测登录绑定以确定单击事件方向。");
				if(sharedPreferences.getBoolean(MoeTuneConstants.Config.IS_MEMBER_REGISTERED,false)){
					//Todo:跳到个人信息Activity
				}else{
					onLoginEventCalledListener.onLoginEventCalled();
				}
			    if (mDrawerLayout != null) {
				    mDrawerLayout.closeDrawer(mFragmentContainerView);
			    }
		    }
	    });

	    checkLoginState();

        return mDrawerMainLayout;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
//        mDrawerToggle = new ActionBarDrawerToggle(
//                getActivity(),                    /* host Activity */
//                mDrawerLayout,                    /* DrawerLayout object */
//                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
//                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
//                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
//        ) {
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                super.onDrawerClosed(drawerView);
//                if (!isAdded()) {
//                    return;
//                }
//                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
//            }
//
//	        @Override
//	        public void onDrawerSlide(View drawerView,float offSet){
//		        super.onDrawerSlide(drawerView,offSet);
//		        if(!isAdded()){
//			        return;
//		        }
//	        }
//
//
//	        @Override
//	        public void onDrawerStateChanged(int newState){
//		        super.onDrawerStateChanged(newState);
//		        if(!isAdded()){
//			        return;
//		        }
//	        }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//                if (!isAdded()) {
//                    return;
//                }
//
//                if (!mUserLearnedDrawer) {
//                    // The user manually opened the drawer; store this flag to prevent auto-showing
//                    // the navigation drawer automatically in the future.
//                    mUserLearnedDrawer = true;
//                    SharedPreferences sp = PreferenceManager
//                            .getDefaultSharedPreferences(getActivity());
//                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
//                }
//
//                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
//            }
//        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
	    // 此处教学方式换成简单图片解释 只出现一次
//        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
//            mDrawerLayout.openDrawer(mFragmentContainerView);
//        }

        // Defer code dependent on restoration of previous instance state.
//        mDrawerLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                mDrawerToggle.syncState();
//            }
//        });

	    mDrawerLayout.setScrimColor(Color.parseColor("#33000000"));

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

	public void setOnLoginEventCalledListener(OnLoginEventCalledListener onLoginEventCalledListener){
		this.onLoginEventCalledListener = onLoginEventCalledListener;
	}

	public void checkLoginState(){
		//Log.v("Config Debug", "检查登录绑定以刷新Navigation Drawer状态。");
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MoeTuneConstants.Config.PREFERENCE_NAME,0);
		if(sharedPreferences.getBoolean(MoeTuneConstants.Config.IS_MEMBER_REGISTERED,false) &&
                sharedPreferences.getBoolean(MoeTuneConstants.Config.IS_WIFI_CONNECTED,false)){
			userName.setText("已登录");
			//Log.v("Config Debug", "检测为已登录。");
		}else{
			userName.setText("点此登录");
			//Log.v("Config Debug", "检测为未登录");
		}
	}

    public DrawerLayout getDrawer(){
        return this.mDrawerLayout;
    }

    public View getFragmentContainer(){
        return this.mFragmentContainerView;
    }
}
