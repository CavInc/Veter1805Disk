package tk.cavinc.veter1805disk.ui.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.managers.DataManager;
import tk.cavinc.veter1805disk.utils.ConstantManager;
import tk.cavinc.veter1805disk.utils.FilePath;
import tk.cavinc.veter1805disk.utils.UriHelper;

/**
 * Created by cav on 12.03.20.
 * получение файла передаваемого через намерение ACTION_SEND
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

    // обрабатываем педанную ссылку на файл
    private void sendFile(Intent intent){
        String fileName = null;
        fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        fileName = UriHelper.getInstance().getFileName(fileUri,this);

        /*
        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cursor = getContentResolver().query(fileUri, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } else {
                Log.d(TAG,"ХММММ");
            }
        } else {
            fileName=new File(fileUri.getPath()).getName();
        }
        */


        Log.d(TAG,fileUri.toString());
        Log.d(TAG,fileUri.getEncodedPath()+" "+fileUri.getLastPathSegment());
        if (fileName != null) {
            Log.d(TAG, fileName);
        }
        mEditText.setText(fileName);
        //cursor.close();
    }

    // отправка файла на сервер
    private void sendNetFile(final File file, String fileName){
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file",fileName,
                        RequestBody.create(MediaType.parse(typeFile),file))
                .build();

        Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.SEND_FILE_URL)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Request request, final IOException e) {
                e.printStackTrace();
                file.delete();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MySendActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG,response.body().string());
                file.delete();
                finish();
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_bt) {
            onBackPressed();
        }
        if (v.getId() == R.id.store_bt) {
            /*
               пришлось сделать копирование во временный файл т.е. на прямую из uri
               нужный объект файл собрать не вышло.
               так же есть интересный момент с именем файла.
               из uri через ContentProvider имя возвращается только для тех типов файлов
               что умеет обрабатывать система. к примеру для файла с .xls имя не вернется
               а для .apk вернется... 
             */

            String selectedFilePath = FilePath.getPath(this, fileUri);
            Log.d(TAG,"PT :"+selectedFilePath);

            String fname2 = UriHelper.getInstance().getFileName(fileUri,this);

            Log.d(TAG,fname2);
            File file2 = null;
            try {
                file2 = File.createTempFile("123",null,getExternalCacheDir());
            } catch (IOException e) {
                e.printStackTrace();
            }

            ParcelFileDescriptor inputPFD = null;
            try {
                inputPFD = getContentResolver().openFileDescriptor(fileUri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            FileDescriptor fd = inputPFD.getFileDescriptor();

            try {
                FileOutputStream fout = new FileOutputStream(file2);
                FileInputStream finp = new FileInputStream(fd);
                FileChannel fileChannelIn = finp.getChannel();
                FileChannel fileChannelOut = fout.getChannel();
                fileChannelIn.transferTo(0, fileChannelIn.size(), fileChannelOut);

                fout.flush();
                fout.close();
                finp.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ///File file = new File(String.valueOf(fileUri));
            sendNetFile(file2,fname2);

            /*
            String selectedFilePath = FilePath.getPath(this, fileUri);

            sendNetFile(new File(selectedFilePath));
            */
        }
    }
}
