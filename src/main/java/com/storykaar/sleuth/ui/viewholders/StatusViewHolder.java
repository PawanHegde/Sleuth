/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.storykaar.sleuth.Constants;
import com.storykaar.sleuth.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by pawan on 7/8/16.
 */
public class StatusViewHolder
        extends RecyclerView.ViewHolder {
    @BindView(R.id.status_image)
    ImageView statusImage;
    @BindView(R.id.status_text)
    TextView statusText;
    private int finishedMessage;

    public StatusViewHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setStatus(Integer status) {
        if (isCuriosityListEmpty(status)) {
            Timber.d("Setting Status: Curiosity List Empty");
            itemView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            statusImage.setImageResource(R.drawable.empty_folder);
            statusImage.setVisibility(View.VISIBLE);
            statusText.setText(getEmptyCuriosityListMessage());
            return;
        }

        if (isNoResultAvailable(status)) {
            Timber.d("Setting Status: No Result Available");
            itemView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            statusImage.setVisibility(View.GONE);
            statusText.setText(getNoResultsFoundMessage());
            return;
        }

        if (isStillTrying(status)) {
            Timber.d("Setting Status: Still Trying");
            itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            statusImage.setVisibility(View.GONE);
            statusText.setText(getStillTryingMessage());
            statusText.setVisibility(View.VISIBLE);
            return;
        }

        if (isSuccessful(status)) {
            Timber.d("Setting Status: Successful");
            itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            statusImage.setVisibility(View.GONE);
            statusText.setText(getFinishedMessage());
            statusText.setVisibility(View.VISIBLE);
            return;
        }

        Timber.d("Setting status actually %d", status);
        statusImage.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
    }

    private boolean isSuccessful(Integer status) {
        return ((status & Constants.STATUS_QUERYING_FINISHED) == Constants.STATUS_QUERYING_FINISHED)
                && ((status & Constants.STATUS_HAS_RESULTS) == Constants.STATUS_HAS_RESULTS);
    }

    private boolean isStillTrying(Integer status) {
        return !((status & Constants.STATUS_QUERYING_FINISHED) == Constants.STATUS_QUERYING_FINISHED);
    }

    private boolean isNoResultAvailable(Integer status) {
        return (((status & Constants.STATUS_QUERYING_FINISHED) == Constants.STATUS_QUERYING_FINISHED)
                && !((status & Constants.STATUS_HAS_RESULTS) == Constants.STATUS_HAS_RESULTS));
    }

    private boolean isCuriosityListEmpty(Integer status) {
        return (status & Constants.STATUS_NO_CURIOSITIES) == Constants.STATUS_NO_CURIOSITIES;
    }


    public int getEmptyCuriosityListMessage() {
        return R.string.curiosity_list_empty;
    }

    public int getNoResultsFoundMessage() {
        return R.string.no_results_found;
    }

    public int getStillTryingMessage() {
        return R.string.still_trying;
    }

    public int getFinishedMessage() {
        return R.string.fin;
    }
}
