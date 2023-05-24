package com.github.rooneyandshadows.lightbulb.accordionviewdemo.fragments

import com.github.rooneyandshadows.lightbulb.accordionview.AccordionView
import com.github.rooneyandshadows.lightbulb.accordionviewdemo.R
import com.github.rooneyandshadows.lightbulb.accordionviewdemo.getHomeDrawable
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen
import com.github.rooneyandshadows.lightbulb.application.fragment.base.BaseFragment
import com.github.rooneyandshadows.lightbulb.application.fragment.cofiguration.ActionBarConfiguration
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

@FragmentScreen(screenName = "Accordion", screenGroup = "Demo")
@FragmentConfiguration(layoutName = "fragment_demo_accordion", hasLeftDrawer = true)
class FragmentAccordionDemo : BaseFragment() {
    @BindView(name = "accordionViewCard")
    lateinit var accordionCard: AccordionView
    @BindView(name = "accordionViewStroke")
    lateinit var accordionStroke: AccordionView

    @Override
    override fun configureActionBar(): ActionBarConfiguration {
        val title = ResourceUtils.getPhrase(requireContext(), R.string.demo_fragment_title)
        val subTitle = ResourceUtils.getPhrase(requireContext(), R.string.app_name)
        val homeIcon = getHomeDrawable(requireContext())
        return ActionBarConfiguration(R.id.toolbar)
            .withActionButtons(true)
            .withHomeIcon(homeIcon)
            .withTitle(title)
            .withSubTitle(subTitle)
    }
}