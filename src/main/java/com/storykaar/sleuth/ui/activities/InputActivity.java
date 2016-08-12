/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.storykaar.sleuth.R;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.services.CuriosityStore;
import com.thefinestartist.movingbutton.MovingButton;
import com.thefinestartist.movingbutton.enums.ButtonPosition;

import butterknife.BindView;
import butterknife.OnClick;

public class InputActivity
        extends AppCompatActivity
        implements MovingButton.OnPositionChangedListener {

    @BindView(R.id.actionBarInputActivity)
    Toolbar actionBar;
    @BindView(R.id.query_box) EditText queryBox;
    @BindView(R.id.search_button) MovingButton searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        searchBtn.setOnPositionChangedListener(this);
    }

    @OnClick(R.id.search_button)
    public void onClick(View view) {
        Toast.makeText(this, "McSmartyPants wants to Google for shit!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositionChanged(int action, ButtonPosition position) {
        String query = queryBox.getText().toString();

        if (position.equals(ButtonPosition.ORIGIN) || query.isEmpty()) {
            return;
        }

        if (position.equals(ButtonPosition.DOWN)) {
            CuriosityStore.getInstance().addCuriosity(new Curiosity(query));
        } else if (position.equals(ButtonPosition.UP)) {
            Toast.makeText(InputActivity.this, "Up pulled!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
