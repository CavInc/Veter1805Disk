package tk.cavinc.veter1805disk.utils;

/**
 * Created by cav on 07.03.20.
 * константы
 */

public interface ConstantManager {
    short RECORD_FILE = 0; // тип записи файл
    short RECORD_DIR = 1; // тип записи каталог

    String BASE_URL = "http://storeserver.kempir.com";
    //String BASE_URL = "http://192.168.1.23:5000";
    String GET_FILES_URL = "/api/getfiles";
    String DELTE_URL = "/api/deleteitem";
    String CREATE_DIR_URL = "/api/createdir";
    String SEND_FILE_URL = "/api/sendfile";
    String GET_FILE_URL = "/api/getfile/";

}
