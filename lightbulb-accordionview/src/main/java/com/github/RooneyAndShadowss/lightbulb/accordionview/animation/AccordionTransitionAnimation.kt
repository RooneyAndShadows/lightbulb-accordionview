package com.github.rooneyandshadows.lightbulb.accordionview.animation

import android.animation.ValueAnimator
import android.view.View
import android.view.View.GONE
import android.view.View.MeasureSpec.*
import android.view.View.VISIBLE
import android.view.animation.DecelerateInterpolator
import java.lang.Integer.max

class AccordionTransitionAnimation(private val targetView: View) : AccordionAnimation {
    private val animator: ValueAnimator = ValueAnimator.ofInt().apply {
        interpolator = DecelerateInterpolator()
        addUpdateListener { animation ->
            val newValue = (animation.animatedValue as Int)
            val targetVisibility = if (newValue == 0) GONE else VISIBLE
            targetView.layoutParams.height = newValue
            if (targetView.visibility != targetVisibility) targetView.visibility = targetVisibility
            targetView.requestLayout()
        }
    }

    @Override
    override fun expand(duration: Int) {
        animate(duration, true)
    }

    @Override
    override fun collapse(duration: Int) {
        animate(duration, false)
    }

    @Override
    override fun hasRunningAnimation(): Boolean {
        return animator.isRunning
    }

    private fun animate(duration: Int, expand: Boolean) {
        interruptAnimationIfRunning()
        measureTarget()
        val start = max(targetView.layoutParams.height, 0)
        val end = if (expand) targetView.measuredHeight else 0
        animator.setIntValues(start, end)
        animator.duration = duration.toLong()
        animator.start()
    }

    private fun measureTarget() {
        val matchParentMeasureSpec = makeMeasureSpec((targetView.parent as View).width, EXACTLY)
        val wrapContentMeasureSpec = makeMeasureSpec(0, UNSPECIFIED)
        targetView.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    }

    private fun interruptAnimationIfRunning() {
        if (!hasRunningAnimation()) return
        targetView.clearAnimation()
        animator.cancel()
    }
}