/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tassadar.vnote;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class FileListItem {
    public interface FileItemClicked {
        void onFileItemChecked(String name, boolean is_folder, boolean checked);
    }
    
    public FileListItem(FileManagerActivity activity, ViewGroup parent, String file_name, boolean is_folder) {
        m_view = View.inflate(activity, R.layout.file_list_item, parent);
        
        m_file_name = file_name;
        m_is_folder = is_folder;
        m_listener = activity;

        CheckBox b = (CheckBox)m_view.findViewById(R.id.file_select);
        TextView t = (TextView)m_view.findViewById(R.id.folder_name);
        if(!is_folder)
        {
            
            b.setText(file_name);
            b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkbox, 0);
            b.setVisibility(View.VISIBLE);
            t.setVisibility(View.GONE);
            b.setOnCheckedChangeListener(new BoxChangeListener());
        }
        else
        {
            t.setCompoundDrawablesWithIntrinsicBounds(R.drawable.collections_collection, 0, 0, 0);
            t.setText(file_name);
            b.setVisibility(View.GONE);
            t.setVisibility(View.VISIBLE);
            t.setOnClickListener(new TextClickListener());
        }
    }
    
    public View getView() {
        return m_view;
    }
    
    private class BoxChangeListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton btn, boolean checked) {
            m_listener.onFileItemChecked(m_file_name, m_is_folder, checked);
        }
    }
    
    private class TextClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            m_listener.onFileItemChecked(m_file_name, m_is_folder, false);
        }
    }

    public void setSelected(boolean selected) {
        if(m_is_folder)
            return;

        CheckBox b = (CheckBox)m_view.findViewById(R.id.file_select);
        if(b.isChecked() == selected)
            return;

        b.setChecked(selected);
    }

    private View m_view;
    private String m_file_name;
    private FileItemClicked m_listener;
    private boolean m_is_folder;
}
