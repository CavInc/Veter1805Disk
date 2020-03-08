package tk.cavinc.veter1805disk.data.managers;

import android.content.Context;

import java.util.ArrayList;

import tk.cavinc.veter1805disk.BuildConfig;
import tk.cavinc.veter1805disk.data.models.FileModels;
import tk.cavinc.veter1805disk.utils.App;

/**
 * Created by cav on 07.03.20.
 */

public class DataManager {
    private static DataManager INSTANCE = null;

    private Context mContext;

    private ArrayList<FileModels> mFileModels;

    public static DataManager getInstance() {
        if (INSTANCE==null){
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public DataManager(){
        mContext = App.getContext();
    }

    // версия программы
    public String getVersionSoft(){
        return "Версия : "+ BuildConfig.VERSION_NAME;
    }

    public Context getContext() {
        return mContext;
    }

    public ArrayList<FileModels> getFileModels() {
        return mFileModels;
    }

    public void setFileModels(ArrayList<FileModels> fileModels) {
        mFileModels = fileModels;
    }

    public int getFilesStorageSize(){
        return mFileModels.size();
    }
}
