package com.tassadar.vnote;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.service.textservice.SpellCheckerService.Session;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ListActivity implements OnItemClickListener
{
    private static final int REQ_IMPORT = 1;
    private static final int REQ_EDIT   = 2;
    private static final int REQ_NEW    = 3;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_selected = new ArrayList<CheckBox>();

        VntManager.loadNotes(this);
        loadNotes();
        
        getListView().setOnItemClickListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.import_note:
                startActivityForResult(new Intent(this, FileManagerActivity.class), REQ_IMPORT);
                return false;
            case R.id.new_note:
                startActivityForResult(new Intent(this, NoteActivity.class), REQ_NEW);
                return false;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode != RESULT_OK)
            return;
        
        switch(reqCode) {
            case REQ_IMPORT:
                VntManager.loadNotes(this);
                loadNotes();
                break;
            case REQ_EDIT:
            case REQ_NEW:
                loadNotes();
                break;
        }
    }
    
    private void loadNotes() {
        String[] from = new String[] { "note", "date" };
        int[] to = new int[] { R.id.note_line, R.id.note_date };

        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

        SimpleDateFormat f = new SimpleDateFormat("d.M.yyyy H:mm");
        ArrayList<VntNote> notes = VntManager.getNotes();
        String line;
        for(VntNote n : notes) {
            int end = n.m_text.indexOf("\n");
            if(end == -1)
                end = n.m_text.length();

            line = n.m_text.substring(0, end);

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("note", line);
            map.put("date", f.format(n.m_mod_date));
            fillMaps.add(map);
        }
        
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.note_list_item, from, to); 
        setListAdapter(adapter);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(this, NoteActivity.class);
        i.putExtra("vntIdx", position);
        startActivityForResult(i, REQ_EDIT);
    }
    
    public void noteCheckClicked(View v) {
        CheckBox b = (CheckBox)v;
        if(b.isChecked())
        {
            m_selected.add(b);
            if (m_action_mode == null) {
                // Start the CAB using the ActionMode.Callback defined above
                m_action_mode = startActionMode(m_action_mode_callback);
            }
        }
        else
        {
            m_selected.remove(b);
            if(m_selected.isEmpty() && m_action_mode != null)
            {
                m_action_mode.finish();
                m_action_mode = null;
            }
        }

        if(m_action_mode != null) {
            String plural = getResources().getQuantityString(R.plurals.note_plural, m_selected.size());
            String str = String.format(getString(R.string.note_selected), m_selected.size(), plural);
            m_action_mode.setTitle(str);
        }
    }
    
    private void deselectAll() {
        for(CheckBox b : m_selected) {
            b.setChecked(false);
        }
        m_selected.clear();
    }
    
    private void eraseSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String plural = getResources().getQuantityString(R.plurals.note_plural, m_selected.size());
        String text = String.format(getString(R.string.erase_notes_text), m_selected.size(), plural);

        builder.setMessage(text)
               .setTitle(R.string.erase_notes_title)
               .setPositiveButton(R.string.delete, new DeleteListener())
               .setNegativeButton(R.string.cancel, null);

        builder.create().show();
    }
    
    private class DeleteListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            ArrayList<VntNote> notes = new ArrayList<VntNote>();
            for(CheckBox b : m_selected) {
                int idx = getListView().getPositionForView(b);
                notes.add(VntManager.getNotes().get(idx));
            }
            
            for(VntNote n : notes)
                VntManager.eraseNote(n);

            notes.clear();
            
            m_selected.clear();
            if(m_action_mode != null) {
                m_action_mode.finish();
            }
            
            loadNotes();
        }
    }
    
    private ActionMode.Callback m_action_mode_callback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.select_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_notes:
                    eraseSelected();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            deselectAll();
            m_action_mode = null;
        }
    };
   
    
    private ActionMode m_action_mode;
    private ArrayList<CheckBox> m_selected;
}
