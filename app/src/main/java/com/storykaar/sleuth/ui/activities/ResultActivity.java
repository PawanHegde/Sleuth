/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.storykaar.sleuth.R;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.ui.adapters.ResultAdapter;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by pawan on 10/7/16.
 */
public class ResultActivity
        extends AppCompatActivity {

    public static final String CURIOSITY_KEY = "curiosity";

    @BindView(R.id.results_action_bar)
    Toolbar actionBar;
    @BindView(R.id.results_action_bar_textview)
    TextView actionBarText;
    @BindView(R.id.results_list_view)
    RecyclerView recyclerView;

    private Curiosity curiosity;
    private ResultAdapter adapter;

    @Override
    // TODO: Move less important items to other functions
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("Creating resultsactivity with %s", savedInstanceState);

        setContentView(R.layout.activity_results);
        ButterKnife.bind(this);
        setSupportActionBar(actionBar);
        if (getSupportActionBar() == null) {
            Timber.e("Failed to get an action bar. Killing self with a rusted scythe");
            throw new IllegalStateException("Unable to find actionbar");
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);

        if (savedInstanceState != null) {
            Timber.d("Restoring Curiosity from savedInstanceState");
            curiosity = (Curiosity) savedInstanceState.getSerializable(CURIOSITY_KEY);
        } else {
            Timber.d("Restoring Curiosity from intent");
            curiosity = (Curiosity) getIntent().getSerializableExtra(CURIOSITY_KEY);
            Timber.d("Intent bundle had %s", getIntent().getExtras());
        }

        if (curiosity != null) {
            actionBarText.setText(curiosity.query);
        } else {
            Timber.e("Creating a results activity without a curiosity? Hahah... Sure.");
            throw new IllegalStateException("No Curiosity object obtained");
        }

        adapter = new ResultAdapter(curiosity);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Timber.d("Registering ResultAdapter");
        if (!EventBus.getDefault().isRegistered(adapter)) {
            EventBus.getDefault().register(adapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Timber.d("Saving instance state");
        savedInstanceState.putSerializable(CURIOSITY_KEY, curiosity);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter.refresh(false);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Timber.d("Unregistering ResultAdapter");
        EventBus.getDefault().unregister(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as opposed to navigating up

        return false;
    }
}
