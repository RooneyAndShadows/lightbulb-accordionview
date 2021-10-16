package com.rands.lightbulb.accordionview;

interface AccordionAnimation {

    void expand(int duration);

    void collapse(int duration);

    boolean hasRunningAnimation();
}