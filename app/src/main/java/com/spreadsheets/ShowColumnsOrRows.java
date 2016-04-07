package com.spreadsheets;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

/**
 * Created by Roxana on 2/26/2016.
 */
public class ShowColumnsOrRows extends Activity {
    List<String> tableString;
    TableLayout table_layout;
    String spreadsheetName;
    String worksheetName;

    int cols;
    int rows;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_cols_rows);
        table_layout = new TableLayout(this);
        Intent intent = getIntent();
        spreadsheetName = null;
        worksheetName = null;
        if (intent.getExtras() != null) {
            spreadsheetName = intent.getStringExtra("Spreadsheet name");
            worksheetName = intent.getStringExtra("Worksheet name");
        }

        Log.i("Spreadsheet name", spreadsheetName);
        Log.i("Worksheet name", worksheetName);
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
                cellFeedUrl = new URI(worksheet.getCellFeedUrl().toString() + "?return-empty=true").toURL();
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
            tableString = new ArrayList<>();
            int index = 0;
            for (CellEntry cell : cellFeed.getEntries()) {
                String value = cell.getCell().getValue();
                if(value == null){
                    value = "";
                    tableString.add(index, value);
                }else{
                    tableString.add(index, value);}
                Log.i("Index", String.valueOf(index));
                Log.i("Cell", value);
                index++;
            }
            cols = worksheet.getColCount();
            rows = worksheet.getRowCount();
            Log.i("Application", tableString.toString());
            Log.i("Cols", String.valueOf(cols));
            Log.i("Rows", String.valueOf(rows));
            Log.i("Table String", tableString.toString());
            return tableString;
        }

        protected void onPostExecute(List<String> result) {
            BuildTable(rows, cols, result);
        }

    }
    private void BuildTable(int rows, int cols, List<String> tableString) {

        int index = 0;
        // outer for loop
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        table_layout.setLayoutParams(params);
        for (int i = 0; i < rows; i++) {

            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

            // inner for loop
            for (int j = 0; j < cols; j++) {

                TextView tv = new TextView(this);
                tv.setLayoutParams(lp);
                tv.setText(tableString.get(index));
                index++;
                row.addView(tv);
                Log.d("Index", String.valueOf(index));
                Log.d("Size of list", String.valueOf(tableString.size()));
//                row.addView(tv);
            }
            Log.d("Size of list", String.valueOf(tableString.size()));
            table_layout.addView(row);
        }
        LinearLayout main = (LinearLayout) findViewById(R.id.table);
        main.addView(table_layout);
    }

}
