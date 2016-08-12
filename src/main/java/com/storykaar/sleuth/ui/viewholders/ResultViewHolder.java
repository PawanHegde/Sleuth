/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.storykaar.sleuth.R;
import com.storykaar.sleuth.model.Result;

import java.util.HashMap;

/**
 * Created by pawan on 9/7/16.
 */
public class ResultViewHolder
        extends RecyclerView.ViewHolder {

    private final TextView resultTextView;
    private final ImageView imageView;

    public ResultViewHolder(final View itemView) {
        super(itemView);

        this.resultTextView = (TextView) itemView.findViewById(R.id.result_text);
        this.imageView = (ImageView) itemView.findViewById(R.id.rshow_poster);
    }

    public void setResult(final Result result) {
        this.resultTextView.setText(Html.fromHtml(getTextRepresentation(result)));
        this.resultTextView.setLinksClickable(true);
        this.resultTextView.setMovementMethod(LinkMovementMethod.getInstance());

        if (result.image != null) {
            Glide.with(imageView.getContext())
                    .load(result.image)
                    .into(imageView);
        }
    }

    protected String getTextRepresentation(final Result result) {
        HashMap<String, String> properties = result.propertyMap;
        StringBuilder textBuilder = new StringBuilder();

        for (String key : properties.keySet()) {
            textBuilder.append("<b>");
            textBuilder.append(key);
            textBuilder.append(": ");
            textBuilder.append("</b>");
            textBuilder.append(properties.get(key));
            textBuilder.append("<br/>");
        }

        return textBuilder.toString();
    }
}