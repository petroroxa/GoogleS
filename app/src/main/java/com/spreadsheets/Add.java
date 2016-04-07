package com.spreadsheets;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;

import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import spreadsheets.test.spreadsheet.R;

public class Add extends Activity {
    List<String> tableString;
    int index = 0;
    String spreadsheetName;
    String worksheetName;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table);

        Intent intent = getIntent();
        spreadsheetName = null;
        worksheetName = null;
        if (intent.getExtras() != null) {
            spreadsheetName = intent.getStringExtra("Sp name");
            worksheetName = intent.getStringExtra("Wk name");
        }
        Log.i("Sp name", spreadsheetName);
        Log.i("Wk name", worksheetName);
        new Task().execute();
    }
    class Task extends AsyncTask<Void, Void, List<String>> {

        public List<String> doInBackground(Void... params) {
            List<SpreadsheetEntry> spreadsheets = SFeed.getFeed().getEntries();
            SpreadsheetEntry spreadsheet = null;
            for (SpreadsheetEntry spsheet : spreadsheets) {
                if (spsheet.getTitle().getPlainText().equals(spreadsheetName)) {
                    spreadsheet = spreadsheets.get(spreadsheets.indexOf(spsheet));
                }
            }
            WorksheetEntry worksheet = null;
            List<WorksheetEntry> worksheets = null;
            try {
                worksheets = spreadsheet.getWorksheets();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            for (WorksheetEntry wksheet : worksheets) {
                if (wksheet.getTitle().getPlainText().equals(worksheetName)) {
                    worksheet = worksheets.get(worksheets.indexOf(wksheet));
                }
            }
            URL cellFeedUrl = null;
            try {
                cellFeedUrl = new URI(worksheet.getCellFeedUrl().toString() + "?min-row=1&max-row=1").toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
            CellFeed cellFeed = null;
            try {
                cellFeed = Service.getService().getFeed(cellFeedUrl, CellFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            for (CellEntry cell : cellFeed.getEntries()) {
                String value = cell.getCell().getValue();
                if (value == null) {
                    value = "";

                } else {

                    Log.i("Index", String.valueOf(index));
                    Log.i("Cell", value);
                    index++;
                }
            }
                return tableString;
        }
        protected void onPostExecute(List<String> result) {

        }

    }
}
