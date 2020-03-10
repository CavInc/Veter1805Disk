package tk.cavinc.veter1805disk.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.utils.ConstantManager;

/**
 * Created by cav on 08.03.20.
 */

public class OperationDialog extends DialogFragment implements View.OnClickListener{

    private static final String RECORD_TYPE = "RT";
    private OperationDialogListener mDialogListener;

    private int recordType;

    public static OperationDialog newInstance(int recordType){
        Bundle args = new Bundle();
        args.putInt(RECORD_TYPE,recordType);
        OperationDialog dialog = new OperationDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            recordType = getArguments().getInt(RECORD_TYPE);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.operation_dialog,null);

        v.findViewById(R.id.op_download).setOnClickListener(this);
        v.findViewById(R.id.op_move).setOnClickListener(this);
        v.findViewById(R.id.op_delete).setOnClickListener(this);

        if (recordType == ConstantManager.RECORD_DIR) {
            v.findViewById(R.id.op_download).setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.BOTTOM | Gravity.LEFT | Gravity.RIGHT); // выставляем диалоговое окно внизу экрана

        return dialog;
    }

    public void setDialogListener(OperationDialogListener listener){
        mDialogListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mDialogListener != null) {
            mDialogListener.onSelectItem(v.getId()); //передаем id выбранного пункта
        }
        dismiss();
    }

    // интерфейс для передачи данных из диалога
    public interface OperationDialogListener {
        void onSelectItem(int id);
    }
}
