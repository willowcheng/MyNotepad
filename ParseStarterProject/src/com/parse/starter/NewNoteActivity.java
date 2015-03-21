package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.model.Note;

public class NewNoteActivity extends ActionBarActivity {

    private Button saveButton;
    private Button deleteButton;
    private EditText noteTitle;
    private EditText noteLongitude;
    private EditText noteLatitude;
    private EditText noteBody;
    private Note note;
    private String noteId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        // Fetch the todoId from the Extra data
        if (getIntent().hasExtra("ID")) {
            noteId = getIntent().getExtras().getString("ID");
        }

        noteTitle = (EditText) findViewById(R.id.note_title);
        noteLongitude = (EditText) findViewById(R.id.note_longitude);
        noteLatitude = (EditText) findViewById(R.id.note_latitude);
        noteBody = (EditText) findViewById(R.id.note_body);
        saveButton = (Button) findViewById(R.id.saveButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);

        if (noteId == null) {
            note = new Note();
            note.setUuidString();

        } else {
            ParseQuery<Note> query = Note.getQuery();
            query.fromLocalDatastore();
            query.whereEqualTo("uuid", noteId);
            query.getFirstInBackground(new GetCallback<Note>() {

                @Override
                public void done(Note object, ParseException e) {
                    if (!isFinishing()) {
                        note = object;
                        noteTitle.setText(note.getTitle());
                        noteBody.setText(note.getBody());
                        noteLatitude.setText(note.getLatitude());
                        noteLongitude.setText(note.getLongitude());
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                }

            });

        }

        saveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                note.setTitle(noteTitle.getText().toString());
                note.setBody(noteBody.getText().toString());
                note.setLongitude(noteLongitude.getText().toString());
                note.setLatitude(noteLatitude.getText().toString());
                note.setDraft(true);
                note.setAuthor(ParseUser.getCurrentUser());
                note.pinInBackground(ParseApplication.NOTE_GROUP_NAME,
                        new SaveCallback() {

                            @Override
                            public void done(ParseException e) {
                                if (isFinishing()) {
                                    return;
                                }
                                if (e == null) {
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Error saving: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                        });
            }

        });

        deleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // The todo will be deleted eventually but will
                // immediately be excluded from query results.
                note.deleteEventually();
                setResult(Activity.RESULT_OK);
                finish();
            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_map) {
            startActivity(new Intent().setClass(this, MapActivity.class).putExtra("Longitude", Double.valueOf(note.getLongitude())).putExtra("Latitude", Double.valueOf(note.getLatitude())));

        }

        return super.onOptionsItemSelected(item);
    }

}
