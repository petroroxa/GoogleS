package com.spreadsheets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.RecordQuery;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.Field;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.RecordEntry;
import com.google.gdata.data.spreadsheet.RecordFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.TableEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import spreadsheets.test.spreadsheet.R;

/**
 * Created by Roxana on 4/9/2016.
 */
public class Sort extends Activity implements AdapterView.OnItemSelectedListener {
    private String spreadsheetName;
    private String worksheetName;
    private WorksheetEntry worksheetEntry;
    private List<String> tableString = new ArrayList<>();
    private String range;
    private int colNum;
    boolean asc, desc;
    ArrayAdapter<CharSequence> sortAdapter;
    ArrayAdapter<String> colAdapter;
    ArrayAdapter<CharSequence> valueAdapter;
    TableLayout table_layout;
    ArrayList<String> column_tags = new ArrayList<>();
    private String queryColumn = null;
    LinearLayout main;
    private int tableId;
    private URL recordsFeedUrl;
    private String baseUrl;
    private String spreadsheetKey;
    SpreadsheetEntry spreadsheet = null;
    private Map<String, RecordEntry> entriesCached = new HashMap<String, RecordEntry>();
    URL listFeedUrl = null;
    Spinner sortSpinner;
    Spinner colSpinner;
    Spinner valueSpinner;
    String option= null;
    EditText queryValue;
    String queryText;
    String operator = "=";
    String value =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sort);
        table_layout = new TableLayout(this);


        sortSpinner = (Spinner) findViewById(R.id.sort_spinner);
        sortSpinner.setOnItemSelectedListener(this);
        sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);


        colSpinner = (Spinner) findViewById(R.id.col_spinner);
        colSpinner.setOnItemSelectedListener(this);
        colAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, column_tags);
        colAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colSpinner.setAdapter(colAdapter);

        valueSpinner = (Spinner) findViewById(R.id.value_spinner);
        valueSpinner.setOnItemSelectedListener(this);
        valueAdapter = ArrayAdapter.createFromResource(this, R.array.value_pick, android.R.layout.simple_spinner_item);
        valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        valueSpinner.setAdapter(valueAdapter);

        queryValue = (EditText) findViewById(R.id.queries);

        Intent intent = getIntent();
        spreadsheetName = null;
        worksheetName = null;
        if (intent.getExtras() != null) {
            spreadsheetName = intent.getStringExtra("Sp name");
            worksheetName = intent.getStringExtra("Wk name");
        }
        Log.i("Sp name", spreadsheetName);
        Log.i("Wk name", worksheetName);

        Button b = (Button) findViewById(R.id.button);

        new Task1().execute();
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(main!=null){
                    table_layout.removeView(main);
                }
                if(valueSpinner.getSelectedItem().toString().equals("Equals to")){
                    operator = "=";
                }else if(valueSpinner.getSelectedItem().toString().equals("Greater than")){
                    operator = ">";
                }else if(valueSpinner.getSelectedItem().toString().equals("Smaller than")){
                    operator = "<";
                }
                if(option.equals("Specify query")){
                    queryText = queryValue.getText().toString();
                    if (asc) {
                        new Task4().execute();
                    }
                    if (desc) {
                        new Task5().execute();
                    }
                    Log.i("query" , queryText);
                }else if(option.equals("Search in table value")) {
                    queryText =  queryValue.getText().toString();
                    if (asc) {
                        new Task6().execute();
                    }
                    if (desc) {
                        new Task7().execute();
                    }
                    Log.i("query" , queryText);
                }else
                {
                        value = queryValue.getText().toString();
                        queryText = queryColumn.toLowerCase() + operator + value;
                        Log.i("query" , queryText);

                        if (asc) {
                            new Task2().execute();
                        }
                        if (desc) {
                            new Task3().execute();
                        }
                    }
                }



        });

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.asc:
                if (checked)
                    asc = true;
                break;
            case R.id.desc:
                if (checked)
                    desc = true;
                    break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        if(sortSpinner.getSelectedItem().toString().equals("Sort by column") ){

            colSpinner.setVisibility(View.VISIBLE);
            valueSpinner.setVisibility(View.VISIBLE);
            queryColumn = colSpinner.getSelectedItem().toString();
            option = colSpinner.getSelectedItem().toString();
            Log.i("option spinner", option);



        } else
        {
            queryValue.setVisibility(View.VISIBLE);
            colSpinner.setVisibility(View.INVISIBLE);
            valueSpinner.setVisibility(View.INVISIBLE);
            option = sortSpinner.getSelectedItem().toString();
            Log.i("option sort", option );
        }

        if(valueSpinner.getSelectedItem().toString().equals("Equals to")){
            operator = "=";
        }else if(valueSpinner.getSelectedItem().toString().equals("Greater than")){
            operator = ">";
        }else if(valueSpinner.getSelectedItem().toString().equals("Smaller than")){
            operator = "<";
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    class Task1 extends AsyncTask<Void, Void, List<String>> {

        public List<String> doInBackground(Void... params) {
            List<SpreadsheetEntry> spreadsheets = SFeed.getFeed().getEntries();
            SpreadsheetEntry spreadsheet = null;
            int index = 0;
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
            URL cellFeedUrl_Column_Names = null;
            try {
                cellFeedUrl_Column_Names = new URI(worksheetEntry.getCellFeedUrl().toString()
                        + "?min-row=1&max-row=1").toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            CellFeed cellFeed_Column_Names = null;
            try {
                cellFeed_Column_Names  = Service.getService().getFeed(cellFeedUrl_Column_Names, CellFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            for (CellEntry cell : cellFeed_Column_Names.getEntries()) {
                String value =  cell.getCell().getValue();
                column_tags.add(value);
            }
            return column_tags;
        }

        protected void onPostExecute(List<String> result) {
            colAdapter.notifyDataSetChanged();
        }
    }

    class Task2 extends AsyncTask<Void, Void, List<String>> {
        int rows, cols;
        ListFeed listFeed = null;
        public List<String> doInBackground(Void... params) {
            try {
                listFeedUrl = new URI(worksheetEntry.getListFeedUrl().toString()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ListQuery listQuery = new ListQuery(listFeedUrl);

            listQuery.setSpreadsheetQuery(queryText);
            listQuery.setSortColumn(queryColumn.toLowerCase());
            listQuery.getOrderBy();
            Log.i("param 1", listQuery.getOrderBy());
            listFeed = null;
            try {
                listFeed = Service.getService().query(listQuery, ListFeed.class);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            if(listFeed.getEntries().size()!=0){
                for (ListEntry row : listFeed.getEntries()) {
                    for (String tag : row.getCustomElements().getTags()) {
                        Log.i("row.getCustomElements", row.getCustomElements().getValue(tag) + "\t");
                        tableString.add(row.getCustomElements().getValue(tag));
                    }
                }
                rows = listFeed.getEntries().size();
                cols = listFeed.getEntries().get(0).getCustomElements().getTags().size();
                Log.i("row numbers", String.valueOf(rows));
                Log.i("col numbers", String.valueOf(cols));
            }

         /*   for (ListEntry row : listFeed.getEntries()) {
                Map<String, Object> rowValues = getRowData(row);
                for (Map.Entry<String, Object> entry : rowValues.entrySet()) {
                    Log.i("vals:", "Key = " + entry.getKey() + ", Value = " + entry.getValue());
                }


                Log.i("row.getTitle:",  + "\t");
                // Iterate over the remaining columns, and print each cell value
                for (String tag : row.getCustomElements().getTags()) {
                    Log.i("row.getCustomElements", row.getCustomElements().getValue(tag) + "\t");
                }
            }*/
            /*cols = cellFeed.getColCount();
            rows = cellFeed.getRowCount() - 1;
            for (CellEntry cell : cellFeed.getEntries()) {
                String value = cell.getCell().getValue();

                if (value == null || value.isEmpty()) {
                    value = "";

                } else {

                    Log.i("Index", String.valueOf(index));
                    Log.i("Cell", value);

                    index++;
                }
                tableString.add(value);
            }*/
            /*try {
                //  setTableId("2");
                search("ana");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }*/
            return tableString;
        }

        private Map<String, Object> getRowData(ListEntry row) {
            Map<String, Object> rowValues = new HashMap<String, Object>();
            for (String tag : row.getCustomElements().getTags()) {
                Object value = row.getCustomElements().getValue(tag);
                rowValues.put(tag, value);
            }
            return rowValues;
        }

        protected void onPostExecute(List<String> result) {
            if(listFeed.getEntries().size()!=0) {
                BuildTable(rows, cols, tableString);
            }else {
                Toast.makeText(getApplicationContext(), "Valoarea nu a fost gasita", Toast.LENGTH_LONG).show();
            }
        }
    }

    class Task3 extends AsyncTask<Void, Void, List<String>>{
        int rows, cols;
        ListFeed listFeed = null;
        public List<String> doInBackground(Void... params) {
            try {
                listFeedUrl = new URI(worksheetEntry.getListFeedUrl().toString()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ListQuery listQuery = new ListQuery(listFeedUrl);

            listQuery.setSpreadsheetQuery(queryText);
            listQuery.setSortColumn(queryColumn.toLowerCase());
            listQuery.setReverse(true);
            listQuery.getOrderBy();
            Log.i("param 1", listQuery.getOrderBy());

            try {
                listFeed = Service.getService().query(listQuery, ListFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            if(listFeed.getEntries().size()!=0){
                for (ListEntry row : listFeed.getEntries()) {
                    for (String tag : row.getCustomElements().getTags()) {
                        Log.i("row.getCustomElements", row.getCustomElements().getValue(tag) + "\t");
                        tableString.add( row.getCustomElements().getValue(tag));
                    }
                }
                rows =  listFeed.getEntries().size();
                cols = listFeed.getEntries().get(0).getCustomElements().getTags().size();
                Log.i("row numbers", String.valueOf(rows));
                Log.i("col numbers", String.valueOf(cols));
            }

            return tableString;
        }
        protected void onPostExecute(List<String> result) {
            if(listFeed.getEntries().size()!=0) {
                BuildTable(rows, cols, tableString);
            }else {
                Toast.makeText(getApplicationContext(), "Valoarea nu a fost gasita", Toast.LENGTH_LONG).show();
            }
        }
    }

    class Task4 extends AsyncTask<Void, Void, List<String>> {
        int rows, cols;
        ListFeed listFeed = null;
        public List<String> doInBackground(Void... params) {
            try {
                listFeedUrl = new URI(worksheetEntry.getListFeedUrl().toString()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ListQuery listQuery = new ListQuery(listFeedUrl);
            listQuery.setSpreadsheetQuery(queryText);

            try {
                listFeed = Service.getService().query(listQuery, ListFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            if(listFeed instanceof ListFeed) {
                for (ListEntry row : listFeed.getEntries()) {
                    for (String tag : row.getCustomElements().getTags()) {
                        Log.i("row.getCustomElements", row.getCustomElements().getValue(tag) + "\t");
                        tableString.add(row.getCustomElements().getValue(tag));
                    }
                }
                rows = listFeed.getEntries().size();
                cols = listFeed.getEntries().get(0).getCustomElements().getTags().size();
                Log.i("row numbers", String.valueOf(rows));
                Log.i("col numbers", String.valueOf(cols));
            }
            return tableString;
        }
        protected void onPostExecute(List<String> result) {
            if(listFeed.getEntries().size()!=0 || listFeed !=null) {
                BuildTable(rows, cols, tableString);
            }else {
                if (listFeed.getEntries().size() == 0) {
                    Toast.makeText(getApplicationContext(), "Nu s-au gasit valori.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Interogarea nu e valida! \n Pentru mai multe interogari, utilizati operatorul '&'.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    class Task5 extends AsyncTask<Void, Void, List<String>> {
        int rows, cols;
        ListFeed listFeed = null;
        public List<String> doInBackground(Void... params) {
            try {
                listFeedUrl = new URI(worksheetEntry.getListFeedUrl().toString()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ListQuery listQuery = new ListQuery(listFeedUrl);
            listQuery.setSpreadsheetQuery(queryText);
            listQuery.setReverse(true);

            try {
                listFeed = Service.getService().query(listQuery, ListFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            if(listFeed.getEntries().size()!=0 || listFeed !=null) {
                for (ListEntry row : listFeed.getEntries()) {
                    for (String tag : row.getCustomElements().getTags()) {
                        Log.i("row.getCustomElements", row.getCustomElements().getValue(tag) + "\t");
                        tableString.add(row.getCustomElements().getValue(tag));
                    }
                }
                rows = listFeed.getEntries().size();
                cols = listFeed.getEntries().get(0).getCustomElements().getTags().size();
                Log.i("row numbers", String.valueOf(rows));
                Log.i("col numbers", String.valueOf(cols));
            }
            return tableString;
        }
        protected void onPostExecute(List<String> result) {
            if(listFeed.getEntries().size()!=0  || listFeed ==null) {
                BuildTable(rows, cols, tableString);
            }else {
                Toast.makeText(getApplicationContext(),"Interogarea nu e valida! \n Pentru mai multe interogari, utilizati operatorul '&'.", Toast.LENGTH_LONG).show();
            }
        }
    }

    class Task6 extends AsyncTask<Void, Void, List<String>> {
        int rows, cols;
        ListFeed listFeed = null;
        public List<String> doInBackground(Void... params) {
            try {
                listFeedUrl = new URI(worksheetEntry.getListFeedUrl().toString()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ListQuery listQuery = new ListQuery(listFeedUrl);
            listQuery.setFullTextQuery(queryText);

            try {
                listFeed = Service.getService().query(listQuery, ListFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            if(listFeed.getEntries().size()!=0) {
                for (ListEntry row : listFeed.getEntries()) {
                    for (String tag : row.getCustomElements().getTags()) {
                        Log.i("row.getCustomElements", row.getCustomElements().getValue(tag) + "\t");
                        tableString.add(row.getCustomElements().getValue(tag));
                    }
                }
                rows = listFeed.getEntries().size();
                cols = listFeed.getEntries().get(0).getCustomElements().getTags().size();
                Log.i("row numbers", String.valueOf(rows));
                Log.i("col numbers", String.valueOf(cols));
            }
            return tableString;
        }
        protected void onPostExecute(List<String> result) {
            if(listFeed.getEntries().size()!=0) {
                BuildTable(rows, cols, tableString);
            }else {
                Toast.makeText(getApplicationContext(), "Valoarea nu a fost gasita", Toast.LENGTH_LONG).show();
            }
        }
    }

    class Task7 extends AsyncTask<Void, Void, List<String>> {
        int rows, cols;
        ListFeed listFeed = null;
        public List<String> doInBackground(Void... params) {
            try {
                listFeedUrl = new URI(worksheetEntry.getListFeedUrl().toString()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ListQuery listQuery = new ListQuery(listFeedUrl);
            listQuery.setFullTextQuery(queryText);
            listQuery.setReverse(true);

            try {
                listFeed = Service.getService().query(listQuery, ListFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            if(listFeed.getEntries().size()!=0) {
                for (ListEntry row : listFeed.getEntries()) {
                    for (String tag : row.getCustomElements().getTags()) {
                        Log.i("row.getCustomElements", row.getCustomElements().getValue(tag) + "\t");
                        tableString.add(row.getCustomElements().getValue(tag));
                    }
                }
                rows = listFeed.getEntries().size();
                cols = listFeed.getEntries().get(0).getCustomElements().getTags().size();
                Log.i("row numbers", String.valueOf(rows));
                Log.i("col numbers", String.valueOf(cols));
            }
            return tableString;
        }
        protected void onPostExecute(List<String> result) {
            if(listFeed.getEntries().size()!=0) {
                BuildTable(rows, cols, tableString);
            }else {
                Toast.makeText(getApplicationContext(), "Valoarea nu a fost gasita", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void BuildTable(int rows, int cols, List<String> tableString) {

        int index = 0;
        // outer for loop
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 40, 0);
        table_layout.setLayoutParams(params);
        for (int i = 0; i < rows; i++) {

            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
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
        main = (LinearLayout) findViewById(R.id.tbl);
        main.addView(table_layout);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup parent = (ViewGroup) findViewById(R.id.sortLayout);
        inflater.inflate(R.layout.tbl, parent);

    }

    public void search(String fullTextSearchString) throws IOException,
            ServiceException {
        RecordQuery query = new RecordQuery(listFeedUrl);
        query.setFullTextQuery(fullTextSearchString);
        Log.i("query", query.getUrl().toString());
        RecordFeed feed = Service.getService().query(query, RecordFeed.class);
        for (RecordEntry entry : feed.getEntries()) {
            printAndCacheEntry(entry);
            List<Field> values = entry.getFields();
            for(Field f:values) {
                Log.i("field", f.getName() + " " +f.getValue() + " " +f.getIndex());
            }
        }
    }

    public void printAndCacheEntry(RecordEntry entry) {

        // We only care about the entry id, chop off the leftmost part.
        // I.E., this turns http://spreadsheets.google.com/..../cpzh6 into cpzh6.
        String id = entry.getId().substring(entry.getId().lastIndexOf('/') + 1);

        // Cache all displayed entries so that they can be updated later.
        entriesCached.put(id, entry);

        Log.i("entry","-- id: " + String.valueOf(id) + "  title: " + entry.getTitle().getPlainText());

        for (Field field : entry.getFields()) {
            Log.i("message","     <field name=" + field.getName() + ">"
                    + field.getValue() + "</field>");
        }
    }
}
