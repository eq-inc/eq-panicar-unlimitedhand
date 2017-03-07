package jp.eq_inc.aranduh;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import jp.eq_inc.aranduh.fragment.MenuItemFragment;

public class TopActivity extends AppCompatActivity implements MenuItemFragment.OnListFragmentInteractionListener {
    private static final String TAG = TopActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag(MenuItemFragment.class.getName());
        if (fragment == null) {
            fragment = MenuItemFragment.newInstance();
            manager.beginTransaction().add(R.id.flFragment, fragment, MenuItemFragment.class.getName()).commitAllowingStateLoss();
        }
    }

    @Override
    public void onListFragmentInteraction(Intent intent) {
        startActivity(intent);
    }
}
