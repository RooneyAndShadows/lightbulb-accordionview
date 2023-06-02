package com.github.rooneyandshadows.lightbulb.accordionviewdemo.fragments

import com.github.rooneyandshadows.lightbulb.accordionview.AccordionGroupView
import com.github.rooneyandshadows.lightbulb.accordionviewdemo.R
import com.github.rooneyandshadows.lightbulb.accordionviewdemo.getHomeDrawable
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen
import com.github.rooneyandshadows.lightbulb.application.fragment.base.BaseFragment
import com.github.rooneyandshadows.lightbulb.application.fragment.cofiguration.ActionBarConfiguration
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

@FragmentScreen(screenName = "AccordionGroup", screenGroup = "Demo")
@FragmentConfiguration(layoutName = "fragment_demo_accordion_group", hasLeftDrawer = true)
class FragmentAccordionGroupDemo : BaseFragment() {
    @BindView(name = "accordion_group")
    lateinit var accordionGroup: AccordionGroupView

    @Override
    override fun configureActionBar(): ActionBarConfiguration {
        val title = ResourceUtils.getPhrase(requireContext(), R.string.app_name)
        val subTitle = ResourceUtils.getPhrase(requireContext(), R.string.demo_accordion_title)
        val homeIcon = getHomeDrawable(requireContext())
        return ActionBarConfiguration(R.id.toolbar)
            .withActionButtons(true)
            .withHomeIcon(homeIcon)
            .withTitle(title)
            .withSubTitle(subTitle)
    }
}