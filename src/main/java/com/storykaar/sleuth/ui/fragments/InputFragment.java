/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.storykaar.sleuth.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pawan on 29/6/16.
 */
public class InputFragment
        extends DialogFragment
        implements TextView.OnEditorActionListener {
    @BindView(R.id.query_box) EditText queryBox;

    public interface InputFragmentListener {
        void onInputDone(final String query);
    }

    public InputFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_input, container);
        ButterKnife.bind(this, view);

        queryBox.setOnEditorActionListener(this);
        queryBox.requestFocus();

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_SEARCH == actionId) {
            String query = queryBox.getText().toString().trim();
            // TODO: Validate the query
            // TODO: Add quick labels
            // TODO: Extract labels

            if (!query.isEmpty()) {
                ((InputFragmentListener) getParentFragment()).onInputDone(query);
                dismiss();
                return true;
            }
        }

        return false;
    }
}
