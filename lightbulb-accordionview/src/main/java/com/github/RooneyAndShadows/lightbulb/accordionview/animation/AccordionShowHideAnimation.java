package com.github.rooneyandshadows.lightbulb.accordionview.animation;

import android.view.View;

public class AccordionShowHideAnimation implements AccordionAnimation {
    private final View targetView;

    public AccordionShowHideAnimation(View targetView) {
        this.targetView = targetView;
    }

    @Override
    public void expand(int duration) {
        targetView.setVisibility(View.VISIBLE);
    }

    @Override
    public void collapse(int duration) {
        targetView.setVisibility(View.GONE);
    }

    public boolean hasRunningAnimation() {
        return false;
    }
}