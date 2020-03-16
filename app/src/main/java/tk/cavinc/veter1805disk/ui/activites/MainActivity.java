package tk.cavinc.veter1805disk.ui.activites;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;


import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.managers.DataManager;
import tk.cavinc.veter1805disk.data.models.FileModels;
import tk.cavinc.veter1805disk.ui.adapters.FilesAdapter;
import tk.cavinc.veter1805disk.ui.dialogs.CreateDirDialog;
import tk.cavinc.veter1805disk.ui.dialogs.OperationDialog;
import tk.cavinc.veter1805disk.ui.helpers.CreateDialogListener;
import tk.cavinc.veter1805disk.ui.helpers.FilesItemClickListener;
import tk.cavinc.veter1805disk.utils.ConstantManager;

/**
 * главная активность приложениея
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MA";
    private static final int PERMISSION_REQUEST_CODE = 218;
    private DataManager mDataManager; // менеджер даннных

    private FilesAdapter mAdapter; // адаптер с данными о файлах
    private RecyclerView mRecyclerView; // виджет для показа списка
    private OkHttpClient client;  // сетевой клиент для собственно работы с сервером

    private ActionBar actionToolbar;
    private MenuItem storeMoveItem;
    private MenuItem cancelMoveItem;
    private MenuItem createDirItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataManager = DataManager.getInstance(); // создали или получили экземпляр из синглтона

        mRecyclerView = findViewById(R.id.lv);

        GridLayoutManager grid = new GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager); // установили менеджер размещения элементов
        //mRecyclerView.setLayoutManager(grid);

        //mRecyclerView.addItemDecoration(new LineDividerItemDecoration(this, R.drawable.line_divider))

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setHasFixedSize(true);

        client = new OkHttpClient();

        setupToolbar();
    }

    private void setupToolbar(){
       actionToolbar = getSupportActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // проверка привелегий
        checkAndSetPrivelege();
        // проверим наличие сети и если нет прогураемся
        if (mDataManager.isOnline()) {
            getFiles("/");
        } else {
            Toast.makeText(this,"Нет сети",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        createDirItem = menu.findItem(R.id.main_menu_create_folder);
        storeMoveItem = menu.findItem(R.id.main_menu_store);
        if (mDataManager.isModeMove()) {
            storeMoveItem.setVisible(false);
        }
        cancelMoveItem = menu.findItem(R.id.main_menu_cancel_store);
        if (mDataManager.isModeMove()) {
            cancelMoveItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }



    // проверяем и устанавливаем привеленгии
    private void checkAndSetPrivelege(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }

    // обновляем или создаем адаптер для показа данных
    private void updateUI(){
        if (mAdapter == null) {
            mAdapter = new FilesAdapter(this,mDataManager.getFileModels(),mFilesItemClickListener);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(mDataManager.getFileModels());
            // делаем таким образом потому что может вызываться из не главной нити приложения
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            String level = mDataManager.popPathStack();
            level = mDataManager.peekPathStack();
            getFiles(level);
            updateUI();
            if (level == "/") {
                actionToolbar.setDisplayHomeAsUpEnabled(false);
                actionToolbar.setSubtitle(null);
            }
        }
        if (item.getItemId() == R.id.main_menu_refresh) {
            getFiles(mDataManager.peekPathStack());
        }
        // вызов диалога создания каталога на сервере
        if (item.getItemId() == R.id.main_menu_create_folder) {
            CreateDirDialog dirDialog = new CreateDirDialog();
            dirDialog.setDialogListener(mCreateDialogListener);
            dirDialog.show(getFragmentManager(),"CDD");
        }
        // сохраняем выбранный файл
        if (item.getItemId() == R.id.main_menu_store) {
            storeMoveItem.setVisible(false);
            cancelMoveItem.setVisible(false);
            createDirItem.setVisible(true);
            storeMoveFile();
        }
        // отмена выбора объекта для перемещения
        if (item.getItemId() == R.id.main_menu_cancel_store) {
            storeMoveItem.setVisible(false);
            cancelMoveItem.setVisible(false);
            mDataManager.setMoveFile(null);
            mDataManager.setModeMove(false);
            createDirItem.setVisible(true);
        }
        return super.onOptionsItemSelected(item);
    }

    // получаем список файлов с сервера используя okhttpclient
    private void getFiles(String path){
        String json = "{\"path\":\""+path+"\"}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);

        final Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.GET_FILES_URL)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG,"CODE :"+response.code());
                String result = response.body().string();
                Log.d(TAG,result);
                JSONObject json;
                ArrayList<FileModels> rec = new ArrayList<>();
                try {
                    json = new JSONObject(result);
                    JSONArray dt = json.getJSONArray("files");
                    long fileSize = 0;
                    for (int i=0;i< dt.length();i++) {
                        JSONObject item = dt.getJSONObject(i);
                        short fileRecType;
                        if (item.getString("type").equals("DIR")) {
                            fileRecType = ConstantManager.RECORD_DIR;
                            fileSize = 0;
                        } else {
                            fileRecType = ConstantManager.RECORD_FILE;
                            fileSize = item.getLong("size");
                        }
                        rec.add(new FileModels(item.getString("name"),fileSize,fileRecType));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mDataManager.setFileModels(rec);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        });
    }

    private FileModels selectFileModel;

    // обработчик собыйтий от списка файлов
    FilesItemClickListener mFilesItemClickListener = new FilesItemClickListener() {
        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onItemClick(FileModels items) {
            // если тип файла каталог то проваливаемся внутрь
            if (items.getTypeRecord() == ConstantManager.RECORD_DIR) {
                String upLevel = mDataManager.peekPathStack();
                upLevel = upLevel+"/"+items.getName();
                mDataManager.pushPathStack(upLevel);
                getFiles(upLevel);
                if (actionToolbar != null) {
                    actionToolbar.setDisplayHomeAsUpEnabled(true);
                    actionToolbar.setSubtitle(upLevel);
                }
            }
        }

        @Override
        public void onItemMoreClick(FileModels fileModels) {
            selectFileModel = fileModels;
            OperationDialog dialog = OperationDialog.newInstance(fileModels.getTypeRecord());
            dialog.setDialogListener(mOperationDialogListener);
            dialog.show(getFragmentManager(),"OD");
        }
    };

    // обработчик событий от диалога создания каталога
    CreateDialogListener mCreateDialogListener = new CreateDialogListener() {
        @Override
        public void onCreate() {
            getFiles(mDataManager.peekPathStack());
        }

        @Override
        public void onError(final String error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,error,Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    // обработчик событий от диалога операций над файлом
    OperationDialog.OperationDialogListener mOperationDialogListener = new OperationDialog.OperationDialogListener() {
        @Override
        public void onSelectItem(int id) {
            switch (id){
                case R.id.op_download:
                    downloadFile();
                    break;
                case R.id.op_move:
                    moveFileRecord();
                    break;
                case R.id.op_delete:
                    deleteFileRecord();
                    break;
            }
        }
    };

    // подготавливаемся к перемещению
    private void moveFileRecord(){
        Log.d(TAG,mDataManager.peekPathStack()+"/"+selectFileModel.getName());
        mDataManager.setMoveFile(mDataManager.peekPathStack()+"/"+selectFileModel.getName());
        storeMoveItem.setVisible(true);
        cancelMoveItem.setVisible(true);
        createDirItem.setVisible(false);
    }

    // перемещаем файл
    private void storeMoveFile(){
        String json = "{\"src\":\""+mDataManager.getMoveFile()+"\"," +
                "\"dest\":\""+mDataManager.peekPathStack()+"\"}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);

        Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.MOVE_ITEM_URL)
                .post(requestBody)
                .build();


        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG,response.body().string());
                getFiles(mDataManager.peekPathStack());
            }
        });

    }

    // считываем файл с сервера используя okhttp
    private void downloadFile(){
        Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.GET_FILE_URL+selectFileModel.getName())
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG,"CODE :"+response.code());
                Set<String> names = response.headers().names();
                for(String l: names){
                    Log.d(TAG,l);
                    Log.d(TAG,response.header(l));
                }
                ResponseBody body = response.body();
                File downloadPath = mDataManager.getDownloadPathInStorage();

                Log.d(TAG,downloadPath.getAbsolutePath());
                storeFile(downloadPath,selectFileModel.getName(),body);
            }
        });
    }

    // сохраняем полученный файл в хранилище на устройстве. (в каталог Downdloads/Загрузки
    private void storeFile(File downloadPath,String fname,ResponseBody body){
        InputStream inStr = null;
        try {
            inStr = body.byteStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream out;
        BufferedInputStream bufferinstream = new BufferedInputStream(inStr);
        File outfile = new File(downloadPath, fname);
        Log.d(TAG,outfile.getAbsolutePath());
        try {
            out = new FileOutputStream(new File(downloadPath,fname));
            int current = 0;
            while ((current = bufferinstream.read()) != -1){
                out.write(current);
            }
            out.flush();
            out.close();
            inStr.close();
            body.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"Получение файла завершено",Toast.LENGTH_LONG).show();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // удаляем элемент на сервере с использованием okhttpclient
    private void deleteFileRecord(){
        String json = "{\"name\":\""+mDataManager.peekPathStack()+"/"+selectFileModel.getName()+"\"}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);

        final Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.DELTE_URL)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Request request, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG,"CODE :"+response.code());
                String result = response.body().string();
                JSONObject json;
                try {
                    json = new JSONObject(result);
                    if (!json.getBoolean("status")) {
                        Toast.makeText(MainActivity.this,json.getString("msg"),Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getFiles(mDataManager.peekPathStack());
            }

        });
    }
}
