package com.codepath.bookself.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.Date;

@Parcel(analyze = UsersBookProgress.class)
@ParseClassName("UsersBookProgress")
public class UsersBookProgress extends ParseObject {

    public static final String KEY_CURRENT_PAGE = "currentPage";
    public static final String KEY_USER = "user";
    public static final String KEY_BOOK = "book";
    public static final String KEY_HEARTED = "hearted";
    public static final String KEY_LAST_READ = "lastRead";
    public static final String KEY_READ = "read";
    public static final String KEY_WISHLIST = "wishlist";

    public UsersBookProgress() {
    }

    // creating getter and setter methods
    public int getCurrentPage() {
        return getInt(KEY_CURRENT_PAGE);
    }

    public void setCurrentPage(int currentPage) {
        put(KEY_CURRENT_PAGE, currentPage);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public BooksParse getBook() {
        return (BooksParse) getParseObject(KEY_BOOK);
    }

    public void setBook(BooksParse book) {
        put(KEY_BOOK, book);
    }

    public boolean getHearted() { return getBoolean(KEY_HEARTED); }

    public void setHearted(boolean hearted) { put(KEY_HEARTED, hearted); }

    public Date getLastRead() { return getDate(KEY_LAST_READ); }

    public void setLastRead(Date lastRead) {put(KEY_LAST_READ, lastRead); }

    public void setRead(boolean read) {put(KEY_READ, read); }

    public boolean getRead() { return getBoolean(KEY_READ); }

    public boolean getWishlist() { return getBoolean(KEY_WISHLIST); }

    public void setWishlist(boolean wishlist) { put(KEY_WISHLIST, wishlist); }

    public void setProgress(int currentPage, ParseUser user, ParseObject book, boolean hearted, boolean wishlist) {

        put(KEY_CURRENT_PAGE, currentPage);
        put(KEY_USER, user);
        put(KEY_BOOK, book);
        put(KEY_HEARTED, hearted);
        put(KEY_WISHLIST, wishlist);
    }
}
