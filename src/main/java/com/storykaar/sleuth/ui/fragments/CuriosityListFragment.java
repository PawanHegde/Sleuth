/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.storykaar.sleuth.R;
import com.storykaar.sleuth.events.CuriosityListEmptyMessage;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.ui.activities.ResultActivity;
import com.storykaar.sleuth.ui.adapters.CuriosityAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import github.nisrulz.recyclerviewhelper.RVHItemClickListener;
import github.nisrulz.recyclerviewhelper.RVHItemDividerDecoration;
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import timber.log.Timber;

public class CuriosityListFragment
        extends Fragment
        implements View.OnClickListener, InputFragment.InputFragmentListener {
    //@BindView(R.id.add_curiosity_button) FloatingActionButton addCuriosityButton;
    @BindView(R.id.curiosity_list_view)
    RecyclerView recyclerView;
    @BindView(R.id.no_curiosites_text)
    TextView noCuriositiesText;
    @BindView(R.id.no_curiosities_image)
    ImageView noCuriositiesImage;

    private Context context;
    private CuriosityAdapter adapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curiosity_list, container, false);
    }

    @Override
    public void onViewCreated(final View view, final @Nullable Bundle savedInstanceState) {
        Timber.d("CuriosityListFragment view created");

        ButterKnife.bind(this, view);

        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CuriosityAdapter(view, layoutManager);
//        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                Timber.d("onChanged called with %d", adapter.getItemCount());
//                if (adapter.getItemCount() == 0) {
//                    recyclerView.setVisibility(View.GONE);
//                    noCuriositiesImage.setVisibility(View.VISIBLE);
//                    noCuriositiesText.setVisibility(View.VISIBLE);
//                } else {
//                    noCuriositiesImage.setVisibility(View.GONE);
//                    noCuriositiesText.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onItemRemoved() {
//                Timber.d("onChanged called with %d", adapter.getItemCount());
//                if (adapter.getItemCount() == 0) {
//                    recyclerView.setVisibility(View.GONE);
//                    noCuriositiesImage.setVisibility(View.VISIBLE);
//                    noCuriositiesText.setVisibility(View.VISIBLE);
//                } else {
//                    noCuriositiesImage.setVisibility(View.GONE);
//                    noCuriositiesText.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onItemRangeChanged(int positionStart, int itemCount) {
//                Timber.d("onItemRangeChanged called with %d", adapter.getItemCount());
//                if (adapter.getItemCount() == 0) {
//                    recyclerView.setVisibility(View.GONE);
//                    noCuriositiesImage.setVisibility(View.VISIBLE);
//                    noCuriositiesText.setVisibility(View.VISIBLE);
//                } else {
//                    noCuriositiesImage.setVisibility(View.GONE);
//                    noCuriositiesText.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onItemRangeRemoved(int positionStart, int itemCount) {
//                Timber.d("onItemRangeChanged called with %d", adapter.getItemCount());
//                if (adapter.getItemCount() == 0) {
//                    recyclerView.setVisibility(View.GONE);
//                    noCuriositiesImage.setVisibility(View.VISIBLE);
//                    noCuriositiesText.setVisibility(View.VISIBLE);
//                } else {
//                    noCuriositiesImage.setVisibility(View.GONE);
//                    noCuriositiesText.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        recyclerView.setAdapter(adapter);

        // Setup onItemTouchHandler to enable drag and drop , swipe left or right
        ItemTouchHelper.Callback callback = new RVHItemTouchHelperCallback(adapter, false, true, true);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        // Set the divider in the recyclerview
        recyclerView.addItemDecoration(new RVHItemDividerDecoration(view.getContext(), LinearLayoutManager.VERTICAL));

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(20);
        itemAnimator.setRemoveDuration(200);
        recyclerView.setItemAnimator(itemAnimator);

        // Set On Click Listener
        recyclerView.addOnItemTouchListener(new RVHItemClickListener(view.getContext(), new RVHItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                Curiosity curiosity = adapter.getCuriosityAt(position);

                Intent intent = new Intent()
                        .setClass(getActivity(), ResultActivity.class)
                        .putExtra(ResultActivity.CURIOSITY_KEY, curiosity);

                Timber.d("Launching result activity for %s", curiosity);
                startActivity(intent);
            }
        }));
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        this.context = context;

        Timber.d("CuriosityListFragment attached");
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
        if (!EventBus.getDefault().isRegistered(adapter)) {
            EventBus.getDefault().register(adapter);
        } else {
            Timber.w("Adapter %s was already registered with EventBus. Could be an issue");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.refresh();
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(adapter);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        context = null;

        Timber.d("CuriosityListFragment detached");
    }

    @Override
    @OnClick(R.id.add_curiosity_button)
    public void onClick(final View v) {
        if (R.id.add_curiosity_button == v.getId()) {

            FragmentManager fragmentManager = getChildFragmentManager();
            InputFragment inputFragment = new InputFragment();
            inputFragment.show(fragmentManager, "InputFragment");
        } else {
            Timber.d("Did not recognise %s", v.getId());
        }
    }

    @Override
    public void onInputDone(final String query) {
        Timber.d("New query %s submitted for saving", query);
        adapter.addCuriosity(new Curiosity(query));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCuriosityListEmptyMessage(CuriosityListEmptyMessage message) {
        if (message.isEmpty) {
            recyclerView.setVisibility(View.GONE);
            noCuriositiesImage.setVisibility(View.VISIBLE);
            noCuriositiesText.setVisibility(View.VISIBLE);
        } else {
            noCuriositiesImage.setVisibility(View.GONE);
            noCuriositiesText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}