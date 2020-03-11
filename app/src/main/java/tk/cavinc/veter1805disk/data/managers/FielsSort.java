package tk.cavinc.veter1805disk.data.managers;

import tk.cavinc.veter1805disk.data.models.FileModels;

/**
 * Created by cav on 11.03.20.
 */

class FielsSort implements java.util.Comparator<FileModels> {

    @Override
    public int compare(FileModels o1, FileModels o2) {
        return  o1.getName().compareTo(o2.getName());
    }
}
