package com.github.rooneyandshadows.lightbulb.accordionview.animation

import android.view.View

class AccordionShowHideAnimation(private val targetView: View?) : AccordionAnimation {
    override fun expand(duration: Int) {
        targetView!!.visibility = View.VISIBLE
    }

    override fun collapse(duration: Int) {
        targetView!!.visibility = View.GONE
    }

    override fun hasRunningAnimation(): Boolean {
        return false
    }
}