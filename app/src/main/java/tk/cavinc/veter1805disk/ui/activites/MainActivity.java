package tk.cavinc.veter1805disk.ui.activites;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.managers.DataManager;
import tk.cavinc.veter1805disk.data.models.FileModels;
import tk.cavinc.veter1805disk.ui.adapters.FilesAdapter;
import tk.cavinc.veter1805disk.ui.helpers.FilesItemClickListener;
import tk.cavinc.veter1805disk.utils.ConstantManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MA";
    private DataManager mDataManager;

    private FilesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataManager = DataManager.getInstance();

        mRecyclerView = findViewById(R.id.lv);
        GridLayoutManager grid = new GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setLayoutManager(grid);

        //mRecyclerView.addItemDecoration(new LineDividerItemDecoration(this, R.drawable.line_divider))

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setHasFixedSize(true);

        client = new OkHttpClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFiles("/");
    }

    private void updateUI(){
        if (mAdapter == null) {
            mAdapter = new FilesAdapter(this,mDataManager.getFileModels(),mFilesItemClickListener);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(mDataManager.getFileModels());
            mAdapter.notifyDataSetChanged();
        }

    }

    private void getFiles(String path){
        String json = "{\"path\":\"/\"}";
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

    FilesItemClickListener mFilesItemClickListener = new FilesItemClickListener() {
        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onItemClick(FileModels items) {
            if (items.getTypeRecord() == ConstantManager.RECORD_DIR) {

            }
        }

        @Override
        public void onItemMoreClick(FileModels fileModels) {

        }
    };
}
