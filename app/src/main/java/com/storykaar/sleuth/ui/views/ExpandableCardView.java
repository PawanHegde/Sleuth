/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import timber.log.Timber;

/**
 * Created by pawan on 24/7/16.
 */
public class ExpandableCardView
        extends CardView {

    boolean expanded;

    public ExpandableCardView(Context context) {
        super(context);
        initialise();
    }

    public ExpandableCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public ExpandableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
    }

    private void initialise() {
        expanded = false;
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!expanded) {
            double width = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            double height = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

//            Timber.d("On Measure Width: %s", width);
//            Timber.d("On Measure Height: %s", height);

            if (height != 0) {
                Timber.d("Height wasn't 0");
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) Math.min(width * 0.5, height), MeasureSpec.AT_MOST);
            }


        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void toggleState() {
        expanded =! expanded;

        Timber.d("Toggling state");

        this.requestLayout();
    }

    private void transform(final View view) {
        final int currentHeight = view.getMeasuredHeight();
        final int currentWidth = view.getMeasuredWidth();
        expanded = !expanded;
        Timber.d("Expanded %s", expanded);
        view.measure(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        final int targetHeight = view.getMeasuredHeight();
        final int targetWidth = view.getMeasuredWidth();

        Timber.d("Current Height: %s", currentHeight);
        Timber.d("Target Height: %s", targetHeight);
        Timber.d("Current Width: %s", currentWidth);
        Timber.d("Target Width: %s", targetWidth);

        if (view.getLayoutParams().height == 0) {
            view.getLayoutParams().height = 1;
        }

        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = targetHeight;
                /*view.getLayoutParams().height = interpolatedTime == 1
                        ? targetHeight
                        : (int)(currentHeight + (targetHeight - currentHeight) * interpolatedTime);*/
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        animation.setDuration((int)(Math.abs
                (targetHeight - currentHeight)/ view.getContext().getResources().getDisplayMetrics().density));
//        animation.setDuration(5000);
        view.startAnimation(animation);
    }
}
