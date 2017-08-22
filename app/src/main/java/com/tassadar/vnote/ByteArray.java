package com.tassadar.vnote;


public class ByteArray {
    private final static int PAGE = 512;

    public ByteArray() {
        clear();
    }

    public ByteArray(int size) {
        m_size = size;
        m_data = new byte[size];
    }

    public ByteArray(byte[] data) {
        clear();
        assign(data, 0, data.length);
    }

    public ByteArray(byte[] data, int offset, int len) {
        clear();
        assign(data, offset, len);
    }

    private void ensureEnoughSpace(int extra) {
        int req = m_size + extra;
        if(req < m_data.length)
            return;

        int newSize = m_data.length;
        if(req - m_data.length < PAGE) {
            while(req >= newSize)
                newSize += 8;
        } else {
            while(req >= newSize)
                newSize += PAGE;
        }

        byte[] newData = new byte[newSize];
        System.arraycopy(m_data, 0, newData, 0, m_size);
        m_data = newData;
    }

    public void shrink() {
        if(m_data.length == m_size)
            return;

        byte[] newData = new byte[m_size];
        System.arraycopy(m_data, 0, newData, 0, m_size);
        m_data = newData;
    }

    public void reserve(int size) {
        if(size <= m_size)
            return;

        ensureEnoughSpace(size - m_size);
    }

    public void clear() {
        m_size = 0;
        m_data = new byte[0];
    }

    public int size() {
        return m_size;
    }

    public boolean empty() {
        return m_size == 0;
    }

    public byte[] toByteArray() {
        byte[] res = new byte[m_size];
        System.arraycopy(m_data, 0, res, 0, m_size);
        return res;
    }

    public byte[] data() {
        return m_data;
    }

    public void append(byte[] data) {
        append(data, 0, data.length);
    }

    public void append(byte[] data, int offset, int len) {
        ensureEnoughSpace(len);

        System.arraycopy(data, offset, m_data, m_size, len);
        m_size += len;
    }

    public void append(int oneByte) {
        ensureEnoughSpace(1);
        m_data[m_size++] = (byte)oneByte;
    }

    public void append(byte b) {
        ensureEnoughSpace(1);
        m_data[m_size++] = b;
    }

    public void append(ByteArray other) {
        append(other.data(), 0, other.size());
    }

    public void append(String text) {
        append(text.getBytes());
    }

    public byte at(int idx) {
        return m_data[idx];
    }

    public int uAt(int idx) {
        return (((int)m_data[idx]) & 0xFF);
    }

    public void set(int idx, byte b) {
        m_data[idx] = b;
    }

    public void set(int idx, int byteVal) {
        m_data[idx] = (byte)byteVal;
    }

    public void pop_back() {
        chop(1);
    }

    public void chop(int n) {
        // FIXME: resize array?
        m_size -= n;
    }

    public ByteArray clone() {
        ByteArray res = new ByteArray();
        res.m_data = new byte[m_size];
        res.m_size = m_size;
        System.arraycopy(m_data,  0, res.m_data, 0, m_size);
        return res;
    }

    public void swap(ByteArray other) {
        byte[] tmp = other.m_data;
        other.m_data = m_data;
        m_data = tmp;

        int t = other.m_size;
        other.m_size = m_size;
        m_size = t;
    }

    public void setEmpty() {
        m_size = 0;
    }

    public void setSize(int size) {
        m_size = size;
    }

    public void assign(byte[] data, int offset, int len) {
        if(len > m_size + 512)
            m_data = new byte[len];
        else if(len > m_size)
            ensureEnoughSpace(len - m_size);

        System.arraycopy(data, offset, m_data, 0, len);
        m_size = len;
    }

    public void resize(int len) {
        ensureEnoughSpace(len - m_size);
        if(m_size > len + 512) {
            byte[] d = new byte[len];
            System.arraycopy(m_data, 0, d, 0, len);
            m_data = d;
        }
        m_size = len;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("(");
        b.append(m_size);
        b.append(")[ ");
        for(int i = 0; i < m_size; ++i) {
            b.append(((int)m_data[i]) & 0xFF);
            b.append(", ");
        }
        b.append("]");
        return b.toString();
    }

    private byte[] m_data;
    private int m_size;
}