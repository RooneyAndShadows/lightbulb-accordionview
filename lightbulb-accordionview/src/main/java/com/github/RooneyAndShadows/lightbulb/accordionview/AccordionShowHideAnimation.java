package com.github.RooneyAndShadows.lightbulb.accordionview;

import android.view.View;

class AccordionShowHideAnimation implements AccordionAnimation {
    private View targetView;

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