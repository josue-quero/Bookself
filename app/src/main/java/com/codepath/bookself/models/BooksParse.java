package com.codepath.bookself.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Parcel(analyze = BooksParse.class)
@ParseClassName("Book")
public class BooksParse extends ParseObject {

    // creating string, int and array list
    // variables for our book details
    public static final String KEY_TITLE = "title";
    public static final String KEY_SUBTITLE = "subtitle";
    public static final String KEY_AUTHORS = "authors";
    public static final String KEY_PUBLISHER = "publisher";
    public static final String KEY_PUBLISHED_DATE = "publishedDate";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PAGE_COUNT = "pageCount";
    public static final String KEY_THUMBNAIL = "thumbnail";
    public static final String KEY_PREVIEW_LINK = "previewLink";
    public static final String KEY_INFO_LINK = "infoLink";
    public static final String KEY_BUY_LINK = "buyLink";
    public static final String KEY_GOOGLE_ID = "googleId";
    public static final String KEY_EBOOK_ID = "ebookId";
    public static final String KEY_CATEGORIES = "categories";

    public BooksParse() {
    }

    // creating getter and setter methods
    public String getTitle() { return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public String getSubtitle() {
        return getString(KEY_SUBTITLE);
    }

    public void setSubtitle(String subtitle) {
        put(KEY_SUBTITLE, subtitle);
    }

    public ArrayList<String> getAuthors() {
        return new ArrayList<String>(Objects.requireNonNull(getList(KEY_AUTHORS)));
    }

    public void setAuthors(ArrayList<String> authors) {
        put(KEY_AUTHORS, authors);
    }

    public String getPublisher() {
        return getString(KEY_PUBLISHER);
    }

    public void setPublisher(String publisher) {
        put(KEY_PUBLISHER, publisher);
    }

    public String getPublishedDate() {
        return getString(KEY_PUBLISHED_DATE);
    }

    public void setPublishedDate(String publishedDate) {
        put(KEY_PUBLISHED_DATE, publishedDate);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public int getPageCount() { return getInt(KEY_PAGE_COUNT); }

    public void setPageCount(int pageCount) {
        put(KEY_PAGE_COUNT, pageCount);
    }

    public String getThumbnail() {
        return getString(KEY_THUMBNAIL);
    }

    public void setThumbnail(String thumbnail) {
        put(KEY_THUMBNAIL, thumbnail);
    }

    public String getPreviewLink() {
        return getString(KEY_PREVIEW_LINK);
    }

    public void setPreviewLink(String previewLink) {
        put(KEY_PREVIEW_LINK, previewLink);
    }

    public String getInfoLink() {
        return getString(KEY_INFO_LINK);
    }

    public void setInfoLink(String infoLink) {
        put(KEY_INFO_LINK, infoLink);
    }

    public String getBuyLink() {
        return getString(KEY_BUY_LINK);
    }

    public void setBuyLink(String buyLink) {
        put(KEY_BUY_LINK, buyLink);
    }

    public String getGoogleId() { return getString(KEY_GOOGLE_ID); }

    public void setGoogleId(String googleId) { put(KEY_GOOGLE_ID, googleId); }

    public String getEbookId() { return getString(KEY_EBOOK_ID); }

    public void setEbookId(String ebookId) { put(KEY_EBOOK_ID, ebookId); }

    public ArrayList<String> getCategories() {
        return new ArrayList<String>(Objects.requireNonNull(getList(KEY_CATEGORIES)));
    }

    public void setCategories(ArrayList<String> categories) {
        put(KEY_CATEGORIES, categories);
    }

    public void setBook(String title, String subtitle, ArrayList<String> authors, String publisher,
                    String publishedDate, String description, int pageCount, String thumbnail,
                    String previewLink, String infoLink, String buyLink, String googleId, ArrayList<String> categories) {

        put(KEY_TITLE, title);
        put(KEY_SUBTITLE, subtitle);
        put(KEY_AUTHORS, authors);
        put(KEY_PUBLISHER, publisher);
        put(KEY_PUBLISHED_DATE, publishedDate);
        put(KEY_DESCRIPTION, description);
        put(KEY_PAGE_COUNT, pageCount);
        put(KEY_THUMBNAIL, thumbnail);
        put(KEY_PREVIEW_LINK, previewLink);
        put(KEY_INFO_LINK, infoLink);
        put(KEY_BUY_LINK, buyLink);
        put(KEY_GOOGLE_ID, googleId);
        put(KEY_CATEGORIES, categories);
    }
}