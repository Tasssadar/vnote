package com.tassadar.vnote;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class VntNote implements Comparable<VntNote> {
    public static VntNote createFromFile(File f) {
        VntNote res = new VntNote();
        res.m_file = f;

        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String line;
            String head;
            String data;
            int idx;
            while((line = in.readLine()) != null)
            {
                idx = line.indexOf(":");
                head = line.substring(0, idx);
                data = line.substring(idx+1);
                
                res.handleLine(head, data);
            }
            in.close();
        }
        catch(Exception ex) {
            return null;
        }
        
        if (res.m_text == null || res.m_creation_date == null ||
            res.m_mod_date == null)
        {
            Log.e("VNote", "Skipping note beacause something is null!");
            res = null;
        }

        return res;
    }
    
    public static VntNote createNew(String path, String text) {
        VntNote res = new VntNote();
        res.m_text = text;
        res.m_creation_date = new Date();
        res.m_mod_date = new Date();        
        
        SimpleDateFormat f = new SimpleDateFormat("'" + path + "/'yyyy-MM-dd.HH.mm.ss.'vnt'");
        res.m_file = new File(f.format(new Date()));
        return res;
    }
    
    public void deleteFile() {
        m_file.delete();
    }
    
    public void handleLine(String head, String data) {
        if(head.startsWith("BODY")) {
            m_text = importSpecialChars(data).replace("\r\n", "\n");
        }
        else if(head.equals("DCREATED")) {
            m_creation_date = parseDate(data);
        }
        else if(head.equals("LAST-MODIFIED")) {
            m_mod_date = parseDate(data);
        }
    }
    
    private Date parseDate(String data) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        try {
            return f.parse(data);
        }
        catch(Exception ex) { }
        return null;
    }
    
    private String importSpecialChars(String data) {
        int c;
        StringBuilder res = new StringBuilder(data);
        int idx = res.indexOf("=");
        
        while(idx != -1) {
            c = Integer.parseInt(res.substring(idx+1, idx+3), 16);
            res.replace(idx, idx+3, Character.toString((char)c));
            ++idx;
            idx = res.indexOf("=", idx);
        }

        return res.toString();
    }
    
    public void save() {
        m_mod_date = new Date();
        try {
            FileWriter w = new FileWriter(m_file);
            w.write("BEGIN:VNOTE\r\n");
            w.write("VERSION:1.1\r\n");
            w.write("BODY;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:" + exportSpecialChars() + "\r\n");
            w.write("DCREATED:" + exportDate(m_creation_date) + "\r\n");
            w.write("LAST-MODIFIED:" + exportDate(m_mod_date) + "\r\n");
            w.write("END:VNOTE\r\n");
            w.close();
        }
        catch(Exception e) {
            int y = 0;
        }
    }
    
    private String exportSpecialChars() {
        StringBuilder res = new StringBuilder(m_text.replace("\n", "\r\n"));
        for(int i = 0; i < res.length(); ++i) {
            int c = res.codePointAt(i);
            if(c == 0x0A || c == 0x0D || c == 0x3D) // \r, \n, =
            {
                res.replace(i, i+1, "=" + String.format("%02X", c));
            }
        }
        return res.toString();
    }
    
    private String exportDate(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        return f.format(date);
    }
    
    public int compareTo(VntNote other) {
        return other.m_creation_date.compareTo(m_creation_date);
    }

    File m_file;
    String m_text;
    Date m_creation_date;
    Date m_mod_date;
}
