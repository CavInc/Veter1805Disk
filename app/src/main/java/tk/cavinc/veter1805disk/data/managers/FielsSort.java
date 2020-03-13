package tk.cavinc.veter1805disk.data.managers;

import android.util.Log;

import tk.cavinc.veter1805disk.data.models.FileModels;

/**
 * Created by cav on 11.03.20.
 */

class FielsSort implements java.util.Comparator<FileModels> {

    private static final String TAG = "FS";

    @Override
    public int compare(FileModels o1, FileModels o2) {
        int filetype = (o1.getFileType() < o2.getFileType()) ? -1 : ((o1.getFileType() == o2.getFileType()) ? 0 : 1);
        Log.d(TAG,"FY "+filetype);
        Log.d(TAG,"COMP : "+o1.getName().compareTo(o2.getName()));
        return  o1.getName().compareTo(o2.getName());
    }
}
