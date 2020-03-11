package tk.cavinc.veter1805disk.data.managers;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

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

    private Stack<String> mPathStack;

    public static DataManager getInstance() {
        if (INSTANCE==null){
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public DataManager(){
        mContext = App.getContext();
        mPathStack = new Stack<>();
        mFileModels = new ArrayList<>();
        mPathStack.push("/");
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mFileModels.sort(new FielsSort());
        }
    }

    // получили путь к папке загрузки
    public File getDownloadPathInStorage(){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return path;
    }

    public int getFilesStorageSize(){
        return mFileModels.size();
    }

    public String popPathStack(){
        return mPathStack.pop();
    }

    public void pushPathStack(String path){
        mPathStack.push(path);
    }

    public String peekPathStack(){
        return mPathStack.peek();
    }

    public int sizePathStack(){
        return mPathStack.size();
    }
}
