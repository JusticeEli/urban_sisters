package com.justice.a2urbansisters;

import com.google.firebase.firestore.DocumentSnapshot;

public class Constants {

    ////used during editing of a stock
    public static DocumentSnapshot documentSnapshot;

    /////////determines if we are logged in as customer or admin
    public static boolean isAdmin = true;

    ///name of collections in firebase////////////
    public static final String STOCKS = "stocks";
    public static final String PERSONAL_ORDERS = "personal_orders";
    public static final String ALL_STOCKS = "all stocks";
    public static final String ADMIN_CUSTOMER = "admin or customer";


}
