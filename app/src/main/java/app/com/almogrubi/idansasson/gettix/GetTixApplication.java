package app.com.almogrubi.idansasson.gettix;

import android.app.Application;

import com.cloudinary.android.MediaManager;

/**
 * Created by idans on 12/11/2017.
 */

public class GetTixApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initiating Cloudinary MediaManager (for loading responsive images)
        MediaManager.init(this);
    }
}
