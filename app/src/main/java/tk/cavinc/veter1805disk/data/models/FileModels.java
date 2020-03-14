package tk.cavinc.veter1805disk.data.models;

/**
 * Created by cav on 07.03.20.
 */

public class FileModels {
    private String mName;
    private long mFileSize;
    private short mTypeRecord;
    private short mFileType;

    public FileModels(String name, short typeRecord) {
        mName = name;
        mTypeRecord = typeRecord;
    }

    public FileModels(String name, long fileSize, short typeRecord) {
        mName = name;
        mFileSize = fileSize;
        mTypeRecord = typeRecord;
    }

    public FileModels(String name, long fileSize, short typeRecord, short fileType) {
        mName = name;
        mFileSize = fileSize;
        mTypeRecord = typeRecord;
        mFileType = fileType;
    }

    public String getName() {
        return mName;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public short getTypeRecord() {
        return mTypeRecord;
    }

    public short getFileType() {
        return mFileType;
    }

    @Override
    public String toString() {
        // перекроем метод что бы можно было использовать сортирову по паре тип записи + имя
        return ""+mTypeRecord+"-"+mName;
    }
}
