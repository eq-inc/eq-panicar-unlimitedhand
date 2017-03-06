package jp.eq_inc.aranduh;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import jp.co.thcomp.unlimitedhand.UhAccessHelper;
import jp.co.thcomp.util.RuntimePermissionUtil;
import jp.co.thcomp.util.ToastUtil;
import jp.eq_inc.aranduh.fragment.MainArFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RuntimePermissionUtil.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA},
                mRuntimePermissionResultListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private RuntimePermissionUtil.OnRequestPermissionsResultListener mRuntimePermissionResultListener = new RuntimePermissionUtil.OnRequestPermissionsResultListener() {
        @Override
        public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
            boolean enableLaunch = false;

            if (grantResults != null && grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        enableLaunch = true;
                    } else {
                        enableLaunch = false;
                        break;
                    }
                }
            }

            if (enableLaunch) {
                FragmentManager manager = getFragmentManager();
                if (manager.findFragmentByTag(null) == null) {
                    manager.beginTransaction()
                            .add(R.id.container, new MainArFragment())
                            .commit();
                }
            } else {
                ToastUtil.showToast(MainActivity.this, "cannot launch by not allowed permissions", Toast.LENGTH_LONG);
                finish();
            }
        }
    };
}
