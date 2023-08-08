package com.github.rooneyandshadows.lightbulb.accordionview.animation

interface AccordionAnimation {
    fun expand(duration: Int)
    fun collapse(duration: Int)
    fun hasRunningAnimation(): Boolean
}