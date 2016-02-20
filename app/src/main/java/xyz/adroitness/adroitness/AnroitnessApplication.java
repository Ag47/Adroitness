package xyz.adroitness.adroitness;

import android.app.Application;

import com.estimote.sdk.EstimoteSDK;

/**
 * Main {@link Application} object for Demos. It configures EstimoteSDK.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class AnroitnessApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initializes Estimote SDK with your App ID and App Token from Estimote Cloud.
        // You can find your App ID and App Token in the
        // Apps section of the Estimote Cloud (http://cloud.estimote.com).
        EstimoteSDK.initialize(this, "testtheoska1", "f693aba264212a7e603a855ece5aae2e");

        // Configure verbose debug logging.
        EstimoteSDK.enableDebugLogging(true);
    }
}
