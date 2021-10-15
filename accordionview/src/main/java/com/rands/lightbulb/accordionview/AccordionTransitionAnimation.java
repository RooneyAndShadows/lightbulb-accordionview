package com.rands.lightbulb.accordionview;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

class AccordionTransitionAnimation implements AccordionAnimation {

    private Animation currentAnimation;
    private View targetView;

    public AccordionTransitionAnimation(View targetView) {
        this.targetView = targetView;
    }

    @Override
    public void expand(int duration) {
        if (hasRunningAnimation()) {
            targetView.clearAnimation();
            currentAnimation.cancel();
        }
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) targetView.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        targetView.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = targetView.getMeasuredHeight();
        targetView.getLayoutParams().height = 1;
        targetView.setVisibility(View.VISIBLE);
        currentAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                targetView.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                targetView.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        currentAnimation.setFillAfter(true);
        currentAnimation.setDuration(duration);
        targetView.startAnimation(currentAnimation);
    }

    @Override
    public void collapse(int duration) {
        if (hasRunningAnimation()) {
            targetView.clearAnimation();
            currentAnimation.cancel();
        }
        final int initialHeight = targetView.getMeasuredHeight();
        currentAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    targetView.setVisibility(View.GONE);
                } else {
                    targetView.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    targetView.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }

        };
        currentAnimation.setFillAfter(true);
        currentAnimation.setDuration(duration);
        targetView.startAnimation(currentAnimation);
    }

    public boolean hasRunningAnimation() {
        return currentAnimation != null && currentAnimation.hasStarted() && !currentAnimation.hasEnded();
    }
}
