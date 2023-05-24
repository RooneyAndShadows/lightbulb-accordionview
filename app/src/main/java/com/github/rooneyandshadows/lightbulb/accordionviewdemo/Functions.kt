package com.github.rooneyandshadows.lightbulb.accordionviewdemo

import android.content.Context
import android.graphics.drawable.Drawable
import com.github.rooneyandshadows.lightbulb.accordionview.R
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.drawable.ShowMenuDrawable
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

fun getHomeDrawable(context: Context): Drawable {
    return ShowMenuDrawable(context).apply {
        setEnabled(false)
        backgroundColor = ResourceUtils.getColorByAttribute(context, R.attr.colorError)
    }
}