package com.tassadar.vnote;

import android.content.Context;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;


public class VntManager {
    public static void importFile(Context ctx, String path) {
        File dir = ctx.getFilesDir();
        dir.mkdir();
        
        String filename = path.substring(path.lastIndexOf("/")+1);
        File target = new File(dir, filename);
        while(target.exists())
        {
            int idx = filename.lastIndexOf(".vnt");
            if(idx >= 0)
                filename = filename.substring(0, idx) + "a.vnt";
            else
                filename += "a.vnt";

            target = new File(dir, filename);
        }
        
        File source = new File(path);
        
        try {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }
        catch(FileNotFoundException ex) {
            
        }
        catch(IOException ex) {
            
        }
    }
    
    public static VntNote createNote(Context ctx, String text) {
        File dir = ctx.getFilesDir();
        dir.mkdir();
        
        VntNote n = VntNote.createNew(dir.getAbsolutePath(), text);
        m_notes.add(0, n);
        return n;
    }
    
    public static void eraseNote(VntNote n) {
        n.deleteFile();
        m_notes.remove(n);
    }
    
    public static void loadNotes(Context ctx) {
        m_notes.clear();
       
        File dir = ctx.getFilesDir();
        File[] list = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File path) {
                return !path.isHidden() && path.canRead() &&
                        path.getName().endsWith(".vnt");
            }
        });
        
        for(File f : list)
        {
            VntNote n = VntNote.createFromFile(f);
            if(n != null)
                m_notes.add(n);
        }

        Collections.sort(m_notes);
    }
    
    public static ArrayList<VntNote> getNotes() {
        return m_notes;
    }
    
    private static ArrayList<VntNote> m_notes = new ArrayList<VntNote>();
}
