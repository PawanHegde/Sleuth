/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders;

import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.Result;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;
import com.storykaar.sleuth.util.ServiceGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import timber.log.Timber;

/**
 * Created by pawan on 4/7/16.
 */
public class WordnickDownloader
        implements DownloaderFactory.IDownloader {

    public interface WordnickAPI {
        //http://api.wordnik.com:80/v4/word.json/monster/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5
        //http://developer.wordnik.com/word.json/monster/definitions?limit=200&includeRelated=true&includeTags=false&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5
        @GET("v4/word.json/{word}/definitions")
        Call<List<WordnikDefinition>> fetchDefinition(@Path("word") String word, @Query("api_key") String apiKey, @Query("limit") int limit, @Query("includeRelated") Boolean includeRelated, @Query("includeTags") Boolean includeTags);
    }

    @Override
    public ResultGroup download(Curiosity curiosity) throws IOException {

        HashSet<Result> results = new HashSet<>();

        WordnickAPI wordnick = ServiceGenerator.createService("http://api.wordnik.com:80/", WordnickAPI.class);

        List<WordnikDefinition> definitions;
        try {
            definitions = wordnick.fetchDefinition(curiosity.query, "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5", 200, true, true).execute().body();
        } catch (Exception e) {
            Timber.e(e, "Failed to fetch a result from WordNick");
            throw e;
        }

        for (WordnikDefinition definition : definitions) {
            HashMap<String, Object> propertiesMap = new HashMap<>();

            propertiesMap.put("Part of Speech", definition.partOfSpeech);
            propertiesMap.put("Source", definition.sourceDictionary);
            propertiesMap.put("Meaning", definition.text);

            Result result = new Result(propertiesMap, 1000, Result.DICTIONARY);

            results.add(result);
        }

        return new ResultGroup(curiosity, Source.wordnick, results);
    }

    public class WordnikDefinition {
        private String extendedText;

        private String text;

        private String sourceDictionary;

        private Citations[] citations;

        private Labels[] labels;

        private String score;

        private ExampleUses[] exampleUses;

        private String attributionUrl;

        private String seqString;

        private String attributionText;

        private RelatedWords[] relatedWords;

        private String sequence;

        private String word;

        private TextProns[] textProns;

        private Notes[] notes;

        private String partOfSpeech;

        public String getExtendedText() {
            return extendedText;
        }

        public void setExtendedText(String extendedText) {
            this.extendedText = extendedText;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getSourceDictionary() {
            return sourceDictionary;
        }

        public void setSourceDictionary(String sourceDictionary) {
            this.sourceDictionary = sourceDictionary;
        }

        public Citations[] getCitations() {
            return citations;
        }

        public void setCitations(Citations[] citations) {
            this.citations = citations;
        }

        public Labels[] getLabels() {
            return labels;
        }

        public void setLabels(Labels[] labels) {
            this.labels = labels;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public ExampleUses[] getExampleUses() {
            return exampleUses;
        }

        public void setExampleUses(ExampleUses[] exampleUses) {
            this.exampleUses = exampleUses;
        }

        public String getAttributionUrl() {
            return attributionUrl;
        }

        public void setAttributionUrl(String attributionUrl) {
            this.attributionUrl = attributionUrl;
        }

        public String getSeqString() {
            return seqString;
        }

        public void setSeqString(String seqString) {
            this.seqString = seqString;
        }

        public String getAttributionText() {
            return attributionText;
        }

        public void setAttributionText(String attributionText) {
            this.attributionText = attributionText;
        }

        public RelatedWords[] getRelatedWords() {
            return relatedWords;
        }

        public void setRelatedWords(RelatedWords[] relatedWords) {
            this.relatedWords = relatedWords;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public TextProns[] getTextProns() {
            return textProns;
        }

        public void setTextProns(TextProns[] textProns) {
            this.textProns = textProns;
        }

        public Notes[] getNotes() {
            return notes;
        }

        public void setNotes(Notes[] notes) {
            this.notes = notes;
        }

        public String getPartOfSpeech() {
            return partOfSpeech;
        }

        public void setPartOfSpeech(String partOfSpeech) {
            this.partOfSpeech = partOfSpeech;
        }

        @Override
        public String toString() {
            return "ClassPojo [extendedText = " + extendedText + ", text = " + text + ", sourceDictionary = " + sourceDictionary + ", citations = " + citations + ", labels = " + labels + ", score = " + score + ", exampleUses = " + exampleUses + ", attributionUrl = " + attributionUrl + ", seqString = " + seqString + ", attributionText = " + attributionText + ", relatedWords = " + relatedWords + ", sequence = " + sequence + ", word = " + word + ", textProns = " + textProns + ", notes = " + notes + ", partOfSpeech = " + partOfSpeech + "]";
        }
    }

    public class TextProns {
        private String raw;

        private String seq;

        private String rawType;

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public String getSeq() {
            return seq;
        }

        public void setSeq(String seq) {
            this.seq = seq;
        }

        public String getRawType() {
            return rawType;
        }

        public void setRawType(String rawType) {
            this.rawType = rawType;
        }

        @Override
        public String toString() {
            return "ClassPojo [raw = " + raw + ", seq = " + seq + ", rawType = " + rawType + "]";
        }
    }

    public class Notes {
        private String noteType;

        private String[] appliesTo;

        private String value;

        private String pos;

        public String getNoteType() {
            return noteType;
        }

        public void setNoteType(String noteType) {
            this.noteType = noteType;
        }

        public String[] getAppliesTo() {
            return appliesTo;
        }

        public void setAppliesTo(String[] appliesTo) {
            this.appliesTo = appliesTo;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "ClassPojo [noteType = " + noteType + ", appliesTo = " + appliesTo + ", value = " + value + ", pos = " + pos + "]";
        }
    }

    public class RelatedWords {
        private String label1;

        private String label2;

        private String relationshipType;

        private String label3;

        private String[] words;

        private String label4;

        private String gram;

        public String getLabel1() {
            return label1;
        }

        public void setLabel1(String label1) {
            this.label1 = label1;
        }

        public String getLabel2() {
            return label2;
        }

        public void setLabel2(String label2) {
            this.label2 = label2;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }

        public String getLabel3() {
            return label3;
        }

        public void setLabel3(String label3) {
            this.label3 = label3;
        }

        public String[] getWords() {
            return words;
        }

        public void setWords(String[] words) {
            this.words = words;
        }

        public String getLabel4() {
            return label4;
        }

        public void setLabel4(String label4) {
            this.label4 = label4;
        }

        public String getGram() {
            return gram;
        }

        public void setGram(String gram) {
            this.gram = gram;
        }

        @Override
        public String toString() {
            return "ClassPojo [label1 = " + label1 + ", label2 = " + label2 + ", relationshipType = " + relationshipType + ", label3 = " + label3 + ", words = " + words + ", label4 = " + label4 + ", gram = " + gram + "]";
        }
    }

    public class ExampleUses {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "ClassPojo [text = " + text + "]";
        }
    }


    public class Labels {
        private String text;

        private String type;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "ClassPojo [text = " + text + ", type = " + type + "]";
        }
    }

    public class Citations {
        private String cite;

        private String source;

        public String getCite() {
            return cite;
        }

        public void setCite(String cite) {
            this.cite = cite;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return "ClassPojo [cite = " + cite + ", source = " + source + "]";
        }
    }
}
