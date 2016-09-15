package developer.shivam.joyplayer.util;

import android.app.Activity;

public class PermissionManager {

    public static Permission with(Activity activity) {
        Permission permission = new Permission(activity);
        permission.setSingletonInstance(permission);
        return permission;
    }
}
