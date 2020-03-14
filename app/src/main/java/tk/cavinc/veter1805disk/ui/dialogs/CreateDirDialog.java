package tk.cavinc.veter1805disk.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;


import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.managers.DataManager;
import tk.cavinc.veter1805disk.ui.helpers.CreateDialogListener;
import tk.cavinc.veter1805disk.utils.ConstantManager;

/**
 * Created by cav on 12.03.20.
 * диалог создания каталога на сервере
 */

public class CreateDirDialog extends DialogFragment {
    private String TAG ="CDD";
    private DataManager mDataManager;

    private EditText mName;

    private CreateDialogListener mDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // таким образом мы может получить ссылку на слушателя если он объявлен как импрементация
        // в активности (implements CreateDialogListener)
        try {
            mDialogListener = (CreateDialogListener) getActivity();
        } catch (Exception e){
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataManager = DataManager.getInstance();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.create_dir_dialog,null);

        mName = v.findViewById(R.id.create_dir_et);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v).setTitle("Новый каталог")
                .setNegativeButton(R.string.dialog_no,null)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createRemoteDir();
                    }
                });

        return builder.create();
    }

    // получили ссылку на слушателя
    public void setDialogListener(CreateDialogListener listener){
        mDialogListener = listener;
    }

    // создаем каталог на сервере с использованием okhttpclient
    private void createRemoteDir(){
        String name = mName.getText().toString();
        OkHttpClient client = new OkHttpClient();
        String json = "{\"path\":\""+mDataManager.peekPathStack()+"\",\"name\":\""+name+"\"}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);

        Request request = new Request.Builder()
                .url(ConstantManager.BASE_URL+ConstantManager.CREATE_DIR_URL)
                .addHeader("Accept-Charset","UTF-8")
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                // если слушаетель объявлен дергаем метод
                if (mDialogListener != null) {
                    mDialogListener.onError(e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG,"OKD");
                Log.d(TAG,response.body().string());
                // если слушаетель объявлен дергаем метод
                if (mDialogListener != null){
                    mDialogListener.onCreate();
                }
            }
        });
    }
}
