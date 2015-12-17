package com.aetoslabs.quickfacts;

/**
 * Created by anthony on 13/12/15.
 */
public class SearchResult {
    protected String content;
    protected String id;

    public SearchResult(String content, String id){
        this.content = content;
        this.id = id;
    }

    public SearchResult(String content){
        this.content = content;
        this.id = null;
    }

}
