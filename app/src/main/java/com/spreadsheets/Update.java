package com.spreadsheets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import spreadsheets.test.spreadsheet.R;

/**
 * Created by Roxana on 4/18/2016.
 */
public class Update extends Activity implements AdapterView.OnItemSelectedListener {
    private String spreadsheetName;
    private String worksheetName;
    URL listFeedUrl;
    List<String> tableString = new ArrayList<>();
    List<String> tableHeader = new ArrayList<>();
    TableLayout table_layout;
    WorksheetEntry worksheetEntry;
    String queryText;
    LinearLayout main;
    LinearLayout updateLayout;
    Spinner updateSpinner;
    Spinner rowSpinner;
    List<String> rowValues = new ArrayList<>();
    ArrayAdapter<String> updateAdapter;
    ArrayAdapter<String> rowAdapter;
    String columnToUpdate;
    String updateText;
    int rows, cols;
    ListFeed listFeed = null;
    String rowSelected;
    TextView tv;
    int rowNrSelected;
    String textToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);
        Intent intent = getIntent();
        spreadsheetName = null;
        worksheetName = null;
        if (intent.getExtras() != null) {
            spreadsheetName = intent.getStringExtra("Sp name");
            worksheetName = intent.getStringExtra("Wk name");
        }
        Log.i("Sp name", spreadsheetName);
        Log.i("Wk name", worksheetName);

        table_layout = new TableLayout(this);
        new Load().execute();
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText search = (EditText) findViewById(R.id.search_edit_text);
                queryText = search.getText().toString();
                new Task1().execute();
            }
        });
        //updateLayout = (LinearLayout) findViewById(R.id.update_layout);
        //updateLayout.setVisibility(View.INVISIBLE);

        /*updateSpinner = (Spinner) findViewById(R.id.update_spinner);
        updateSpinner.setOnItemSelectedListener(this);
        updateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tableHeader);
        updateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        updateSpinner.setAdapter(updateAdapter);

        rowSpinner = (Spinner) findViewById(R.id.row_spinner);
        rowSpinner.setOnItemSelectedListener(this);
        rowAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, rowValues);
        rowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rowSpinner.setAdapter(rowAdapter);

        Button updateButton = (Button) findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText update = (EditText) findViewById(R.id.update_edit_text);
                updateText = update.getText().toString();
                new Task2().execute();
            }
        });*/
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        columnToUpdate = updateSpinner.getSelectedItem().toString();
        rowSelected = rowSpinner.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    class Load extends AsyncTask<Void, Void, List<String>> {
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
            try {
                listFeedUrl = new URI(worksheetEntry.getListFeedUrl().toString()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class Task1 extends AsyncTask<Void, Void, List<String>> {


        public List<String> doInBackground(Void... params) {

            ListQuery listQuery = new ListQuery(listFeedUrl);
            listQuery.setFullTextQuery(queryText);

            try {
                listFeed = Service.getService().query(listQuery, ListFeed.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            if (listFeed.getEntries().size() != 0) {
                for (String tag : listFeed.getEntries().get(0).getCustomElements().getTags()) {
                    tableHeader.add(tag);
                }

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
                for (int i = 0; i < rows; i++) {
                    rowValues.add(String.valueOf(i + 1));
                }
            }
            return tableString;
        }

        protected void onPostExecute(List<String> result) {

            if (listFeed.getEntries().size() != 0) {
                BuildTable(rows, cols, tableString);
//                updateAdapter.notifyDataSetChanged();
//                rowAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getApplicationContext(), "Valoarea nu a fost gasita", Toast.LENGTH_LONG).show();
            }
            //updateLayout.setVisibility(View.VISIBLE);

        }
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
            TextView textView = (TextView)findViewById(v.getId());
            menu.setHeaderTitle("You selected " + "\""+ textView.getText().toString()+"\"");
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.cell_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.update_cell:


                showInputDialog();

                return true;
            case R.id.delete_cell:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    String colNameSelected;
    int idSelected;
    View.OnClickListener onclicklistener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            rowNrSelected = Math.round(v.getId() / cols + 1);
            colNameSelected = tableHeader.get(v.getId() % rows);
            Toast.makeText(getApplicationContext(), "coloana " + colNameSelected + "randul " + rowNrSelected, Toast.LENGTH_SHORT).show();


        }

    } ;
    int rowToDelete;
    View.OnLongClickListener onlongclicklistener = new View.OnLongClickListener(){

        @Override
        public boolean onLongClick(View v) {
            idSelected = v.getId();
            //registerForContextMenu(v);
            //openContextMenu(v);
            if (idSelected > rows * cols) {
                Log.i("Msg", "delete selected " + String.valueOf(v.getId()) + "row to delete=" + String.valueOf(idSelected - rows*cols) );
                rowToDelete = idSelected - rows * cols;
                showDeleteDialog();
            } else {

                showInputDialog();
                rowNrSelected = Math.round(v.getId() / cols + 1);
                colNameSelected = tableHeader.get(v.getId() % rows);

            }
            return false;
        }
    };
    EditText dialogEditText;
    protected void showInputDialog() {

        // get input_dialog.xmlg.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        dialogEditText = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);
        dialogEditText.setText(tableString.get(idSelected));
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateText = dialogEditText.getText().toString();
                        new UpdateValue().execute();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    protected void showDeleteDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.delete_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);


        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new Delete().execute();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

        private void BuildTable(int rows, int cols, List<String> tableString) {

            int index = 0;
            // outer for loop
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(40, 20, 40, 20);
            table_layout.setLayoutParams(params);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
            TableRow rowHeader = new TableRow(this);
            lp.setMargins(3, 3, 3, 3);
            rowHeader.setLayoutParams(lp);
            TextView tvNr = new TextView(this);
            tvNr.setLayoutParams(lp);
            tvNr.setText("");
            rowHeader.addView(tvNr);
            for (int i = 0; i < tableHeader.size(); i++) {
                TextView tvHeader = new TextView(this);
                tvHeader.setLayoutParams(lp);
                tvHeader.setText(tableHeader.get(i).toUpperCase());
                rowHeader.addView(tvHeader);
            }
            table_layout.addView(rowHeader);
            for (int i = 0; i < rows; i++) {

                TableRow row = new TableRow(this);
                row.setLayoutParams(lp);
                ImageView icon = new ImageView(this);
                icon.setImageResource(R.drawable.ic_action_name2);
                icon.setId(cols*rows+i+1);
                icon.setOnLongClickListener(onlongclicklistener);
                row.addView(icon);
                /*TextView nr = new TextView(this);
                nr.setLayoutParams(lp);
                nr.setText(String.valueOf(i + 1));
                row.addView(nr);*/
                // inner for loop
                for (int j = 0; j < cols; j++) {
                    tv = new TextView(this);
                    tv.setLayoutParams(lp);
                    tv.setText(tableString.get(index));
                    tv.setTextSize(18);
                    tv.setTextColor(Color.BLACK);
                    tv.setClickable(true);
                    tv.setPadding(0, 10, 0, 0);
                    tv.setGravity(Gravity.CENTER);
                    tv.setId(index);
                    tv.setBackgroundColor(Color.WHITE);
                    tv.setOnClickListener(onclicklistener);
                    tv.setOnLongClickListener(onlongclicklistener);
                    index++;
                    row.addView(tv);
                    Log.d("Index", String.valueOf(index));
                    Log.d("Size of list", String.valueOf(tableString.size()));

                }
                Log.d("Size of list", String.valueOf(tableString.size()));
                table_layout.addView(row);
            }
            main = (LinearLayout) findViewById(R.id.tableValues);
            main.addView(table_layout);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup parent = (ViewGroup) findViewById(R.id.sortLayout);
            inflater.inflate(R.layout.tbl, parent);

        }

        class UpdateValue extends AsyncTask<Void, Void, List<String>> {
            int rows, cols;

            public List<String> doInBackground(Void... params) {
                ListEntry row = listFeed.getEntries().get(rowNrSelected-1);
                row.getCustomElements().setValueLocal(colNameSelected.toLowerCase(), updateText);
                try {
                    row.update();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    Toast.makeText(getApplicationContext(), "Row couldn't update!", Toast.LENGTH_LONG).show();
                }

                return null;

            }

            @Override
            protected void onPostExecute(List<String> strings) {
                super.onPostExecute(strings);
                TextView textView = (TextView)findViewById(idSelected);
                textView.setText(dialogEditText.getText().toString());
                Toast.makeText(getApplicationContext(), "Row updated!", Toast.LENGTH_LONG).show();
            }
        }

    class Delete extends AsyncTask<Void, Void, List<String>> {
        int rows, cols;

        public List<String> doInBackground(Void... params) {
            ListEntry row = listFeed.getEntries().get(rowToDelete-1);

            try {
                row.delete();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                Toast.makeText(getApplicationContext(), "Row couldn't be deleted!", Toast.LENGTH_LONG).show();
            }

            return null;

        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            Toast.makeText(getApplicationContext(), "Row" + rowToDelete + "deleted!", Toast.LENGTH_LONG).show();
        }
    }

    }
