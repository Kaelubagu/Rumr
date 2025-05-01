package com.example.rumrapp;

import androidx.annotation.NonNull;

public class Message {
    public String content;
    public int senderId;

   //these should probably be private, but am feeling lazy today
    @NonNull
    @Override
    public String toString(){
        //for testing idk if we'll actually use this or just call fields
        return "Sender: " + senderId + " Message: " + content;
    }
}
