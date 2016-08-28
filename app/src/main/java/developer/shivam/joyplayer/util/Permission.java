package developer.shivam.joyplayer.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permission extends Activity {

    private Permission mPermission = null;
    private Activity mActivity;
    private final int READ_EXTERNAL_CARD_REQUEST = 100;
    private onPermissionListener mPermissionListener;

    Permission(Activity activity) {
        mActivity = activity;
    }

    void setSingletonInstance (Permission permission) {
        mPermission = permission;
    }

    public void getPermission(String permission) {
        if (ContextCompat.checkSelfPermission(mActivity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{permission},
                    READ_EXTERNAL_CARD_REQUEST);
        } else {
            mPermissionListener.onPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        checkPermission(requestCode, grantResults);
    }

    private void checkPermission(int requestCode, int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_CARD_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mPermissionListener.onPermissionGranted();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    mPermissionListener.onPermissionDenied();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public Permission setPermissionListener(onPermissionListener listener) {
        mPermission.mPermissionListener = listener;
        return mPermission;
    }
}
