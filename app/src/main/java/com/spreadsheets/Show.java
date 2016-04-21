package com.spreadsheets;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spreadsheets.test.spreadsheet.R;


public class Show extends Fragment {
    List<SpreadsheetEntry> spreadsheets;
    static List<String> list = new ArrayList<>();
    static String spreadsheetName;
    static String worksheetName;
    static ArrayAdapter<String> arrayAdapter;
    TextView textView;
    private WorksheetEntry worksheetEntry;
    private List<String> column_tags;

    public Show() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        thiscontext = container.getContext();
        return inflater.inflate(R.layout.show_fragment, container, false);
    }

    static ExpandableListAdapter listAdapter;

    static List<String> listDataHeader = new ArrayList<>();
    static HashMap<String, List<String>> listDataChild = new HashMap<>();
    Context thiscontext;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("Result", list.toString());

        ExpandableListView expListView = (ExpandableListView) getActivity().findViewById(R.id.lvExp);
        listAdapter = new ExpandableListAdapter(getActivity().getApplicationContext(), listDataHeader, listDataChild);

        registerForContextMenu(expListView);
        expListView.setAdapter(listAdapter);
        spreadsheetName = null;
        worksheetName = null;
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                spreadsheetName = listDataHeader.get(groupPosition);
                worksheetName = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);

                Intent intent = new Intent(getActivity().getApplicationContext(), ShowWorksheet.class);
                intent.putExtra("Spreadsheet name", spreadsheetName);
                intent.putExtra("Worksheet name", worksheetName);
                startActivity(intent);

                return false;
            }
        });
        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                                   public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                                       int itemType = ExpandableListView.getPackedPositionType(id);

                                                       if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                                                           int childPosition = ExpandableListView.getPackedPositionChild(id);
                                                           int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                                                           Log.i("child", String.valueOf(groupPosition) + " " + String.valueOf(childPosition));

                                                           spreadsheetName = listDataHeader.get(groupPosition);
                                                           worksheetName = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                                                       } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                                                           int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                                                           Log.i("parent", String.valueOf(groupPosition));

                                                           spreadsheetName = listDataHeader.get(groupPosition);

                                                       }
                                                       return false;
                                                   }
                                               }

        );


    }

    public void addViewList(HashMap<String, List<String>> result) {
        listDataChild.clear();
        listDataChild.putAll(result);
        List<String> keys = new ArrayList<String>();
        for (String key : listDataChild.keySet()) {
            keys.add(key);
        }
        listDataHeader.clear();
        listDataHeader.addAll(keys);
        Log.i("Header", result.keySet().toString());
        Log.i("Data", result.toString());
        /*list.clear();
        list.addAll(result);
        Log.i("Result", list.toString());
        arrayAdapter.notifyDataSetChanged();*/
        listAdapter.notifyDataSetChanged();

    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Optiuni");
        menu.add(0, v.getId(), 0, "Add rows");
        menu.add(0, v.getId(), 0, "Sort by column name");
        menu.add(0, v.getId(), 0, "Update row");


    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Add rows") {
            Log.i("Pos", spreadsheetName + " " + worksheetName);
            Intent intentAdd = new Intent(getActivity().getApplicationContext(), Add.class);
            intentAdd.putExtra("Sp name", spreadsheetName);
            intentAdd.putExtra("Wk name", worksheetName);
            Log.i("sp name", spreadsheetName);
            Log.i("wk name", worksheetName);
            startActivity(intentAdd);

            return true;
        } else if (item.getTitle() == "Sort by column name") {
            Log.i("Pos", spreadsheetName + " " + worksheetName);
            Intent intentShow = new Intent(getActivity().getApplicationContext(), Sort.class);
            intentShow.putExtra("Sp name", spreadsheetName);
            intentShow.putExtra("Wk name", worksheetName);
            Log.i("sp name", spreadsheetName);
            Log.i("wk name", worksheetName);
            startActivity(intentShow);

            return true;
        }else if (item.getTitle() == "Update row") {
            Log.i("Pos", spreadsheetName + " " + worksheetName);
            Intent intentShow = new Intent(getActivity().getApplicationContext(), Update.class);
            intentShow.putExtra("Sp name", spreadsheetName);
            intentShow.putExtra("Wk name", worksheetName);
            Log.i("sp name", spreadsheetName);
            Log.i("wk name", worksheetName);
            startActivity(intentShow);

            return true;
        }
        return super.onContextItemSelected(item);
    }


}



