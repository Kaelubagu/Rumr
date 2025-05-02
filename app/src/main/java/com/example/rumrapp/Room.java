package com.example.myapp.models;

public class Room {
    private String id;
    private String name;

    public Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    @Override
    public String toString() {
        return name; // useful for ListView or Spinner
    }
}
