package com.parse.starter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.model.Note;
import com.parse.ui.ParseLoginBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final int LOGIN_ACTIVITY_CODE = 100;
    private static final int EDIT_ACTIVITY_CODE = 200;

    // Adapter for the Todos Parse Query
    private ParseQueryAdapter<Note> noteListAdapter;

    private LayoutInflater inflater;

    // For showing empty and non-empty todo views
    private ListView todoListView;
    private LinearLayout noTodosView;

    private TextView loggedInInfoView;
    private Note note;
    private Builder mBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBuilder = new AlertDialog.Builder(this);

        // Set up the views
        todoListView = (ListView) findViewById(R.id.todo_list_view);
        noTodosView = (LinearLayout) findViewById(R.id.no_todos_view);
        todoListView.setEmptyView(noTodosView);
        loggedInInfoView = (TextView) findViewById(R.id.loggedin_info);

        // Set up the Parse query to use in the adapter
        ParseQueryAdapter.QueryFactory<Note> factory = new ParseQueryAdapter.QueryFactory<Note>() {
            public ParseQuery<Note> create() {
                ParseQuery<Note> query = Note.getQuery();
                query.orderByDescending("createdAt");
                query.fromLocalDatastore();
                return query;
            }
        };
        // Set up the adapter
        inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        noteListAdapter = new ToDoListAdapter(this, factory);

        // Attach the query adapter to the view
        ListView noteListView = (ListView) findViewById(R.id.todo_list_view);
        noteListView.setAdapter(noteListAdapter);

        noteListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Note note = noteListAdapter.getItem(position);
                openEditView(note);
            }
        });
        noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                note = noteListAdapter.getItem(i);
                mBuilder.setIcon(R.drawable.ic_launcher)
                        .setMessage("Delete Note?")
                        .setNegativeButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (ParseUser.getCurrentUser() != null) {
                                            note.deleteEventually();
                                        }
                                        noteListAdapter.loadObjects();
                                        if (ParseUser.getCurrentUser().isNew()) {
                                            syncTodosToParse();
                                        } else {
                                            loadFromParse();
                                        }

                                    }
                                })
                        .setPositiveButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog,
                                                        int id) {
                                        dialog.dismiss();
                                    }
                                });
                mBuilder.create().show();
                noteListAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        // Check if we have a real user
        if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            // Sync data to Parse
            syncTodosToParse();
            // Update the logged in label info
            updateLoggedInInfo();
        }
    }

    private void updateLoggedInInfo() {
        if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            loggedInInfoView.setText(getString(R.string.logged_in,
                    currentUser.getString("name")));
        } else {
            loggedInInfoView.setText(getString(R.string.not_logged_in));
        }
    }

    private void openEditView(Note note) {
        Intent i = new Intent(this, NewNoteActivity.class);
        i.putExtra("ID", note.getUuidString());
        startActivityForResult(i, EDIT_ACTIVITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // An OK result means the pinned dataset changed or
        // log in was successful
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_ACTIVITY_CODE) {
                // Coming back from the edit view, update the view
                noteListAdapter.loadObjects();
            } else if (requestCode == LOGIN_ACTIVITY_CODE) {
                // If the user is new, sync data to Parse,
                // else get the current list from Parse
                if (ParseUser.getCurrentUser().isNew()) {
                    syncTodosToParse();
                } else {
                    loadFromParse();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            // Make sure there's a valid user, anonymous
            // or regular
            if (ParseUser.getCurrentUser() != null) {
                startActivityForResult(new Intent(this, NewNoteActivity.class),
                        EDIT_ACTIVITY_CODE);
            }
        }

//        if (item.getItemId() == R.id.action_sync) {
//            syncTodosToParse();
//        }

        if (item.getItemId() == R.id.action_logout) {
            // Log out the current user
            ParseUser.logOut();
            // Create a new anonymous user
            ParseAnonymousUtils.logIn(null);
            // Update the logged in label info
            updateLoggedInInfo();
            // Clear the view
            noteListAdapter.clear();
            // Unpin all the current objects
            ParseObject
                    .unpinAllInBackground(ParseApplication.NOTE_GROUP_NAME);
            invalidateOptionsMenu();
        }

        if (item.getItemId() == R.id.action_all_map) {
            Intent i = new Intent();
            String tmLongitude;
            String tmLatitude;
            String tmTitle;
            i.setClass(this, MapActivity.class);
            ArrayList<String> latitudeList = new ArrayList<>();
            ArrayList<String> longitudeList = new ArrayList<>();
            ArrayList<String> titleList = new ArrayList<>();
            for (int j = 0; j < noteListAdapter.getCount(); j++) {
                tmLatitude = noteListAdapter.getItem(j).getLatitude();
                tmLongitude = noteListAdapter.getItem(j).getLongitude();
                tmTitle = noteListAdapter.getItem(j).getTitle();
                latitudeList.add(j, tmLatitude);
                longitudeList.add(j, tmLongitude);
                titleList.add(j, tmTitle);
            }
            i.putStringArrayListExtra("Latitudes", latitudeList);
            i.putStringArrayListExtra("Longitudes", longitudeList);
            i.putStringArrayListExtra("Titles", titleList);
            startActivity(i);
        }

        if (item.getItemId() == R.id.action_login) {

            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean realUser = !ParseAnonymousUtils.isLinked(ParseUser
                .getCurrentUser());
        menu.findItem(R.id.action_login).setVisible(!realUser);
        menu.findItem(R.id.action_logout).setVisible(realUser);
        return true;
    }

    private void syncTodosToParse() {
        // We could use saveEventually here, but we want to have some UI
        // around whether or not the draft has been saved to Parse
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
                // If we have a network connection and a current logged in user,
                // sync the
                // notes

                // In this app, local changes should overwrite content on the
                // server.

                ParseQuery<Note> query = Note.getQuery();
                query.fromPin(ParseApplication.NOTE_GROUP_NAME);
                query.whereEqualTo("isDraft", true);
                query.findInBackground(new FindCallback<Note>() {
                    public void done(List<Note> notes, ParseException e) {
                        if (e == null) {
                            for (final Note note : notes) {
                                // Set is draft flag to false before
                                // syncing to Parse
                                note.setDraft(false);
                                note.saveInBackground(new SaveCallback() {

                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            // Let adapter know to update view
                                            if (!isFinishing()) {
                                                noteListAdapter
                                                        .notifyDataSetChanged();
                                            }
                                        } else {
                                            // Reset the is draft flag locally
                                            // to true
                                            note.setDraft(true);
                                        }
                                    }

                                });

                            }
                        } else {
                            Log.i("TodoListActivity",
                                    "syncTodosToParse: Error finding pinned todos: "
                                            + e.getMessage());
                        }
                    }
                });
            } else {
                // If we have a network connection but no logged in user, direct
                // the person to log in or sign up.

                ParseLoginBuilder builder = new ParseLoginBuilder(this);
                startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
            }
        } else {
            // If there is no connection, let the user know the sync didn't
            // happen
            Toast.makeText(
                    getApplicationContext(),
                    "Your device appears to be offline. Some todos may not have been synced to Parse.",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void loadFromParse() {
        ParseQuery<Note> query = Note.getQuery();
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Note>() {
            public void done(List<Note> notes, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground((List<Note>) notes,
                            new SaveCallback() {
                                public void done(ParseException e) {
                                    if (e == null) {
                                        if (!isFinishing()) {
                                            noteListAdapter.loadObjects();
                                        }
                                    } else {
                                        Log.i("TodoListActivity",
                                                "Error pinning notes: "
                                                        + e.getMessage());
                                    }
                                }
                            });
                } else {
                    Log.i("TodoListActivity",
                            "loadFromParse: Error finding pinned notes: "
                                    + e.getMessage());
                }
            }
        });
    }

    private class ToDoListAdapter extends ParseQueryAdapter<Note> {

        public ToDoListAdapter(Context context,
                               ParseQueryAdapter.QueryFactory<Note> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public View getItemView(Note note, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_note, parent, false);
                holder = new ViewHolder();
                holder.todoTitle = (TextView) view
                        .findViewById(R.id.note_title);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            TextView todoTitle = holder.todoTitle;
            todoTitle.setText(note.getTitle());
            if (note.isDraft()) {
                todoTitle.setTypeface(null, Typeface.ITALIC);
            } else {
                todoTitle.setTypeface(null, Typeface.NORMAL);
            }
            return view;
        }
    }

    private static class ViewHolder {
        TextView todoTitle;
    }
}
