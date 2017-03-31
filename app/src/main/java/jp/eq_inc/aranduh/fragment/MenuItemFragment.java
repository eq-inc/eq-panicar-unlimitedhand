package jp.eq_inc.aranduh.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.eq_inc.aranduh.AROnCameraActivity;
import jp.eq_inc.aranduh.AROnCameraWithGLActivity;
import jp.eq_inc.aranduh.AROnMapActivity;
import jp.eq_inc.aranduh.R;

public class MenuItemFragment extends Fragment implements View.OnClickListener {

    private static final int VIEW_TAG_MENU_ITEM_TYPE = "VIEW_TAG_MENU_ITEM_TYPE".hashCode();
    private OnListFragmentInteractionListener mListener;

    public enum MenuItemType {
        ARonCamera("AR on camera", AROnCameraActivity.class),
        ARonMap("AR on map", AROnMapActivity.class),
        ARonCameraWithGL("AR with OpenGL on camera", AROnCameraWithGLActivity.class),
        ;

        String title;
        Class activityClass;

        MenuItemType(String title, Class activityClass) {
            this.title = title;
            this.activityClass = activityClass;
        }

        String getTitle() {
            return this.title;
        }

        Class getActivityClass() {
            return this.activityClass;
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MenuItemFragment() {
    }

    @SuppressWarnings("unused")
    public static MenuItemFragment newInstance() {
        MenuItemFragment fragment = new MenuItemFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menuitem_list, container, false);
        LinearLayout menuItemList = (LinearLayout) view.findViewById(R.id.llMenuItemList);

        for (MenuItemType menuItemType : MenuItemType.values()) {
            View itemView = inflater.inflate(R.layout.fragment_menuitem, menuItemList, false);
            ((TextView) itemView.findViewById(R.id.tvMenuItemTitle)).setText(menuItemType.getTitle());
            itemView.setTag(VIEW_TAG_MENU_ITEM_TYPE, menuItemType);
            itemView.setOnClickListener(this);
            menuItemList.addView(itemView);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        MenuItemType menuItemType = (MenuItemType) v.getTag(VIEW_TAG_MENU_ITEM_TYPE);
        if (mListener != null && menuItemType != null) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), menuItemType.getActivityClass());
            mListener.onListFragmentInteraction(intent);
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Intent intent);
    }
}
