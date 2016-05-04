package com.spreadsheets;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import spreadsheets.test.spreadsheet.R;

public class MainActivity extends Activity {
    private Account account;
    int col;
    int rows;
    Link fileLink;
    private static final String tag = "MainActivity";
    List<SpreadsheetEntry> spreadsheets;
    int pos = 0;
    TableLayout table_layout;
    List<String> tableString;
    SpreadsheetEntry spreadsheet1;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        AccountManager.get(MainActivity.this)
                .getAuthTokenByFeatures("com.google", "wise", null, MainActivity.this,
                        null, null, doneCallback, null);
    }


    private AccountManagerCallback<Bundle> doneCallback = new AccountManagerCallback<Bundle>() {
        public void run(AccountManagerFuture<Bundle> arg0) {
            Bundle b;
            try {
                b = arg0.getResult();
                String name = b.getString(AccountManager.KEY_ACCOUNT_NAME);
                String type = b.getString(AccountManager.KEY_ACCOUNT_TYPE);
                account = new Account(name, type);
                new Task().execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    };

    class Task extends AsyncTask<Void, Void, List<String>> {

        public List<String> doInBackground(Void... params) {
            SpreadsheetService service = new SpreadsheetService("Spreadsheet Integration");
            SpreadsheetFeed feed;
            try {
                String wiseToken = AccountManager.get(MainActivity.this).blockingGetAuthToken(account, "wise", true);
                service.setUserToken(wiseToken);
                URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
                feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
                SFeed.setFeed(feed);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return getSheets(feed);
        }

        public List<String> getSheets(SpreadsheetFeed feed) {
            List<String> sheetlist = new ArrayList<>();
            spreadsheets = feed.getEntries();
            for (SpreadsheetEntry spreadsheet : spreadsheets) {
                Log.i("My App", spreadsheet.getTitle().getPlainText());
                sheetlist.add(spreadsheet.getTitle().getPlainText());
            }
            return sheetlist;
        }

        protected void onPostExecute(List<String> result) {
            List<String> list = result;
            ListView lv = (ListView) findViewById(R.id.list_view_id);
            registerForContextMenu(lv);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getBaseContext(),
                    R.layout.show_list_item, R.id.lblListItem, list);
            lv.setAdapter(arrayAdapter);
            AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                        long arg3) {

                    spreadsheet1 = spreadsheets.get(position);
                    fileLink = spreadsheet1.getSpreadsheetLink();
                    pos = position;
                    Log.e("Click", "Position " + pos);

                }

            };

            lv.setOnItemClickListener(onItemClickListener);
        }
        /*public void getContent() throws IOException, ServiceException {
            SpreadsheetEntry spreadsheet = spreadsheets.get(pos);
            WorksheetFeed worksheetFeed = service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
            WorksheetEntry worksheet = worksheets.get(0);
            URL cellFeedUrl = null;
            try {
                cellFeedUrl = new URI(worksheet.getCellFeedUrl().toString() + "?return-empty=true").toURL();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
            tableString = new ArrayList<>();
            int index = 0;
            for (CellEntry cell : cellFeed.getEntries()) {
                tableString.add(index, cell.getCell().getValue());
                Log.e("Index", String.valueOf(index));
                Log.e("Cell", cell.getCell().getValue());
                index++;
            }
            Log.e("Application", tableString.toString());
            Log.e("Cols", String.valueOf(col));
            Log.e("Rows", String.valueOf(rows));
        }*/
    }

    void open() {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(fileLink.getHref()));
            startActivity(i);
        } catch (Exception e) {
            Log.d("error", e.getMessage());
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
                Log.e("Index", String.valueOf(index));
                Log.e("Size of list", String.valueOf(tableString.size()));
//                row.addView(tv);
            }
            Log.e("Size of list", String.valueOf(tableString.size()));
            table_layout.addView(row);
        }
        LinearLayout main = (LinearLayout) findViewById(R.id.table);
        main.addView(table_layout);
    }
/*    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId() == R.id.list_view_id) {  //YOUR VIEW THAT IS ATTACHED TO LISTASK

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            menu.setHeaderTitle("Options");
            String[] menuItems = { "Option1", "Option2", "Option3", "Option4" };
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }

    }*/

  /*  public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            //case R.id.open:

                Intent intent = new Intent(getApplicationContext(), DisplaySheet.class);
                intent.putStringArrayListExtra("feed", (ArrayList<String>) tableString);
                startActivity(intent);
                //open();
                return true;
           // case R.id.edit:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);


    }
 /*   protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
    }*/
}