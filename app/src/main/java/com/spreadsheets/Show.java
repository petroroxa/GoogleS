package com.spreadsheets;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gdata.data.spreadsheet.SpreadsheetEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spreadsheets.test.spreadsheet.R;


public class Show extends Fragment {
    List<SpreadsheetEntry> spreadsheets;
    static List<String> list = new ArrayList<>();
    static String spreadsheetName;
    static String worksheetName;
    static ArrayAdapter<String> arrayAdapter;
    TextView textView;
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
        menu.add(Menu.NONE, R.id.add, Menu.NONE, "Add rows");
        menu.add(Menu.NONE, R.id.search, Menu.NONE, "Search");


    }

    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add:

                Log.i("Pos", spreadsheetName + " " + worksheetName);
                Intent intent = new Intent(getActivity().getApplicationContext(), Add.class);
                intent.putExtra("Sp name", spreadsheetName);
                intent.putExtra("Wk name", worksheetName);
                Log.i("sp name", spreadsheetName);
                Log.i("wk name", worksheetName);
                startActivity(intent);

                return true;
            case R.id.search:


                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    /*@Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show:
                //archive(item);
                return true;
            case R.id.show2:
               // delete(item);
                return true;
            default:
                return false;
        }
    }*/

}