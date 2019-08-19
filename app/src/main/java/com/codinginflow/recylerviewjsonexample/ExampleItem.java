package com.codinginflow.recylerviewjsonexample;

import com.google.android.gms.maps.model.LatLng;

public class ExampleItem {
    private String mImageUrl;
    private String mCreator;
    private String mLikes;
    private String mLat;
    private String mLng;
    private String mCatName;

    public ExampleItem(String imageUrl, String creator, String likes, String lat, String lng, String catName) {
        mImageUrl = imageUrl;
        mCreator = creator;
        mLikes = likes;
        mLat = lat;
        mLng = lng;
        mCatName = catName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getCreator() {
        return mCreator;
    }

    public String getLikeCount() {
        return mLikes;
    }

    public String getLat() {
        return mLat;
    }

    public String getLng() { return mLng; }

    public String getCat(){ return mCatName; }
}