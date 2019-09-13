package uk.co.diegobarle.longclicklistanimation

import android.animation.Animator
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.pow
import kotlin.math.sqrt

class ActionLongClickListener(activity: Activity, actionImageRes: Int, private var accentColor: Int, private val callback: ActionLongClickCallback?): RecyclerView.OnItemTouchListener{
    private val activityRoot = activity.window.decorView.rootView as ViewGroup
    private val rootViewToDim = activityRoot.getChildAt(0)

    private var handler = Handler()
    private var currentAction = 0
    private var currentShareId = 0L

    private var selectedView: View? = null
    private var selectedPosition = -1
    private var viewToKeep: View? = null

    private var actionView = ImageView(activity)
    private var loaderView = View(activity)

    private var initialXTouch = -1f
    private var initialYTouch = -1f

    private val MIN_DELTA_TOUCH = activity.resources.getDimensionPixelSize(R.dimen.min_action_delta_touch)

    init {

        loaderView.setBackgroundColor(accentColor)
        loaderView.setBackgroundColor(accentColor)

        val actionViewSize = activity.resources.getDimensionPixelSize(R.dimen.tap_share_image_size)
        val actionButtonPadding = activity.resources.getDimensionPixelSize(R.dimen.small_button_padding)
        actionView.layoutParams = ViewGroup.LayoutParams(actionViewSize, actionViewSize)
        actionView.setBackgroundResource(R.drawable.share_round_background)
        actionView.setImageResource(actionImageRes)
        actionView.setPadding(actionButtonPadding, actionButtonPadding, actionButtonPadding, actionButtonPadding)

    }

    companion object {
        private const val SHARE_WAIT_TIME = 200L
        private const val FADE_ANIMATION_DURATION = 200L
        private const val SHARE_ANIMATION_DURATION = 1000L

        private const val DIMMED_ALPHA = 0.2f
        private const val FULL_ALPHA = 1f

        private const val CANCELLED_BY_MOVE = -1
        private const val READY = 0
        private const val VIEW_SELECTED = 1
        private const val SHARE_ANIMATION = 2
        private const val SHARE = 3

        const val BOUNDARY_MATCH_ORIGINAL = -1

        private fun shareStarted(action: Int): Boolean{
            return action == SHARE_ANIMATION || action == SHARE
        }
    }

    private val highlightItemRunnable = Runnable {
        if(currentAction != VIEW_SELECTED){
            return@Runnable
        }
        val shareId = System.currentTimeMillis()
        currentShareId = shareId
        currentAction = SHARE_ANIMATION
        viewToKeep = createImageViewFromView(selectedView)

        initShareItemsViews(selectedView)

        activityRoot.addView(viewToKeep)
        activityRoot.addView(loaderView)
        activityRoot.addView(actionView)

        animateShare()

        handler.postDelayed({
            if(currentAction == SHARE_ANIMATION && shareId == currentShareId){
                currentAction = SHARE
                callback?.onAction(selectedPosition)
                selectedView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                animateToOriginalState(false)
            }
        }, SHARE_ANIMATION_DURATION)
    }

    private fun createImageViewFromView(view: View?): ImageView{
        val imageView = ImageView(activityRoot.context)
        val highlightedViewRect = getViewRect(callback?.getViewBoundary(selectedPosition, selectedView), callback?.getHighlightedViewBoundaryPadding(selectedPosition, selectedView), view)
        if(highlightedViewRect == null) {
            imageView.layoutParams = ViewGroup.LayoutParams(
                view?.width ?: ViewGroup.LayoutParams.WRAP_CONTENT,
                view?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT)
        }else{
            imageView.layoutParams = ViewGroup.LayoutParams(
                highlightedViewRect.width(),
                highlightedViewRect.height())
        }

        val completeViewBitmap = view?.takeScreenshot()
        imageView.setImageBitmap(
            if(highlightedViewRect == null || completeViewBitmap == null)
                completeViewBitmap
            else
                Bitmap.createBitmap(
                    completeViewBitmap,
                    highlightedViewRect.left,
                    highlightedViewRect.top,
                    highlightedViewRect.width(),
                    highlightedViewRect.height())
        )

        val selectedViewPosition = intArrayOf(0, 0)
        view?.getLocationOnScreen(selectedViewPosition)
        imageView.x = selectedViewPosition[0].plus(highlightedViewRect?.left?:0).toFloat()
        imageView.y = selectedViewPosition[1].plus(highlightedViewRect?.top?:0).toFloat()
        return imageView
    }

    private fun getViewRect(viewBoundary: Rect?, viewBoundaryPadding: Rect?, view: View?): Rect?{
        if(view == null){
            return null
        }
        if(viewBoundary == null){
            return if(viewBoundaryPadding == null) {
                Rect(0, 0, view.width, view.height)
            }else{
                Rect(0.plus(viewBoundaryPadding.left), 0.plus(viewBoundaryPadding.top), view.width.minus(viewBoundaryPadding.right), view.height.minus(viewBoundaryPadding.bottom))
            }
        }
        val left = if(viewBoundary.left == BOUNDARY_MATCH_ORIGINAL) 0 else viewBoundary.left
        val top = if(viewBoundary.top == BOUNDARY_MATCH_ORIGINAL) 0 else viewBoundary.top
        val rect = Rect(
            left,
            top,
            if(viewBoundary.right == BOUNDARY_MATCH_ORIGINAL) view.width else viewBoundary.right,
            if(viewBoundary.bottom == BOUNDARY_MATCH_ORIGINAL) view.height else viewBoundary.bottom)

        if(rect.width() > view.width){
            rect.right = view.width.minus(left)
        }
        if(rect.height() > view.height){
            rect.bottom = view.height.minus(top)
        }

        if(viewBoundaryPadding != null){
            rect.left = rect.left.minus(viewBoundaryPadding.left)
            rect.top = rect.top.minus(viewBoundaryPadding.top)
            rect.right = rect.right.minus(viewBoundaryPadding.right)
            rect.bottom = rect.bottom.minus(viewBoundaryPadding.bottom)
        }

        return rect
    }

    private fun initShareItemsViews(view: View?){
        val viewRectWithoutPadding = callback?.getViewBoundary(selectedPosition, selectedView)
        val viewRect = getViewRect(viewRectWithoutPadding, callback?.getViewBoundaryPadding(selectedPosition, selectedView), view)
        val highlightedViewRect = getViewRect(viewRectWithoutPadding, callback?.getHighlightedViewBoundaryPadding(selectedPosition, selectedView), view)

        val viewWidth = viewRect?.width()?:view?.width?: activityRoot.width
        val highlightedViewHeight = highlightedViewRect?.height()?:view?.height?:0
        val highlightedBottomPosition = highlightedViewRect?.bottom?:0
        val buttonCenterPosition = highlightedBottomPosition.minus(highlightedViewHeight.div(2))

        val loaderBottomPosition = viewRect?.bottom?:0

        val selectedViewPosition = intArrayOf(0, 0)
        view?.getLocationOnScreen(selectedViewPosition)
        val shareLoaderHeight = view?.resources?.getDimensionPixelSize(R.dimen.tap_share_loader_height)?:0
        val shareItemX = 0f.minus(viewWidth)
        loaderView.layoutParams = ViewGroup.LayoutParams(viewWidth, shareLoaderHeight)
        loaderView.x = shareItemX
        loaderView.y = (selectedViewPosition[1].plus(loaderBottomPosition)).minus(shareLoaderHeight).toFloat()
        loaderView.setBackgroundColor(accentColor)
        loaderView.alpha = 1f

        val shareImageHeight = view?.resources?.getDimensionPixelSize(R.dimen.tap_share_image_size)?:0
        actionView.x = shareItemX
        actionView.y = (selectedViewPosition[1].plus(buttonCenterPosition)).minus(shareImageHeight.div(2)).toFloat()
    }

    private fun animateShare(){
        animateAlphaView(rootViewToDim, DIMMED_ALPHA, FADE_ANIMATION_DURATION, null)

        val shareImageLeftMargin = actionView.resources.getDimensionPixelSize(R.dimen.margin_medium).toFloat()
        animateTranslationView(loaderView, 0f, SHARE_ANIMATION_DURATION, null)
        animateTranslationView(actionView, shareImageLeftMargin, SHARE_ANIMATION_DURATION, null)
        animateRotationView(actionView, -1440f, SHARE_ANIMATION_DURATION, null)
    }

    private fun animateToOriginalState(cancelled: Boolean){
        val viewToRemove = viewToKeep
        val onEnd = Runnable{
            activityRoot.removeView(viewToRemove)
            activityRoot.removeView(loaderView)
            activityRoot.removeView(actionView)
        }
        if(!cancelled || currentAction == CANCELLED_BY_MOVE){
            onEnd.run()
            animateAlphaView(rootViewToDim, FULL_ALPHA, FADE_ANIMATION_DURATION, null)
        }else{
            animateAlphaView(rootViewToDim, FULL_ALPHA, FADE_ANIMATION_DURATION, onEnd)

            val toX = 0f.minus(loaderView.width)
            animateAlphaView(loaderView, 0f, FADE_ANIMATION_DURATION, null)
            animateTranslationView(loaderView, toX, FADE_ANIMATION_DURATION, null)
            animateTranslationView(actionView, toX, FADE_ANIMATION_DURATION, null)
            animateRotationView(actionView, 0f, SHARE_ANIMATION_DURATION, null)
        }
    }

    private fun animateAlphaView(view: View?, endAlpha: Float, duration: Long, onEnd: Runnable?) {
        view?.animate()?.setDuration(duration)?.alpha(endAlpha)?.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                view.alpha = endAlpha
                onEnd?.run()
            }

            override fun onAnimationCancel(animation: Animator?) {
                view.alpha = endAlpha
                onEnd?.run()
            }

            override fun onAnimationStart(animation: Animator?) { }

        })?.start()
    }

    private fun animateTranslationView(view: View?, endX: Float, duration: Long, onEnd: Runnable?) {
        view?.animate()?.
            setDuration(duration)?.
            translationX(endX)?.
            setInterpolator(DecelerateInterpolator(1.5f))?.
            setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    view.x = endX
                    onEnd?.run()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    view.x = endX
                    onEnd?.run()
                }

                override fun onAnimationStart(animation: Animator?) { }

            })?.start()
    }

    private fun animateRotationView(view: View?, endDegree: Float, duration: Long, onEnd: Runnable?) {
        view?.animate()?.
            setDuration(duration)?.
            rotation(endDegree)?.
            setInterpolator(DecelerateInterpolator(1.5f))?.
            setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    view.rotation = endDegree
                    onEnd?.run()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    view.rotation = endDegree
                    onEnd?.run()
                }

                override fun onAnimationStart(animation: Animator?) { }

            })?.start()
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) { }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        var intercepted = false
        when(e.action){
            MotionEvent.ACTION_DOWN -> {
                handler.removeCallbacks(null)
                currentAction = VIEW_SELECTED
                selectedView = rv.findChildViewUnder(e.x, e.y)
                selectedView?.let { selectedPosition = rv.getChildLayoutPosition(it) }

                val boundariesToHighlight = getViewRect(callback?.getViewBoundary(selectedPosition, selectedView), callback?.getHighlightedViewBoundaryPadding(selectedPosition, selectedView), selectedView)
                boundariesToHighlight?.offset(selectedView?.x?.toInt()?:0, selectedView?.y?.toInt()?:0)

                if(callback?.isItemActionable(selectedPosition) == true && boundariesToHighlight?.contains(e.x.toInt(), e.y.toInt()) == true){
                    handler.postDelayed(highlightItemRunnable, SHARE_WAIT_TIME)
                    initialXTouch = e.x
                    initialYTouch = e.y
                }
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                if(callback?.isItemActionable(selectedPosition) == false){
                    return intercepted
                }

                val distance = sqrt(e.x.minus(initialXTouch).toDouble().pow(2.0)
                        .plus(e.y.minus(initialYTouch).toDouble().pow(2.0)))

                if(isFinishAction(e.action) || distance > MIN_DELTA_TOUCH) {
                    handler.removeCallbacks(null)
                    animateToOriginalState(true)
                    intercepted = isFinishAction(e.action) && (currentAction == CANCELLED_BY_MOVE || shareStarted(currentAction))
                    currentAction =
                        when {
                            isFinishAction(e.action) -> READY
                            shareStarted(currentAction) -> CANCELLED_BY_MOVE
                            currentAction == CANCELLED_BY_MOVE -> CANCELLED_BY_MOVE
                            else -> READY
                        }
                }
            }
        }
        return intercepted
    }

    private fun isFinishAction(motionEventAction: Int): Boolean{
        return  motionEventAction == MotionEvent.ACTION_CANCEL
                || motionEventAction == MotionEvent.ACTION_UP
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) { }

    interface ActionLongClickCallback{
        fun onAction(selectedPosition: Int)
        fun isItemActionable(selectedPosition: Int): Boolean

        //Defines the part of the view selected from the recycler view we want to use for the gesture
        //If null, the view boundary matches the selected view from the recycler view.
        // ╔══════════════╗
        // ║ ┌─────────┐  ║--> view selected from the recycler view
        // ║ │ Θ       │--║--> part of the view to be used for the gesture
        // ║ │         │  ║    Θ -> where the action image is going to be shown
        // ║ │°°°°°°°° │--║--> where the loader is going to be shown
        // ║ └─────────┘  ║
        // ╚══════════════╝
        fun getViewBoundary(selectedPosition: Int, selectedView: View?): Rect?

        //Padding to the view that should be included for the gesture. Used to exclude/include
        //fragments of the view selected from the recycler view.
        //If null, the view to be part of the gesture matches the getViewBoundary()
        // ┌──────────────┐
        // │  ..........  │--> view selected from getViewBoundary()
        // │  : Θ      :--│--> part of the view to be used for the gesture after applying padding
        // │  ..........  │    Θ -> where the action image is going to be shown
        // │  °°°°°°° ----│-----> where the loader is going to be shown - the padding doesnt affect its position
        // └──────────────┘
        fun getViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect?

        //Padding to the view being highlighted.
        // If values in Rect are positive, the highlighted section to be taken from the view will be smaller.
        // If values in Rect are negative, the highlighted section to be taken from the view will be bigger.
        // If null, the highlighted section will match the one defined by getViewBoundaryPadding()
        // ................
        // :              :--> view to be used for the gesture from getViewBoundaryPadding()
        // :  ▓▓▓▓▓▓▓▓▓▓  :
        // :  ▓Θ▓▓▓▓▓▓▓▓  :    Θ -> where the action image is going to be shown
        // :  ▓▓▓▓▓▓▓▓▓▓--:--> part of the view to be highlighted if HighlightedViewBoundaryPadding added
        // :              :
        // ................
        // °°°°°°°°°° -------> where the loader is going to be shown
        fun getHighlightedViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect?

        //All together
        // ╔═══════════════════════╗
        // ║ ┌──────────────────┐  ║
        // ║ │ ................ │  ║--> view selected from recycler view (selectedView)
        // ║ │ :              : │--║--> view section to be part of the selection gesture if not ViewBoundaryPadding added
        // ║ │ :  ▓Θ▓▓▓▓▓▓▓▓--:-│--║--> view section  to be highlighted if HighlightedViewBoundaryPadding added
        // ║ │ :              :-│--║--> view section to be part of the selection gesture (when ViewBoundaryPadding added)
        // ║ │ ................ │  ║    Θ -> where the action image is going to be shown
        // ║ │ °°°°°°°°°°° -----│--║--> where the loader is going to be shown
        // ║ └──────────────────┘  ║
        // ╚═══════════════════════╝

        //Where the 0 will be in the center of the highlightedView (considering padding if any specified),
        //and the loader will be at the bottom of the view boundary (or at the bottom of the padding if any).
        //Moreover the highlightedView is the only region accepted for the touching gesture. If user touches anywhere else, it won't activate the animation.
    }

    interface SimpleActionLongClickCallback: ActionLongClickCallback{
        override fun getHighlightedViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect? {
            return null
        }

        override fun getViewBoundary(selectedPosition: Int, selectedView: View?): Rect? {
            return null
        }

        override fun getViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect? {
            return null
        }

        override fun isItemActionable(selectedPosition: Int): Boolean {
            return true
        }
    }
}