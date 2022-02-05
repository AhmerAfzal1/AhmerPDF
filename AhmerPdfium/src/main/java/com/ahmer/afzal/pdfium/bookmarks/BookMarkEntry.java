package com.ahmer.afzal.pdfium.bookmarks;


import com.ahmer.afzal.pdfium.R;
import com.ahmer.afzal.pdfium.treeview.TreeViewAdapter;

public class BookMarkEntry implements TreeViewAdapter.LayoutItemType {
    public int page;
    public String entryName;

    public BookMarkEntry(String entryName, int page) {
        this.entryName = entryName;
        this.page = page;
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_item;
    }

    @Override
    public String toString() {
        return "BookMarkEntry{" +
                "page=" + page +
                ", entryName='" + entryName + '\'' +
                '}';
    }
}
