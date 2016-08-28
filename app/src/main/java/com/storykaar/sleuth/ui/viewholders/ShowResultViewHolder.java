/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.viewholders;

import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.storykaar.sleuth.R;
import com.storykaar.sleuth.model.Result;
import com.storykaar.sleuth.ui.views.ExpandableCardView;
import com.storykaar.sleuth.ui.views.LabeledTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pawan on 9/7/16.
 */
public class ShowResultViewHolder
        extends ResultViewHolder {

    @BindView(R.id.rshow_card)
    ExpandableCardView card;
    @BindView(R.id.rshow_plot)
    LabeledTextView plotView;
    @BindView(R.id.rshow_title)
    TextView titleView;
    @BindView(R.id.rshow_image_holder)
    PercentRelativeLayout imageHolder;
    @BindView(R.id.rshow_poster)
    ImageView imageView;
    @BindView(R.id.rshow_actors)
    LabeledTextView actorsView;
    @BindView(R.id.rshow_directors)
    LabeledTextView directorsView;
    @BindView(R.id.rshow_rated)
    LabeledTextView ratedView;
    @BindView(R.id.rshow_genre)
    LabeledTextView genreView;
    @BindView(R.id.rshow_country)
    LabeledTextView countryView;

    String sourceURL;

    public ShowResultViewHolder(final View view) {
        super(view);

        ButterKnife.bind(this, view);
    }

    @Override
    public void setResult(final Result result) {
        titleView.setText(String.valueOf(result.propertyMap.get("Title")));
        plotView.setValue(String.valueOf(result.propertyMap.get("Plot")));
        actorsView.setValue(String.valueOf(result.propertyMap.get("Actors")));
        directorsView.setValue(String.valueOf(result.propertyMap.get("Director")));
        ratedView.setValue(String.valueOf(result.propertyMap.get("Rated")));
        genreView.setValue(String.valueOf(result.propertyMap.get("Genre")));
        countryView.setValue(String.valueOf(result.propertyMap.get("Country")));

        if (result.image != null) {
//            float finalWidth = imageView.getWidth();
//            float finalHeight = finalWidth * 9 / 16;
//
//            Matrix imageMatrix = new Matrix();
//            imageMatrix.setScale(finalWidth, finalHeight);

//            imageView.setImageMatrix(imageMatrix);
            Glide.with(imageView.getContext())
                    .load(result.image)
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
            imageView.bringToFront();
        }
    }

    @OnClick(R.id.rshow_visit_source)
    public void onClickVisitSource(View view) {
        Toast.makeText(view.getContext(), "Shall take you to Imdb", Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.rshow_card, R.id.rshow_poster})
    public void onClickCard(View view) {
        if (view instanceof ImageView && card.isExpanded()) {
            Toast.makeText(view.getContext(), "Image Touched", Toast.LENGTH_SHORT).show();
        } else {
            card.toggleState();
            Toast.makeText(view.getContext(), "Card Touched", Toast.LENGTH_SHORT).show();
        }
    }
}