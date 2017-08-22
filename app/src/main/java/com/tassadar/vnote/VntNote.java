package com.tassadar.vnote;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class VntNote implements Comparable<VntNote> {
    public static VntNote createFromFile(File f) {
        VntNote res = new VntNote();
        res.m_file = f;

        try {
            BufferedReader in = new BufferedReader(new FileReader(f));

            int body = 0;
            String line;
            ArrayList<String> lines = new ArrayList<String>();

            while((line = in.readLine()) != null) {
                line = line.trim();

                if(body == 0 && line.startsWith("BODY;") && line.endsWith("=")) {
                    body = 1;
                    lines.add(line.substring(0, line.length()-1));
                } else if(body == 1) {
                    String l = lines.get(lines.size()-1);
                    if(line.endsWith("=")) {
                         l += line.substring(0, line.length()-1);
                    } else {
                        body = 2;
                        l += line;
                    }
                    lines.set(lines.size()-1, l);
                } else {
                    lines.add(line);
                }
            }

            in.close();

            int idx;
            String head;
            String data;

            for(String l : lines) {
                idx = l.indexOf(":");
                head = l.substring(0, idx);
                data = l.substring(idx+1);
                
                res.handleLine(head, data);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        if (res.m_text == null)
        {
            Log.e("VNote", "Skipping note because text is null!");
            return null;
        }

        // Lets require only BODY tag
        if(res.m_creation_date == null)
            res.m_creation_date = new Date();

        if(res.m_mod_date == null)
            res.m_mod_date = res.m_creation_date;

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
        int c, start, end;
        StringBuilder res = new StringBuilder(data);
        int idx = res.indexOf("=");
        ByteArray bytes = new ByteArray();
        
        while(idx >= 0) {
            bytes.clear();
            start = idx;
            while(true)
            {
                ++idx;
                end = idx+2;
                c = Integer.parseInt(res.substring(idx, end), 16);
                bytes.append(c);
                idx = res.indexOf("=", idx);
                if(idx == -1 || idx != end)
                    break;
            }

            try {
                String str = new String(bytes.toByteArray(), "UTF-8");
                res.replace(start, end, str);
                idx -= (end - start) - str.length();
            } catch(UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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
    
    private static boolean needsEscape(byte c) {
        return c < 32 || c > 126 || c == 0x3D;
    }
    
    private String exportSpecialChars() {
        byte[] data = new byte[0];

        try {
            data = m_text.replace("\n", "\r\n").getBytes("UTF-8");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder res = new StringBuilder();
        for(int i = 0; i < data.length; ++i) {
            if(VntNote.needsEscape(data[i]))
                res.append("=").append(String.format("%02X", ((int)data[i]) & 0xFF));
            else
                res.append((char)data[i]);
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
