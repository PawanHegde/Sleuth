/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.storykaar.sleuth.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pawan on 24/7/16.
 */
public class LabeledTextView
        extends RelativeLayout {
    @BindView(R.id.label_text) TextView labelView;
    @BindView(R.id.value_text) TextView valueView;
    private String label;
    private String value;

    public LabeledTextView(Context context) {
        super(context);

        initialiseViews(context, null);
    }

    public LabeledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialiseViews(context, attrs);
    }

    public LabeledTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialiseViews(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LabeledTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialiseViews(context, attrs);
    }

    private void initialiseViews(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.labeled_text, this);

        ButterKnife.bind(this, view);

        if (attrs != null) {
            label = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "label");
            if (label != null) {
                labelView.setVisibility(VISIBLE);
                labelView.setText(label);
            }
        }
    }


    public void setValue(String value) {
        this.value = value;

        if (value != null) {
            valueView.setText(value);
            this.setVisibility(VISIBLE);
//            Timber.d("Setting value as %s", value);
        } else {
//            Timber.d("Setting value as %s", value);
            this.setVisibility(GONE);
        }
    }
}


