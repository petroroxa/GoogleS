package com.spreadsheets;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.spreadsheets.MainActivity;

import java.io.Serializable;
import java.util.List;

import spreadsheets.test.spreadsheet.R;

/**
 * Created by red on 30.01.2016.
 */

public class DisplaySheet extends Activity implements Serializable{

    TableLayout table_layout;
    List<String> tableString;
    SpreadsheetFeed feed;
    List<SpreadsheetEntry> spreadsheets;
    List<String> sheetlist;
    int position;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table);
        //table_layout = new TableLayout(this);
        if (getIntent().getExtras() != null) {
            for(String a : getIntent().getExtras().getStringArrayList("feed")) {
                Log.e("=======","Data " + a);
            }
        }
    }




    private void BuildTable(int rows, int cols) {
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
    class Task extends AsyncTask<Void, Void, Void> {
        public Void doInBackground(Void... params) {
            try {
           // SpreadsheetFeed feed = MainActivity.feed;
                feed= (SpreadsheetFeed) getIntent().getSerializableExtra("feed");
                for (SpreadsheetEntry spreadsheet : spreadsheets) {
                    Log.i("My App", spreadsheet.getTitle().getPlainText());
                    sheetlist.add(spreadsheet.getTitle().getPlainText());
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return null;
        }

    }



}
