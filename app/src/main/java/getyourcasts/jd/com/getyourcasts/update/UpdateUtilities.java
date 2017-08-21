package getyourcasts.jd.com.getyourcasts.update;

import android.content.Context;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import io.reactivex.annotations.NonNull;

/**
 * Created by chuondao on 8/16/17.
 */

public class UpdateUtilities  {

    private static boolean initialized = false;

    private static final String UPDATE_JOB_TAG = "UPDATE_PODCAST_EPS";

    private static final int REMINDER_INTERVALS = 5;//12*60*60; // run every 12 hours
    private static final int REMINDER_FLEX_TIME = 5*2;//60*60; // run every 12 hours


    public synchronized static void scheduleUpdateTask (@NonNull  Context context) {
        if (initialized) return;
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        // create job ready for scheudle
        Job updateJob = dispatcher.newJobBuilder()
                        .setService(UpdateJobService.class)
                        .setTag(UPDATE_JOB_TAG)
                        .setConstraints(Constraint.ON_ANY_NETWORK)
                        .setLifetime(Lifetime.FOREVER)
                        .setRecurring(true)
                        .setTrigger(Trigger.executionWindow(
                                REMINDER_INTERVALS,
                                REMINDER_INTERVALS + REMINDER_FLEX_TIME
                        ))
                        .setReplaceCurrent(true)
                        .build();

        dispatcher.schedule(updateJob);
        initialized = true;
    }
}
