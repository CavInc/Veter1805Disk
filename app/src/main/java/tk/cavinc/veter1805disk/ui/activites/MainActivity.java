package tk.cavinc.veter1805disk.ui.activites;

import android.Manifest;
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
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.managers.DataManager;
import tk.cavinc.veter1805disk.data.models.FileModels;
import tk.cavinc.veter1805disk.ui.adapters.FilesAdapter;
import tk.cavinc.veter1805disk.ui.dialogs.OperationDialog;
import tk.cavinc.veter1805disk.ui.helpers.FilesItemClickListener;
import tk.cavinc.veter1805disk.utils.ConstantManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MA";
    private static final int PERMISSION_REQUEST_CODE = 218;
    private DataManager mDataManager;

    private FilesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OkHttpClient client;

    private ActionBar actionToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataManager = DataManager.getInstance();

        mRecyclerView = findViewById(R.id.lv);
        GridLayoutManager grid = new GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
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
        checkAndSetPrivelege();
        getFiles("/");
    }

    // проверяем и устанавливаем привеленгии
    private void checkAndSetPrivelege(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }

    private void updateUI(){
        if (mAdapter == null) {
            mAdapter = new FilesAdapter(this,mDataManager.getFileModels(),mFilesItemClickListener);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(mDataManager.getFileModels());
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
            }
        }
        return super.onOptionsItemSelected(item);
    }

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
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"CODE :"+response.code());
                String result = response.body().string();
                response.close();
                Log.d(TAG,result);
                JSONObject json;
                ArrayList<FileModels> rec = new ArrayList<>();
                try {
                    json = new JSONObject(result);
                    JSONArray dt = json.getJSONArray("files");
                    for (int i=0;i< dt.length();i++) {
                        JSONObject item = dt.getJSONObject(i);
                        short fileRecType;
                        if (item.getString("type").equals("DIR")) {
                            fileRecType = ConstantManager.RECORD_DIR;
                        } else {
                            fileRecType = ConstantManager.RECORD_FILE;
                        }
                        rec.add(new FileModels(item.getString("name"),0,fileRecType));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mDataManager.setFileModels(rec);
                updateUI();
            }
        });
    }

    private FileModels selectFileModel;

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


    OperationDialog.OperationDialogListener mOperationDialogListener = new OperationDialog.OperationDialogListener() {
        @Override
        public void onSelectItem(int id) {
            switch (id){
                case R.id.op_download:
                    downloadFile();
                    break;
                case R.id.op_move:
                    break;
                case R.id.op_delete:
                    deleteFileRecord();
                    break;
            }
        }
    };

    // считываем
    private void downloadFile(){
        Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.GET_FILE_URL+selectFileModel.getName())
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"CODE :"+response.code());
                Set<String> names = response.headers().names();
                for(String l: names){
                    Log.d(TAG,l);
                    Log.d(TAG,response.header(l));
                }
                ResponseBody body = response.body();
            }
        });

    }

    // удаляем элемент
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
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"CODE :"+response.code());
                String result = response.body().string();
                response.close();
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

            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }
}
