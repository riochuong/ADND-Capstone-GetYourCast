package getyourcasts.jd.com.getyourcasts.view.touchListener

import android.view.MotionEvent
import android.view.View

/**
 * Created by chuondao on 7/29/17.
 */
abstract class SwipeDetector: View.OnTouchListener {

    private var downX: Float = 0.0F
    private var downY: Float = 0.0F
    private var upX: Float = 0.0F
    private var upY: Float = 0.0F


    companion object {
        val MIN_DISTANCE = 50
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null){
            when (event.action){
                MotionEvent.ACTION_DOWN ->{
                        downX = event.x
                        downY = event.y
                }

                MotionEvent.ACTION_UP -> {
                    upX = event.x
                    upY = event.y

                    val deltaX = upX - downX
                    val absX = Math.abs(deltaX)
                    val deltaY = upY - downY
                    val absY = Math.abs(deltaY)

                    // check gesture
                    val isSwipeLeftToRight =  ( deltaX > 0) && (absX >= MIN_DISTANCE)
                    val isSwipeRightToLeft = (deltaX < 0 )  && (absX >= MIN_DISTANCE)
                    val isSwipeUp = (deltaY < 0) && (absY >= MIN_DISTANCE)
                    val isSwipeDown = (deltaY > 0) && (absY >= MIN_DISTANCE)

                    // now detect gesture
                    if (isSwipeLeftToRight && !isSwipeDown && !isSwipeUp){
                        return onSwipeLeftToRight()
                    }

                    if (isSwipeRightToLeft && !isSwipeDown && !isSwipeUp){
                       return onSwipeRightToLeft()
                    }

                    if (isSwipeDown && !isSwipeLeftToRight && !isSwipeRightToLeft){
                        return onSwipeDownward()
                    }

                    if (isSwipeUp && !isSwipeLeftToRight && !isSwipeRightToLeft){
                        return onSwipeUpward()
                    }

                }
            }
        }
        return false
    }

    abstract fun onSwipeRightToLeft() : Boolean

    abstract fun onSwipeLeftToRight()  : Boolean

    abstract fun onSwipeUpward()      : Boolean

    abstract fun onSwipeDownward()   : Boolean

}