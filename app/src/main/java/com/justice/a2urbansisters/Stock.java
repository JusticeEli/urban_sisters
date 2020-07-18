package com.justice.a2urbansisters;

import android.net.Uri;

import com.google.firebase.firestore.DocumentSnapshot;

public class Stock {
    private String id;
    private String name;
    private String imageUrl;
    private long price;
    private boolean delivered;

    public Stock() {
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
