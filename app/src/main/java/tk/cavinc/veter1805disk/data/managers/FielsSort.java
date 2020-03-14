package tk.cavinc.veter1805disk.data.managers;

import android.util.Log;

import tk.cavinc.veter1805disk.data.models.FileModels;

/**
 * Created by cav on 11.03.20.
 * Сортировка списка для ArrayList
 */

class FielsSort implements java.util.Comparator<FileModels> {

    private static final String TAG = "FS";

    @Override
    public int compare(FileModels o1, FileModels o2) {
        return o1.toString().compareTo(o2.toString());
        //return  o1.getName().compareTo(o2.getName());
    }
}
