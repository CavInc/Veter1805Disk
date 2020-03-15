package tk.cavinc.veter1805disk.data.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

    private String mMoveFile; // перемещаемый файл
    private boolean mModeMove = false; // флаг о том что перемещаем

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

        Collections.sort(mFileModels,new FielsSort());
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mFileModels.sort(new FielsSort());
        }
        */
    }

    // получили путь к папке загрузки
    public File getDownloadPathInStorage(){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return path;
    }


    // проверяем включен ли интернетик
    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    // имя перемещаемого объекта
    public String getMoveFile() {
        return mMoveFile;
    }

    public void setMoveFile(String moveFile) {
        mMoveFile = moveFile;
    }

    public boolean isModeMove() {
        return mModeMove;
    }

    public void setModeMove(boolean modeMove) {
        mModeMove = modeMove;
    }
}
