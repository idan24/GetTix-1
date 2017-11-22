package app.com.almogrubi.idansasson.gettix;

import android.app.Application;

import com.cloudinary.android.MediaManager;

public class GetTixApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initiating Cloudinary MediaManager (for loading responsive images)
        MediaManager.init(this);
    }
}
