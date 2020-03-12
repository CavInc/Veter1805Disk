package tk.cavinc.veter1805disk.ui.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.managers.DataManager;
import tk.cavinc.veter1805disk.utils.ConstantManager;
import tk.cavinc.veter1805disk.utils.FilePath;

/**
 * Created by cav on 12.03.20.
 */

public class MySendActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MSE";
    private DataManager mDataManager;

    private EditText mEditText;
    private Uri fileUri;
    private String typeFile;

    //https://developer.android.com/training/sharing/receive
    //http://developer.alexanderklimov.ru/android/theory/intent.php#action_send


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mEditText = findViewById(R.id.file_name_et);

        findViewById(R.id.close_bt).setOnClickListener(this);
        findViewById(R.id.store_bt).setOnClickListener(this);

        Intent intent = getIntent();
        String action = intent.getAction();
        typeFile = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && typeFile != null) {
            Log.d(TAG,"ACTION :"+action);
            Log.d(TAG,"TYPE :"+ typeFile);
            sendFile(intent);
        }
    }

    private void sendFile(Intent intent){
        fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        Log.d(TAG,fileUri.toString());
        Log.d(TAG,fileUri.getEncodedPath()+" "+fileUri.getLastPathSegment());

        String fileName=new File(fileUri.getPath()).getName();
        Log.d(TAG,fileName);
        mEditText.setText(fileName);

    }

    private void sendNetFile(File file){
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file",file.getName(),
                        RequestBody.create(file, MediaType.parse(typeFile)))
                .build();

        Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.SEND_FILE_URL)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,response.body().string());
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_bt) {
            onBackPressed();
        }
        if (v.getId() == R.id.store_bt) {
            String selectedFilePath = FilePath.getPath(this, fileUri);

            sendNetFile(new File(selectedFilePath));
        }
    }
}
