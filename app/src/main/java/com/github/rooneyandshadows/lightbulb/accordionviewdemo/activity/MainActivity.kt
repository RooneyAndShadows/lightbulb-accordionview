package com.github.rooneyandshadows.lightbulb.accordionviewdemo.activity

import android.os.Bundle
import com.github.rooneyandshadows.lightbulb.accordionviewdemo.R
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.config.SliderMenuConfiguration

@ActivityConfiguration
class MainActivity : BaseActivity() {

    @Override
    override fun doBeforeCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DemoTheme)
    }

    @Override
    override fun getMenuConfiguration(): SliderMenuConfiguration {
        return MenuConfigurations.getConfiguration(this)
    }

    @Override
    override fun doOnCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            MainActivityNavigator.route().toDemoAccordion().newRootScreen()
        }
    }
}