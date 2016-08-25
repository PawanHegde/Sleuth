/*
 * Copyright (c) 2016. Pawan Hegde
 */

package com.storykaar.sleuth.services.downloaders;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.storykaar.sleuth.model.Curiosity;
import com.storykaar.sleuth.model.ResultGroup;
import com.storykaar.sleuth.model.sources.Source;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

/**
 * Created by pawan on 23/3/16.
 * Fetches results from the DuckDuckGo Answers API for the given query
 */
public class DuckDuckGoDownloader
        implements DownloaderFactory.IDownloader {
    private final Source source = Source.duckDuckGo;
    private DuckService duckService;

    //ServiceGenerator.createService("https://api.duckduckgo.com/", DuckService.class);

    @Override
    public ResultGroup download(Curiosity curiosity) throws IOException {
        ResultGroup resultGroup = null;

        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Integer.class, new IntegerDeserializer());
            gsonBuilder.registerTypeAdapter(Infobox.class, new InfoboxDeserialiser());
            GsonConverterFactory converterFactory = GsonConverterFactory.create(gsonBuilder.create());

            Retrofit.Builder builder =
                    new Retrofit.Builder()
                            .baseUrl("https://api.duckduckgo.com/")
                            .addConverterFactory(converterFactory);

            Retrofit retrofit = builder.client(new OkHttpClient.Builder().build()).build();
            duckService = retrofit.create(DuckService.class);
            //duckService = ServiceGenerator.createService("https://api.duckduckgo.com/", DuckService.class);
        } catch (Exception e) {
            Timber.e(e, "Error creating duck service");
            throw e;
        }

        try {
            Timber.d("Requesting url: %s", duckService.fetch(curiosity.query, "json", 1, 1).request().url());
            Timber.d("About to execute a duck");

            DuckObject duckObject = duckService.fetch(curiosity.query, "json", 1, 1).execute().body();

            //Timber.d("Obtained Duck Object: %s", duckObject);

            if (duckObject.meta == null) {
                Timber.i("Duckduckgo returned nothing for %s", curiosity.query);
                return new ResultGroup(curiosity, source, new HashSet<com.storykaar.sleuth.model.Result>(0));
            }

            resultGroup = new ResultGroup(curiosity, source, new HashSet<com.storykaar.sleuth.model.Result>());

            Timber.d("Object Type: %s", duckObject.Type);
            if ("A".equals(duckObject.Type)) {
                HashMap<String, Object> propertyMap = new HashMap<>();

                propertyMap.put("Heading", duckObject.Heading);
                propertyMap.put("Entity", duckObject.Entity);
                propertyMap.put("Text", duckObject.AbstractText);

                for (Result result : duckObject.Results) {
                    propertyMap.put(result.Text, result.FirstURL);
                    Timber.d("Setting [%s : %s]", result.Text, result.FirstURL);
                }

                Timber.d("Added this as a new message");
                Timber.d("Infobox is %s", duckObject.Infobox);
                if (duckObject.Infobox != null && duckObject.Infobox.content != null) {
                    for (Content content : duckObject.Infobox.content) {
                        // TODO: Figure out how to handle dynamic datatypes
                        // TODO: Handle empty string results
                        try {
                            propertyMap.put(content.label, String.valueOf(content.value));
                        } catch (Exception e) {
                            Timber.e(e, "Failed trying to add the property %s from infobox", content.label);
                        }
                    }
                }

                propertyMap.put("Source URL", duckObject.AbstractURL);
                propertyMap.put("Source", duckObject.AbstractSource);

                com.storykaar.sleuth.model.Result result = new com.storykaar.sleuth.model.Result(propertyMap, 1000, com.storykaar.sleuth.model.Result.ANSWER, duckObject.Image);

                resultGroup.add(result);
            } else {
                Timber.d("duckObject Type is [%s]", duckObject.Type);
            }

            Timber.d("Related queries %s", duckObject.RelatedTopics);
            for (RelatedTopic relatedTopic : duckObject.RelatedTopics) {
                HashMap<String, Object> propertyMap = new HashMap<>();
                com.storykaar.sleuth.model.Result result;

                if (relatedTopic.Name != null && relatedTopic.Topics != null) {
                    // This 'Related Topic' is actually a list of topics
                    Timber.d("Deserializing Topic [%s]", relatedTopic.Name);
                    String topicName = relatedTopic.Name;
                    for (Topic topic : relatedTopic.Topics) {
                        propertyMap.put("Topic", topicName);
                        propertyMap.put("Text", topic.Text);
                        propertyMap.put("URL", topic.FirstURL);

                        if (topic.Icon != null && topic.Icon.URL != null) {
                            result = new com.storykaar.sleuth.model.Result(propertyMap, 300, com.storykaar.sleuth.model.Result.GENERAL, topic.Icon.URL);
                        } else {
                            result = new com.storykaar.sleuth.model.Result(propertyMap, 300, com.storykaar.sleuth.model.Result.GENERAL);
                        }
                        resultGroup.add(result);
                    }
                } else {
                    propertyMap.put("Text", relatedTopic.Text);
                    propertyMap.put("URL", relatedTopic.FirstURL);

                    if (relatedTopic.Icon != null && relatedTopic.Icon.URL != null) {
                        result = new com.storykaar.sleuth.model.Result(propertyMap, 500, com.storykaar.sleuth.model.Result.GENERAL, relatedTopic.Icon.URL);
                    } else {
                        result = new com.storykaar.sleuth.model.Result(propertyMap, 500, com.storykaar.sleuth.model.Result.GENERAL);
                    }

                    resultGroup.add(result);
                }
            }
        } catch (IOException e) {
            Timber.e(e, "Encountered an issue in DuckDuckGoDownloader");
            throw e;
        } catch (Exception e) {
            Timber.e(e, "Caught an exception of type %s", e.getClass().getName());
        }

        return resultGroup;
    }

    interface DuckService {
        @GET("/")
        Call<DuckObject> fetch(@Query("q") String query,
                               @Query("format") String format,
                               @Query("skip_disambig") Integer skipDisambig,
                               @Query("no_redirect") Integer noRedirect);
    }
}

/*class InfoboxAdapter extends TypeAdapter<Infobox> {

    final Gson embedded = new Gson();

    @Override
    public void write(JsonWriter out, Infobox infobox) throws IOException {
        if (infobox == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        out.name("content");
        embedded.toJson(embedded.toJsonTree(infobox.content), out);

        out.name("meta");
        embedded.toJson(embedded.toJsonTree(infobox.meta), out);
        out.endObject();

        *//*out.beginArray();

        for (Content content: infobox.content) {
            out.beginObject();
            out.name("data_type").value(content.dataType);
            out.name("value").value(content.value);
            out.name("label").value(content.label);
            out.name("wiki_order").value(content.wikiOrder);
            out.endObject();
        }

        out.endArray();

        out.name("meta");*//*
    }

    @Override
    public Infobox read(JsonReader in) throws IOException {
        if ("\"".equals(in.peek())) {
            return null;
        }

        Timber.d("Infobox contains: %s", in.peek());
        System.out.println("Infobox contains: " + in.peek());

        return embedded.fromJson(in, Infobox.class);
    }
}*/

class IntegerDeserializer implements JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return primitive.getAsInt();
            }

            return 0;
        } catch (Exception e) {
            Timber.d(e, "Failed to deserialize. Returning 0");
            return 0;
        }
    }
}

class InfoboxDeserialiser implements JsonDeserializer<Infobox> {

    @Override
    public Infobox deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        try {
            if (json.isJsonNull() || json.isJsonPrimitive()) {
                return null;
            }

            JsonObject jsonObject = json.getAsJsonObject();

            Infobox infobox = new Infobox();

            JsonArray jsonContent = jsonObject.get("content").getAsJsonArray();
            JsonArray jsonMeta = jsonObject.get("meta").getAsJsonArray();

            infobox.content = new Content[jsonContent.size()];
            for (int i = 0; i < jsonContent.size(); i++) {
                infobox.content[i] = context.deserialize(jsonContent.get(i), Content.class);
            }

            infobox.meta = new Metum[jsonMeta.size()];
            for (int i = 0; i < jsonMeta.size(); i++) {
                infobox.meta[i] = context.deserialize(jsonContent.get(i), Metum.class);
            }

            return infobox;
        } catch (Exception e) {
            Timber.e(e, "Failed to deserialise the infobox");
            return null;
        }
    }
}

class DuckObject {
    @Attribute(name="version", required = false)
    public String version;
    @Element(name = "DefinitionSource", required = false)
    public String DefinitionSource;
    @Element(name = "Heading", required = false)
    public String Heading;
    @Element(name = "ImageWidth", required = false)
    public Integer ImageWidth;
    @Element(name = "RelatedTopics", required = false, type = RelatedTopic[].class)
    public RelatedTopic[] RelatedTopics;
    @Element(name = "Entity", required = false)
    public String Entity;
    @Element(name = "meta", required = false)
    public Meta meta;
    @Element(name = "Type", required = false)
    public String Type;
    @Element(name = "Redirect", required = false)
    public String Redirect;
    @Element(name = "DefinitionURL", required = false)
    public String DefinitionURL;
    @Element(name = "Abstract", required = false)
    public String Abstract;
    @Element(name = "AbstractURL", required = false)
    public String AbstractURL;
    @Element(name = "Definition", required = false)
    public String Definition;
    @Element(name = "AbstractSource", required = false)
    public String AbstractSource;
    @Element(name = "Infobox", type = Infobox.class, required = false)
    public Infobox Infobox;
    @Element(name = "Image", required = false)
    public String Image;
    @Element(name = "ImageIsLogo", required = false)
    public Integer ImageIsLogo;
    @Element(name = "AbstractText", required = false)
    public String AbstractText;
    @Element(name = "AnswerType", required = false)
    public String AnswerType;
    @Element(name = "ImageHeight", required = false)
    public Integer ImageHeight;
    @Element(name = "Answer", required = false)
    public String Answer;
    @Element(name = "Results", required = false, type = ArrayList.class)
    public ArrayList<Result> Results;

    @Override
    public String toString() {
        return "DuckObject{" +
                "version='" + version + '\'' +
                ", DefinitionSource='" + DefinitionSource + '\'' +
                ", Heading='" + Heading + '\'' +
                ", ImageWidth=" + ImageWidth +
                ", RelatedTopics=" + Arrays.toString(RelatedTopics) +
                ", Entity='" + Entity + '\'' +
                ", meta=" + meta +
                ", Type='" + Type + '\'' +
                ", Redirect='" + Redirect + '\'' +
                ", DefinitionURL='" + DefinitionURL + '\'' +
                ", Abstract='" + Abstract + '\'' +
                ", AbstractURL='" + AbstractURL + '\'' +
                ", Definition='" + Definition + '\'' +
                ", AbstractSource='" + AbstractSource + '\'' +
                ", Infobox=" + Infobox +
                ", Image='" + Image + '\'' +
                ", ImageIsLogo=" + ImageIsLogo +
                ", AbstractText='" + AbstractText + '\'' +
                ", AnswerType='" + AnswerType + '\'' +
                ", ImageHeight=" + ImageHeight +
                ", Answer='" + Answer + '\'' +
                ", Results=" + Results +
                '}';
    }
}

class RelatedTopic {
    @Element(name = "Result", required = false)
    public String Result;
    @Element(name = "Icon", required = false)
    public Icon Icon;
    @Element(name = "FirstURL", required = false)
    public String FirstURL;
    @Element(name = "Text", required = false)
    public String Text;
    @Element(name = "Topics", required = false)
    public Topic[] Topics;
    @Element(name = "Name", required = false)
    public String Name;

    @Override
    public String toString() {
        return "RelatedTopic{" +
                "Result='" + Result + '\'' +
                ", Icon=" + Icon +
                ", FirstURL='" + FirstURL + '\'' +
                ", Text='" + Text + '\'' +
                ", Topics=" + Arrays.toString(Topics) +
                ", Name='" + Name + '\'' +
                '}';
    }
}

class Topic {
    @Element(name = "Result", required = false)
    public String Result;
    @Element(name = "Icon", required = false)
    public Icon Icon;
    @Element(name = "FirstURL", required = false)
    public String FirstURL;
    @Element(name = "Text", required = false)
    public String Text;

    @Override
    public String toString() {
        return "Topic{" +
                "Result='" + Result + '\'' +
                ", Icon=" + Icon +
                ", FirstURL='" + FirstURL + '\'' +
                ", Text='" + Text + '\'' +
                '}';
    }
}

class Result {
    @Element(name = "Result", required = false)
    public String Result;
    @Element(name = "Icon", required = false)
    public Icon Icon;
    @Element(name = "FirstURL", required = false)
    public String FirstURL;
    @Element(name = "Text", required = false)
    public String Text;

    @Override
    public String toString() {
        return "Result{" +
                "Result='" + Result + '\'' +
                ", Icon=" + Icon +
                ", FirstURL='" + FirstURL + '\'' +
                ", Text='" + Text + '\'' +
                '}';
    }
}

class Developer {
    @Element(name = "url", required = false)
    public String url;
    @Element(name = "name", required = false)
    public String name;
    @Element(name = "type", required = false)
    public String type;

    @Override
    public String toString() {
        return "Developer{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

class Icon {
    @Element(name = "URL", required = false)
    public String URL;
    @Element(name = "Height", required = false)
    public String Height;
    @Element(name = "Width", required = false)
    public String Width;

    @Override
    public String toString() {
        return "Icon{" +
                "URL='" + URL + '\'' +
                ", Height='" + Height + '\'' +
                ", Width='" + Width + '\'' +
                '}';
    }
}

class Infobox {
    @Element(name = "content", type = Content[].class, required = false)
    public Content[] content;
    @Element(name = "meta", type = Metum[].class, required = false)
    public Metum[] meta;

    @Override
    public String toString() {
        return "Infobox{" +
                "content=" + Arrays.toString(content) +
                ", meta=" + Arrays.toString(meta) +
                '}';
    }
}

class Content {
    @Element(name = "dataType", required = false)
    public String dataType;
    @Element(name = "value", required = false)
    public String value;
    @Element(name = "label", required = false)
    public String label;
    @Element(name = "wikiOrder", required = false)
    public Integer wikiOrder;

    @Override
    public String toString() {
        return "Content{" +
                "dataType='" + dataType + '\'' +
                ", value='" + value + '\'' +
                ", label='" + label + '\'' +
                ", wikiOrder=" + wikiOrder +
                '}';
    }
}

class Metum {
    @Element(name = "dataType", required = false)
    public String dataType;
    @Element(name = "value", required = false)
    public String value;
    @Element(name = "label", required = false)
    public String label;

    @Override
    public String toString() {
        return "Metum{" +
                "dataType='" + dataType + '\'' +
                ", value='" + value + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}

class Maintainer {
    @Element(name = "github", required = false)
    public String github;

    @Override
    public String toString() {
        return "Maintainer{" +
                "github='" + github + '\'' +
                '}';
    }
}

class Meta {
    @Element(name = "maintainer", required = false)
    public Maintainer maintainer;
    @Element(name = "perlModule", required = false)
    public String perlModule;
    @Element(name = "status", required = false)
    public String status;
    @Element(name = "productionState", required = false)
    public String productionState;
    @Element(name = "devDate", required = false)
    public Object devDate;
    @Element(name = "jsCallbackName", required = false)
    public String jsCallbackName;
    @Element(name = "signalFrom", required = false)
    public String signalFrom;
    @Element(name = "liveDate", required = false)
    public Object liveDate;
    @Element(name = "srcId", required = false)
    public Integer srcId;
    @Element(name = "srcOptions", required = false)
    public SrcOptions srcOptions;
    @Element(name = "repo", required = false)
    public String repo;
    public List<Developer> developer = new ArrayList<>();
    @Element(name = "tab", required = false)
    public String tab;
    @Element(name = "producer", required = false)
    public Object producer;
    @Element(name = "unsafe", required = false)
    public Integer unsafe;
    @Element(name = "id", required = false)
    public String id;
    @Element(name = "devMilestone", required = false)
    public String devMilestone;
    public List<String> topic = new ArrayList<>();
    @Element(name = "name", required = false)
    public String name;
    @Element(name = "attribution", required = false)
    public Object attribution;
    @Element(name = "createdDate", required = false)
    public Object createdDate;
    @Element(name = "exampleQuery", required = false)
    public String exampleQuery;
    @Element(name = "description", required = false)
    public String description;
    @Element(name = "isStackexchange", required = false)
    public Object isStackexchange;
    @Element(name = "designer", required = false)
    public Object designer;
    @Element(name = "srcDomain", required = false)
    public String srcDomain;
    @Element(name = "srcName", required = false)
    public String srcName;
    @Element(name = "blockgroup", required = false)
    public Object blockgroup;
    @Element(name = "srcUrl", required = false)
    public Object srcUrl;

    @Override
    public String toString() {
        return "Meta{" +
                "maintainer=" + maintainer +
                ", perlModule='" + perlModule + '\'' +
                ", status='" + status + '\'' +
                ", productionState='" + productionState + '\'' +
                ", devDate=" + devDate +
                ", jsCallbackName='" + jsCallbackName + '\'' +
                ", signalFrom='" + signalFrom + '\'' +
                ", liveDate=" + liveDate +
                ", srcId=" + srcId +
                ", srcOptions=" + srcOptions +
                ", repo='" + repo + '\'' +
                ", developer=" + developer +
                ", tab='" + tab + '\'' +
                ", producer=" + producer +
                ", unsafe=" + unsafe +
                ", id='" + id + '\'' +
                ", devMilestone='" + devMilestone + '\'' +
                ", topic=" + topic +
                ", name='" + name + '\'' +
                ", attribution=" + attribution +
                ", createdDate=" + createdDate +
                ", exampleQuery='" + exampleQuery + '\'' +
                ", description='" + description + '\'' +
                ", isStackexchange=" + isStackexchange +
                ", designer=" + designer +
                ", srcDomain='" + srcDomain + '\'' +
                ", srcName='" + srcName + '\'' +
                ", blockgroup=" + blockgroup +
                ", srcUrl=" + srcUrl +
                '}';
    }
}

class SrcOptions {
    @Element(name = "skipEnd", required = false)
    public String skipEnd;
    @Element(name = "skipAbstract", required = false)
    public Integer skipAbstract;
    @Element(name = "skipQr", required = false)
    public String skipQr;
    @Element(name = "language", required = false)
    public String language;
    @Element(name = "skipIcon", required = false)
    public Integer skipIcon;
    @Element(name = "skipImageName", required = false)
    public Integer skipImageName;
    @Element(name = "directory", required = false)
    public String directory;
    @Element(name = "minAbstractLength", required = false)
    public String minAbstractLength;
    @Element(name = "skipAbstractParen", required = false)
    public Integer skipAbstractParen;
    @Element(name = "isWikipedia", required = false)
    public Integer isWikipedia;
    @Element(name = "sourceSkip", required = false)
    public String sourceSkip;
    @Element(name = "isFanon", required = false)
    public Integer isFanon;
    @Element(name = "isMediawiki", required = false)
    public Integer isMediawiki;
    @Element(name = "srcInfo", required = false)
    public String srcInfo;

    @Override
    public String toString() {
        return "SrcOptions{" +
                "skipEnd='" + skipEnd + '\'' +
                ", skipAbstract=" + skipAbstract +
                ", skipQr='" + skipQr + '\'' +
                ", language='" + language + '\'' +
                ", skipIcon=" + skipIcon +
                ", skipImageName=" + skipImageName +
                ", directory='" + directory + '\'' +
                ", minAbstractLength='" + minAbstractLength + '\'' +
                ", skipAbstractParen=" + skipAbstractParen +
                ", isWikipedia=" + isWikipedia +
                ", sourceSkip='" + sourceSkip + '\'' +
                ", isFanon=" + isFanon +
                ", isMediawiki=" + isMediawiki +
                ", srcInfo='" + srcInfo + '\'' +
                '}';
    }
}