/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.storykaar.sleuth.Constants;
import com.storykaar.sleuth.R;
import com.storykaar.sleuth.events.ResultsFetchedMessage;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.Result;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.services.CuriosityStore;
import com.storykaar.sleuth.services.ResultStore;
import com.storykaar.sleuth.ui.DictionaryResultViewHolder;
import com.storykaar.sleuth.ui.viewholders.ResultViewHolder;
import com.storykaar.sleuth.ui.viewholders.ShowResultViewHolder;
import com.storykaar.sleuth.ui.viewholders.StatusViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import github.nisrulz.recyclerviewhelper.RVHAdapter;
import timber.log.Timber;

/**
 * Created by pawan on 24/6/16.
 *
 * Adapter to show all results
 */
public class ResultAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RVHAdapter {

    Curiosity curiosity;
    Integer status;
    List<Result> results = new ArrayList<>();

    public ResultAdapter(@NonNull Curiosity curiosity) {
        this.curiosity = curiosity;
        status = CuriosityStore.getInstance().getStatus(curiosity);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == results.size()) {
            return Constants.TYPE_STATUS;
        }
        return results.get(position).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case Constants.TYPE_STATUS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_status, parent, false);
                viewHolder = new StatusViewHolder(view);
                break;

            case Result.SHOW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_result_show, parent, false);
                viewHolder = new ShowResultViewHolder(view);
                break;

            case Result.DICTIONARY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_result_dictionary, parent, false);
                viewHolder = new DictionaryResultViewHolder(view);
                break;

            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_result, parent, false);
                viewHolder = new ResultViewHolder(view);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ResultViewHolder) {
            ((ResultViewHolder)holder).setResult(results.get(position));
        } else if (holder instanceof StatusViewHolder) {
            ((StatusViewHolder)holder).setStatus(status);
        }
    }

    public void refresh(final Boolean force) {
        Timber.d("Refreshing the adapter");
        ResultStore.getResultStore().requestResults(curiosity, force);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultsFetched(final ResultsFetchedMessage message) {
        if (!curiosity.equals(message.curiosity)) {
            Timber.d("%s is waiting for %s but got results for %s", this, curiosity, message.curiosity);
            return;
        }

        Timber.d("Obtained results for %s", message.curiosity);

        results.clear();
        for(ResultGroup group:message.resultGroupSet) {
            results.addAll(group.getResults());
        }

        Collections.sort(results, new Comparator<Result>() {
            @Override
            public int compare(Result lhs, Result rhs) {
                return rhs.quality - lhs.quality;
            }
        });

        CuriosityStore.getInstance().markRead(curiosity);
        status = CuriosityStore.getInstance().getStatus(curiosity);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return results.size() + 1;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position, int direction) {}
}
