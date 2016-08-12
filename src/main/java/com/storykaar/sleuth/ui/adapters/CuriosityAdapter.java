/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.adapters;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.storykaar.sleuth.Constants;
import com.storykaar.sleuth.R;
import com.storykaar.sleuth.events.CuriositiesFetchedMessage;
import com.storykaar.sleuth.events.CuriosityChangedMessage;
import com.storykaar.sleuth.events.CuriosityListEmptyMessage;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.services.CuriosityStore;
import com.storykaar.sleuth.ui.viewholders.StatusViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import timber.log.Timber;

/**
 * Created by pawan on 11/6/16.
 * Manages the data of the recyclerview on main page
 */
public class CuriosityAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RVHAdapter {
    @NonNull
    ArrayList<Curiosity> curiosities;
    Map<Curiosity, Integer> statuses;

    private View context;
    private RecyclerView.LayoutManager layoutManager;

    public CuriosityAdapter(@NonNull View contextview, @NonNull RecyclerView.LayoutManager layoutManager) {
        this.context = contextview;
        this.layoutManager = layoutManager;

        curiosities = new ArrayList<>();
        statuses = new HashMap<>();

        Timber.d("New CuriosityAdapter Instance created");
    }

    public void addCuriosity(Curiosity curiosity) {
//        if (curiosities.isEmpty()) { // We have not yet received the added curiosity
//            EventBus.getDefault().post(new CuriosityListEmptyMessage(false));
//        }
        CuriosityStore.getInstance().addCuriosity(curiosity);
        Snackbar.make(context, "Added " + curiosity.query, Snackbar.LENGTH_SHORT).show();
        CuriosityStore.getInstance().requestCuriosities();
    }

    public Curiosity getCuriosityAt(int position) {
        return curiosities.get(position);
    }

    public void refresh() {
        CuriosityStore.getInstance().requestCuriosities();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCuriositiesFetched(CuriositiesFetchedMessage message) {
        Timber.d("Obtained the message %s", message);

        statuses = message.curiosities;
        ArrayList freshList = new ArrayList<>(statuses.keySet());

        Collections.sort(freshList);

        // Maintain the old list to know which positions to update
        List<Curiosity> oldCuriosities = curiosities;
        // Make the new list the default so that any update will pick up from it
        curiosities = freshList;

        notifyDataSetChanged();

        int firstEnteredPosition = Integer.MAX_VALUE;
        int oldIndex = 0;
        int freshIndex = 0;
        for (; oldIndex < oldCuriosities.size(); oldIndex++) {
            while (freshIndex < curiosities.size() &&
                    curiosities.get(freshIndex).shouldOccurBefore(oldCuriosities.get(oldIndex))) {
                freshIndex++;
                notifyItemInserted(oldIndex);
            }
            if (freshIndex < curiosities.size()) {
                if(curiosities.get(freshIndex).equals(oldCuriosities.get(oldIndex))) {
                    freshIndex++;
                } else {
                    notifyItemRemoved(oldIndex);
                }
            }
        }

        if (freshIndex < curiosities.size() && oldIndex < oldCuriosities.size()) {
            Timber.w("We had a general issue reconciling the differences between the old list and the new list. Refreshing should clear it up");
        }

        if (freshIndex < curiosities.size()) {
            notifyItemRangeInserted(freshIndex, curiosities.size() - freshIndex);
        }

        if (oldIndex < oldCuriosities.size()) {
            notifyItemRangeRemoved(oldIndex, oldCuriosities.size() - oldIndex);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCuriosityChanged(CuriosityChangedMessage message) {
        Timber.d("onCuriosityChanged with %s", message);
        if (curiosities.contains(message.curiosity)) {
            statuses.put(message.curiosity, message.status);
            notifyItemChanged(curiosities.indexOf(message.curiosity));
        }
    }

//    @Override
//    public int getItemViewType(int position) {
//        if (curiosities.size() == 0) {
//            return Constants.TYPE_STATUS;
//        }
//        return Constants.TYPE_CURIOSITY;
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (viewType == Constants.TYPE_STATUS) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_status, parent, false);
//            return new StatusViewHolder(view);
//        }

        View curiosityView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_curiosity, parent, false);
        Timber.d("View holder created");
        return new CuriosityViewHolder(curiosityView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CuriosityViewHolder) {
            Curiosity curiosity = curiosities.get(position);
            Integer status = CuriosityStore.getInstance().getStatus(curiosity);
            Timber.d("Binding %s", curiosity);
            ((CuriosityViewHolder)holder).setCuriosity(curiosity, status);
        } else if (holder instanceof StatusViewHolder) {
            ((StatusViewHolder)holder).setStatus(Constants.STATUS_NO_CURIOSITIES);
        }
    }

    @Override
    public int getItemCount() {
        if (curiosities.isEmpty()) {
            EventBus.getDefault().post(new CuriosityListEmptyMessage(true));
        } else {
            EventBus.getDefault().post(new CuriosityListEmptyMessage(false));
        }

        return curiosities.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        final Curiosity curiosity = curiosities.get(position);
        CuriosityStore.getInstance().removeCuriosity(curiosity);
        notifyItemRemoved(position);
        curiosities.remove(position);

        Snackbar.make(context, R.string.undoDeleteCuriosity, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CuriosityStore.getInstance().undoDeletions();
                        refresh();
                    }
                }).show();

//        if (curiosities.isEmpty()) {
//            EventBus.getDefault().post(new CuriosityListEmptyMessage(true));
//        } else {
//            Timber.d("Curiosities not empty because %s", curiosity);
//        }
    }

    public static class CuriosityViewHolder
            extends RecyclerView.ViewHolder
            implements RVHViewHolder {
        private final TextView curiosityTextView;

        @BindView(R.id.colourStrip)
        View colourStrip;
        Integer status = Constants.STATUS_INITIAL;

        public CuriosityViewHolder(View itemView) {
            super(itemView);
            this.curiosityTextView = (TextView)itemView.findViewById(R.id.curiosityTextView);
            ButterKnife.bind(this, itemView);
        }

        public void setCuriosity(Curiosity curiosity, Integer status) {
            if (this.status != status) {
                if (Constants.STATUS_HAS_UNREAD_RESULTS == (status & Constants.STATUS_HAS_UNREAD_RESULTS)) {
                    curiosityTextView.setTypeface(null, Typeface.BOLD);
                    colourStrip.setBackgroundColor(itemView.getResources().getColor(R.color.unreadColourStrip));
//                } else if (Curiosity.STATUS_QUERYING_FINISHED == (status & Curiosity.STATUS_QUERYING_FINISHED) &&
//                          !(Curiosity.STATUS_HAS_RESULTS == (status & Curiosity.STATUS_HAS_RESULTS))) {
//                    curiosityTextView.setTypeface(null, Typeface.NORMAL);
//                    colourStrip.setBackgroundColor(itemView.getResources().getColor(R.color.failedColourStrip));
                } else {
                    curiosityTextView.setTypeface(null, Typeface.NORMAL);
                    colourStrip.setVisibility(View.INVISIBLE);
                }
                colourStrip.invalidate();
            }
            curiosityTextView.setText(curiosity.query + ":" + Integer.toHexString(status));
        }

        @Override
        public void onItemSelected(int actionstate) {
        }

        @Override
        public void onItemClear() {

        }
    }
}
