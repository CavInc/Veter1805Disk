package tk.cavinc.veter1805disk.utils;

/**
 * Created by cav on 07.03.20.
 */

public interface ConstantManager {
    short RECORD_FILE = 0; // тип записи файл
    short RECORD_DIR = 1; // тип записи каталог

    String BASE_URL = "http://192.168.56.10:5000";
    String GET_FILES_URL = "/api/getfiles";
    String DELTE_URL = "";
    String CREATE_DIR_URL = "";
    String SEND_FILE_URL = "";

}
