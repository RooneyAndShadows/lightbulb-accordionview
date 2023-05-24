package com.github.rooneyandshadows.lightbulb.accordionviewdemo.activity

import android.annotation.SuppressLint
import android.view.View
import com.github.rooneyandshadows.lightbulb.accordionviewdemo.R
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.SliderMenu
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.config.SliderMenuConfiguration
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.items.PrimaryMenuItem
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

object MenuConfigurations {
    @SuppressLint("InflateParams")
    fun getConfiguration(activity: BaseActivity): SliderMenuConfiguration {
        val configuration = SliderMenuConfiguration(R.layout.demo_drawer_header_view).apply {
            itemsList.apply {
                add(
                    PrimaryMenuItem(
                        -1,
                        ResourceUtils.getPhrase(activity, R.string.demo_fragment_title),
                        null,
                        null,
                        1
                    ) { slider: SliderMenu ->
                        slider.closeSlider()
                        MainActivityNavigator.route().toDemoAccordion().replace()
                    }
                )
            }
        }
        return configuration
    }
}
