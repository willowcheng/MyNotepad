package com.parse.starter.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.UUID;

@ParseClassName("Note")
public class Note extends ParseObject {

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getBody() {
        return getString("body");
    }

    public void setBody(String body) {
        put("body", body);
    }

    public String getLongitude() {
        return getString("longitude");
    }

    public void setLongitude(String longitude) {
        put("longitude", longitude);
    }

    public String getLatitude() {
        return getString("latitude");
    }

    public void setLatitude(String latitude) {
        put("latitude", latitude);
    }

    public ParseUser getAuthor() {
        return getParseUser("author");
    }

    public void setAuthor(ParseUser currentUser) {
        put("author", currentUser);
    }

    public boolean isDraft() {
        return getBoolean("isDraft");
    }

    public void setDraft(boolean isDraft) {
        put("isDraft", isDraft);
    }

    public void setUuidString() {
        UUID uuid = UUID.randomUUID();
        put("uuid", uuid.toString());
    }

    public String getUuidString() {
        return getString("uuid");
    }

    public static ParseQuery<Note> getQuery() {
        return ParseQuery.getQuery(Note.class);
    }
}