package com.codepath.bookself.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcel;

@Parcel(analyze = Shelves.class)
@ParseClassName("Shelf")
public class Shelves extends ParseObject{
    public static final String KEY_NAME = "name";
    public static final String KEY_PROGRESSES = "progresses";
    public static final String KEY_GOOGLE_ID = "idShelf";
    public static final String KEY_AMOUNT_BOOKS = "amountBooks";
    public static final String KEY_USER = "user";

    public Shelves() {
    }

    public String getNameShelf() {
        return getString(KEY_NAME);
    }

    public void setNameShelf(String name) {
        put(KEY_NAME, name);
    }

    public ParseRelation<UsersBookProgress> getProgresses() { return getRelation(KEY_PROGRESSES);
    }

    public void setRelation(ParseRelation<UsersBookProgress> progress) { put(KEY_PROGRESSES, progress);
    }

    public int getGoogleId() {
        return getInt(KEY_GOOGLE_ID);
    }

    public void setGoogleId(int id) {
        put(KEY_GOOGLE_ID, id);
    }


    public int getAmountBooks() {
        return getInt(KEY_AMOUNT_BOOKS);
    }

    public void setAmountBooks(int amountBooks) {
        put(KEY_AMOUNT_BOOKS, amountBooks);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public void setGoogleShelf(String name, int id, int amountBooks) {

        put(KEY_NAME, name);
        put(KEY_GOOGLE_ID, id);
        put(KEY_AMOUNT_BOOKS, amountBooks);
    }

    public void setParseShelf(String name, int googleId, int amountBooks, ParseUser user) {
        put(KEY_NAME, name);
        put(KEY_GOOGLE_ID, googleId);
        put(KEY_AMOUNT_BOOKS, amountBooks);
        put(KEY_USER, user);
    }
}
