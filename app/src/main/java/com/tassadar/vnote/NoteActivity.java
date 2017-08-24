package com.tassadar.vnote;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class NoteActivity extends Activity
{
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        
        if(getIntent() != null && getIntent().getExtras() != null) {
            int idx = getIntent().getExtras().getInt("vntIdx", -1);
            if(idx >= 0 && idx < VntManager.getNotes().size())
            {
                m_note = VntManager.getNotes().get(idx);
                TextView t = (TextView)findViewById(R.id.text);
                t.setText(m_note.m_text);
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_activity, menu);
        
        boolean monospace = getPreferences(MODE_PRIVATE).getBoolean("use_monospace", false);
        MenuItem it = menu.findItem(R.id.use_monospace);
        if(it != null)
        {
            it.setChecked(monospace);
            setMonospace(monospace);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.save:
                save();
                setResult(RESULT_OK);
                finish();
                return true;
            case R.id.use_monospace:
                item.setChecked(!item.isChecked());
                setMonospace(item.isChecked());
                return true;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void save() {
        TextView t = (TextView)findViewById(R.id.text);
        if(m_note == null)
            m_note = VntManager.createNote(this, t.getText().toString());
        else
            m_note.m_text = t.getText().toString();
        m_note.save();
    }
    
    private void setMonospace(boolean use_monospace) {
        TextView t = (TextView)findViewById(R.id.text);
        if(use_monospace)
            t.setTypeface(Typeface.MONOSPACE);
        else
            t.setTypeface(Typeface.DEFAULT);
        
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean("use_monospace", use_monospace);
        editor.apply();
    }
    
    VntNote m_note;
    
}
