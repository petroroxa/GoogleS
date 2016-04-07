package com.spreadsheets;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;

/**
 * Created by Roxana on 2/25/2016.
 */
public class Service {
    public static SpreadsheetService service;
    public static void setService(SpreadsheetService serv){
        service = serv;
    }
    public static SpreadsheetService getService(){
        return service;
    }
}
