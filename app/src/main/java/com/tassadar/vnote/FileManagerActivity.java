package com.tassadar.vnote;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.tassadar.vnote.FileListItem.FileItemClicked;
import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class FileManagerActivity extends Activity implements FileItemClicked, ActivityCompat.OnRequestPermissionsResultCallback
{
    private static final String START_PATH = "/sdcard";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_select);
        
        m_scroll_pos = new HashMap<String, Integer>();
        m_selected = new ArrayList<String>();
        m_fileItems = new ArrayList<FileListItem>();

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        } else {
            loadPath(START_PATH);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadPath(START_PATH);
        } else {
            Toast.makeText(this, R.string.need_perm, Toast.LENGTH_SHORT);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return false;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
            {
                if(m_cur_path.equals(START_PATH))
                   break;
                loadPath(m_cur_path.substring(0, m_cur_path.lastIndexOf("/")));
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void loadPath(String path) {
        m_cur_path = path;
        m_selected.clear();
        m_fileItems.clear();
        updateSelected();
        
        TextView t = (TextView)findViewById(R.id.path_text);
        t.setText(path);
        
        LinearLayout l = (LinearLayout)findViewById(R.id.file_list);
        l.removeAllViews();

        File folder = new File(path);
        if(!folder.exists() || !folder.isDirectory() || !folder.canRead())
            return;
        
        ArrayList<String> fileNames = new ArrayList<String>();
        ArrayList<String> folderNames = new ArrayList<String>();
        File[] list = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden() && pathname.canRead() && 
                        (pathname.isDirectory() || pathname.getName().endsWith(".vnt"));
            }
        });
        
        for(File f : list) {
            if(f.isDirectory())
                folderNames.add(f.getName());
            else
                fileNames.add(f.getName());
        }

        Collator collator = Collator.getInstance(new Locale("cs", "CZ"));
        Collections.sort(fileNames, collator);
        Collections.sort(folderNames, collator);

        if(!path.equals(START_PATH))
            folderNames.add(0, "..");
        
        for(int i = 0; i < folderNames.size(); ++i) {
             FileListItem listItem = new FileListItem(this, null, folderNames.get(i), true);
             l.addView(listItem.getView());
        }

        for(int i = 0; i < fileNames.size(); ++i) {
             FileListItem listItem = new FileListItem(this, null, fileNames.get(i), false);
             l.addView(listItem.getView());
             m_fileItems.add(listItem);
        }

        if(m_scroll_pos.containsKey(m_cur_path))
        {
            ScrollView v = (ScrollView)findViewById(R.id.scroll_view);
            v.post(new Runnable(){
                @Override
                public void run() {
                    ScrollView v = (ScrollView)findViewById(R.id.scroll_view);
                    v.scrollTo(0, m_scroll_pos.get(m_cur_path));
                }});
        }
    }

    @Override
    public void onFileItemChecked(String name, boolean is_folder, boolean checked) {
        if(is_folder) {
            String path = m_cur_path;
            if(name.equals(".."))
                path = m_cur_path.substring(0, m_cur_path.lastIndexOf("/"));
            else
                path += "/" + name;
            ScrollView v = (ScrollView)findViewById(R.id.scroll_view);
            m_scroll_pos.put(m_cur_path, v.getScrollY());
            loadPath(path);
        }
        else
        {
            if(checked)
                m_selected.add(m_cur_path + "/" + name);
            else
                m_selected.remove(m_selected.indexOf(m_cur_path + "/" + name));
            updateSelected();
        }
    }

    private void updateSelected() {
        String plural = getResources().getQuantityString(R.plurals.file_plural, m_selected.size());
        String str = String.format(getString(R.string.selected), m_selected.size(), plural);
        
        TextView v = (TextView)findViewById(R.id.selected_count);
        v.setText(str);
        Button b = (Button)findViewById(R.id.btn_import);
        b.setEnabled(!m_selected.isEmpty());
    }

    public void onImportClicked(View v) {
        for(String s : m_selected) {
            VntManager.importFile(this, Uri.fromFile(new File(s)));
        }
        String plural = getResources().getQuantityString(R.plurals.file_plural, m_selected.size());
        String str = String.format(getString(R.string.imported), m_selected.size(), plural);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        
        setResult(RESULT_OK);
        finish();
    }

    public void onAllClicked(View v) {
       for(FileListItem i : m_fileItems)
           i.setSelected(true);
    }

    public void onNoneClicked(View v) {
        for(FileListItem i : m_fileItems)
            i.setSelected(false);
    }

    private String m_cur_path;
    private HashMap<String, Integer> m_scroll_pos;
    private ArrayList<String> m_selected;
    private ArrayList<FileListItem> m_fileItems;
}
