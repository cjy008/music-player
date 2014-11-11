package com.example.c5d6e7.musicplayer;

/**
 * Created by c5d6e7 on 11/8/2014.
 */
public class Comment {
    private long id;
    private String comment;
    private String url;

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getUrl(){return url;}

    public void setURL(String url){this.url = url;}

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String toString(){
        return comment;
    }
}
