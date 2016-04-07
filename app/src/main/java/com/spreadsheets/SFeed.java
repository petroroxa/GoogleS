package com.spreadsheets;

import com.google.gdata.data.spreadsheet.SpreadsheetFeed;

import java.util.List;

/**
 * Created by Roxana on 2/22/2016.
 */
public class SFeed {
    public static SpreadsheetFeed feed;
    public static void setFeed(SpreadsheetFeed f){
        feed = f;
    }
    public static SpreadsheetFeed getFeed(){
        return feed;
    }
}
