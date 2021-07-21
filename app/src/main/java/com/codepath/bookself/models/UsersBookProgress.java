package com.codepath.bookself.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.parceler.Parcel;

@Parcel(analyze = UsersBookProgress.class)
@ParseClassName("UsersBookProgress")
public class UsersBookProgress extends ParseObject {

    public static final String KEY_CURRENT_PAGE = "currentPage";
    public static final String KEY_USER = "user";
    public static final String KEY_BOOK = "book";

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

    public void setProgress(int currentPage, ParseUser user, ParseObject book) {

        put(KEY_CURRENT_PAGE, currentPage);
        put(KEY_USER, user);
        put(KEY_BOOK, book);
    }
}
