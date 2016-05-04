package com.spreadsheets;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spreadsheets.test.spreadsheet.R;


public class Main extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Account account;
    private List<SpreadsheetEntry> spreadsheets;
    private int pos = 0;
    SpreadsheetService service;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        AccountManager.get(Main.this)
                .getAuthTokenByFeatures("com.google", "wise", null, Main.this,
                        null, null, doneCallback, null);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Show(), "Show");
        adapter.addFragment(new SearchAll(), "SearchAll");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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

    class Task extends AsyncTask<Void, Integer, HashMap<String, List<String>>> {

        @Override
        protected void onPreExecute() {
            //textView.setText("Hello !!!");
            progressBar = (ProgressBar) findViewById(R.id.progressBar1);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
        public HashMap<String, List<String>> doInBackground(Void... params) {
            service = new SpreadsheetService("Spreadsheet Integration");
            SpreadsheetFeed feed;
            try {
                String wiseToken = AccountManager.get(Main.this).blockingGetAuthToken(account, "wise", true);
                service.setUserToken(wiseToken);
                Service.setService(service);
                URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
                feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
                SFeed.setFeed(feed);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return prepareListData(feed);
        }

        public HashMap<String, List<String>> prepareListData(SpreadsheetFeed feed) {
            List<String> listDataHeader = new ArrayList<String>();
            HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();

            spreadsheets = feed.getEntries();

            for (SpreadsheetEntry spreadsheet : spreadsheets) {
                List<String> worksheetList = new ArrayList<>();
                Log.i("My App", spreadsheet.getTitle().getPlainText());

                listDataHeader.add(spreadsheet.getTitle().getPlainText());

                List<WorksheetEntry> worksheets = null;
                try {
                    worksheets = spreadsheet.getWorksheets();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }

                for (WorksheetEntry worksheet : worksheets) {
                    worksheetList.add(worksheet.getTitle().getPlainText());
                    listDataChild.put(spreadsheet.getTitle().getPlainText(), worksheetList);
                }
            }
            return listDataChild;
        }

        protected void onPostExecute(HashMap<String, List<String>> result) {
            progressBar.setVisibility(View.GONE);
            Show show = new Show();
            show.addViewList(result);

        }



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_button_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_search:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


}