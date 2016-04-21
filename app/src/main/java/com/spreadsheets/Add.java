package com.spreadsheets;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spreadsheets.test.spreadsheet.R;

public class Add extends Activity {
    List<String> tableString = new ArrayList<String>();
    List<String> inputString = new ArrayList<String>();
    TableLayout table_layout;
    Map<String, String> values = new HashMap<>();
    int index = 0;
    String spreadsheetName;
    String worksheetName;
    WorksheetEntry worksheetEntry = null;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table);
        table_layout = new TableLayout(this);

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
                    worksheetEntry = worksheets.get(worksheets.indexOf(wksheet));
                }
            }
            URL cellFeedUrl = null;
            try {
                cellFeedUrl = new URI(worksheetEntry.getCellFeedUrl().toString() + "?min-row=1&max-row=1").toURL();

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
                tableString.add(value);
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
            BuildLayout();

        }
    }

    class Task2 extends AsyncTask <Void, Void, Void>{
        WorksheetEntry worksheetEntry;
        public Task2(WorksheetEntry worksheetEntry){
            this.worksheetEntry = worksheetEntry;
        }
        protected Void doInBackground(Void... params) {

            Log.i("String:", tableString.toString());
            URL listFeedUrl = worksheetEntry.getListFeedUrl();
            ListEntry row = createRow(values);
            try {
                Service.getService().insert(listFeedUrl, row);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Row", "Row inserted!");

        }

        private ListEntry createRow(Map<String, String> rowValues) {
            ListEntry row = new ListEntry();
            for (String columnName : rowValues.keySet()) {
                String value = rowValues.get(columnName);
                row.getCustomElements().setValueLocal(columnName,
                        value);
            }
            return row;
        }


    }


    private void BuildLayout() {

        index = 0;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        table_layout.setLayoutParams(params);
        final List<EditText> etichete = new ArrayList<EditText>();
        for (int i = 0; i < tableString.size(); i++) {

            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

                TextView tv = new TextView(this);
                tv.setLayoutParams(lp);
                tv.setText(tableString.get(index));

                row.addView(tv);
                EditText et = new EditText(this);
                et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                etichete.add(et);
                row.addView(etichete.get(index));
                Log.d("Index", String.valueOf(index));
                Log.d("Size of list", String.valueOf(tableString.size()));
                Log.d("Size of list", String.valueOf(tableString.size()));
                table_layout.addView(row);
            index++;
        }
        Button b = new Button(this);
        b.setText("Insereaza");
        table_layout.addView(b);
        LinearLayout main = (LinearLayout) findViewById(R.id.table);
        main.addView(table_layout);

        b.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     for (EditText eticheta : etichete) {
                                         inputString.add(eticheta.getText().toString());
                                     }
                                     for (int i = 0; i < tableString.size(); i++) {
                                         values.put(tableString.get(i), inputString.get(i));
                                     }
                                     inputString.clear();
                                     new Task2(worksheetEntry).execute();
                                 }
                             }
        );

    }
}
