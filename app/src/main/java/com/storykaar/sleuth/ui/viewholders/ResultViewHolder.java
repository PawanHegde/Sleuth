/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.storykaar.sleuth.R;
import com.storykaar.sleuth.events.ImageFetchedMessage;
import com.storykaar.sleuth.model.Result;
import com.storykaar.sleuth.services.ImageStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

/**
 * Created by pawan on 9/7/16.
 */
public class ResultViewHolder
        extends RecyclerView.ViewHolder {

    private final TextView resultTextView;
    private final ImageView imageView;

    private Result result;

    public ResultViewHolder(final View itemView) {
        super(itemView);

        this.resultTextView = (TextView) itemView.findViewById(R.id.result_text);
        this.imageView = (ImageView) itemView.findViewById(R.id.rshow_poster);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void setResult(@NonNull final Result result) {
        this.result = result;

        this.resultTextView.setText(Html.fromHtml(getTextRepresentation(result)));
        this.resultTextView.setLinksClickable(true);
        this.resultTextView.setMovementMethod(LinkMovementMethod.getInstance());

        if (result.image != null && !result.image.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            ImageStore.getInstance().requestImage(result.image);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImageFetched(@NonNull ImageFetchedMessage message) {
        Timber.d("Has Image [%s] and image Received %s. Was expecting %s", message.hasImage, message.url, result.image);
        if (message.hasImage && Objects.equals(result.image, message.url)) {
            Timber.d("Correct Image Received");
            imageView.setImageBitmap(message.image);
        } else {
            Timber.d("Expected image %s but got %s", result.image, message.url);
        }
    }

    private String getTextRepresentation(final Result result) {
        HashMap<String, Object> properties = result.propertyMap;
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