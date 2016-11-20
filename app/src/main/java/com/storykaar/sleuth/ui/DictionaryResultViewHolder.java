/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.ui;

import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.storykaar.sleuth.Constants;
import com.storykaar.sleuth.R;
import com.storykaar.sleuth.model.Result;
import com.storykaar.sleuth.ui.viewholders.ResultViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pawan on 27/8/16.
 * <p/>
 * ViewHolder for dictionary data
 */
public class DictionaryResultViewHolder
        extends ResultViewHolder {

    @BindView(R.id.rdictionary_word)
    TextView wordView;
    @BindView(R.id.rdictionary_source)
    TextView sourceView;
    @BindView(R.id.rdictionary_meanings)
    TextView meaningsView;

    public DictionaryResultViewHolder(final View view) {
        super(view);

        ButterKnife.bind(this, view);
    }

    @Override
    public void setResult(@NonNull final Result result) {
        String word = (String) result.propertyMap.get(Constants.WORDNIK_WORD);
        wordView.setText(word);

        String sourceDictionary = (String) result.propertyMap.get(Constants.WORDNIK_S0URCE);
        sourceView.setText(sourceDictionary);

        StringBuilder meaningsBuilder = new StringBuilder();
        for (String key : result.propertyMap.keySet()) {
            if (Constants.WORDNIK_WORD.equals(key) || Constants.WORDNIK_S0URCE.equals(key)) {
                continue;
            }

            String partOfSpeech = key;
            ArrayList<String> meanings = (ArrayList<String>) result.propertyMap.get(partOfSpeech);

            StringBuilder builder = new StringBuilder()
                    .append("<br/><em><b>")
                    .append(partOfSpeech)
                    .append("</b></em><br/>");
            for (int i = 0; i < meanings.size(); i++) {
                String meaning = meanings.get(i);

                builder.append("\t")
                        .append(i + 1)
                        .append(". ")
                        .append(meaning)
                        .append("<br/>");
            }

            meaningsBuilder.append(builder.toString());
        }

        meaningsView.setText(Html.fromHtml(meaningsBuilder.toString()));
    }
}