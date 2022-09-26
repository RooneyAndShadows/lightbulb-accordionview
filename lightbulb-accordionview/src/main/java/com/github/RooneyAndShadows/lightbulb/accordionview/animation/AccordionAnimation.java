package com.github.rooneyandshadows.lightbulb.accordionview.animation;

public interface AccordionAnimation {

    void expand(int duration);

    void collapse(int duration);

    boolean hasRunningAnimation();
}
