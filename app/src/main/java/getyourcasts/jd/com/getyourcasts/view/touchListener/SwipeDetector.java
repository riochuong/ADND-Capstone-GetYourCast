package getyourcasts.jd.com.getyourcasts.view.touchListener;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chuondao on 9/11/17.
 */

public abstract class SwipeDetector implements View.OnTouchListener {

    private Float downX  = 0.0F;
    private Float downY  = 0.0F;
    private Float upX  = 0.0F;
    private Float upY  = 0.0F;
    private static final int MIN_DISTANCE = 50;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event != null){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN :
                    downX = event.getX();
                    downY = event.getY();
                            break;

                case MotionEvent.ACTION_UP:
                    upX = event.getX();
                    upY = event.getY();

                    float deltaX = upX - downX;
                    float absX = Math.abs(deltaX);
                    float deltaY = upY - downY;
                    float absY = Math.abs(deltaY);

                    // check gesture
                    boolean isSwipeLeftToRight =  ( deltaX > 0) && (absX >= MIN_DISTANCE);
                    boolean isSwipeRightToLeft = (deltaX < 0 )  && (absX >= MIN_DISTANCE);
                    boolean isSwipeUp = (deltaY < 0) && (absY >= MIN_DISTANCE);
                    boolean isSwipeDown = (deltaY > 0) && (absY >= MIN_DISTANCE);

                    // now detect gesture
                    if (isSwipeLeftToRight && !isSwipeDown && !isSwipeUp){
                        return onSwipeLeftToRight();
                    }

                    if (isSwipeRightToLeft && !isSwipeDown && !isSwipeUp){
                        return onSwipeRightToLeft();
                    }

                    if (isSwipeDown && !isSwipeLeftToRight && !isSwipeRightToLeft){
                        return onSwipeDownward();
                    }

                    if (isSwipeUp && !isSwipeLeftToRight && !isSwipeRightToLeft){
                        return onSwipeUpward();
                    }
                    break;


            }
        }
        return false;
    }

    public abstract boolean onSwipeRightToLeft();

    public abstract boolean onSwipeLeftToRight();

    public abstract boolean onSwipeUpward();

    public abstract boolean onSwipeDownward();
}
