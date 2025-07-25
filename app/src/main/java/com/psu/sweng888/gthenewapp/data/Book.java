package com.psu.sweng888.gthenewapp.data;

import java.io.Serializable;

import com.psu.sweng888.gthenewapp.R;

public class Book implements Serializable {

    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private int imageResourceId;
    private String coverImageUri;

    public Book() {
    }

    public Book(String title, String author, String isbn, String publisher) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }

    public Book(String title, String author, String isbn, String publisher, String coverImageUri) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.coverImageUri = coverImageUri;
    }

    //Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getImageResourceId() {
        return R.drawable.ic_book;
    }

    public String getCoverImageUri() {
        return coverImageUri;
    }

    public void setCoverImageUri(String coverImageUri) {
        this.coverImageUri = coverImageUri;
    }
}