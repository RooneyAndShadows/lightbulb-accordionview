package com.github.rooneyandshadows.lightbulb.accordionview.animation

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout

class AccordionTransitionAnimation(private val targetView: View?) : AccordionAnimation {
    private var currentAnimation: Animation? = null
    override fun expand(duration: Int) {
        if (hasRunningAnimation()) {
            targetView!!.clearAnimation()
            currentAnimation!!.cancel()
        }
        val matchParentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec((targetView!!.parent as View).width, View.MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        targetView.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = targetView.measuredHeight
        targetView.layoutParams.height = 1
        targetView.visibility = View.VISIBLE
        currentAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                targetView.layoutParams.height =
                    if (interpolatedTime == 1f) LinearLayout.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                targetView.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        currentAnimation!!.fillAfter = true
        currentAnimation!!.duration = duration.toLong()
        targetView.startAnimation(currentAnimation)
    }

    override fun collapse(duration: Int) {
        if (hasRunningAnimation()) {
            targetView!!.clearAnimation()
            currentAnimation!!.cancel()
        }
        val initialHeight = targetView!!.measuredHeight
        currentAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    targetView.visibility = View.GONE
                } else {
                    targetView.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    targetView.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        currentAnimation!!.fillAfter = true
        currentAnimation!!.duration = duration.toLong()
        targetView.startAnimation(currentAnimation)
    }

    override fun hasRunningAnimation(): Boolean {
        return currentAnimation != null && currentAnimation!!.hasStarted() && !currentAnimation!!.hasEnded()
    }
}