package tk.cavinc.veter1805disk.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.models.FileModels;
import tk.cavinc.veter1805disk.ui.helpers.FilesItemClickListener;
import tk.cavinc.veter1805disk.utils.ConstantManager;

/**
 * Created by cav on 07.03.20.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private static final String TAG = "FAD";
    private ArrayList<FileModels> data;
    private Context mContext;
    private FilesItemClickListener mFilesItemClickListener;


    public FilesAdapter(Context context,ArrayList<FileModels> data,FilesItemClickListener listener){
        mContext = context;
        this.data = data;
        mFilesItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_items2,parent,false);
        return new ViewHolder(view,mFilesItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModels record = data.get(position);
        holder.mName.setText(record.getName());
        if (record.getTypeRecord() == ConstantManager.RECORD_DIR) {
            holder.mImageView.setImageDrawable(mContext.getResources().
                    getDrawable(R.drawable.ic_folder_black_24dp,null));
        } else {
            holder.mImageView.setImageDrawable(mContext.getResources().
                    getDrawable(R.drawable.ic_description_black_24dp,null));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<FileModels> data) {
        this.data.clear();
        this.data.addAll(data);
    }


    // класс содержит данный о элементе списка и работе с ним
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private TextView mName;

        private FilesItemClickListener mFilesItemClickListener;

        public ViewHolder(View itemView,FilesItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.file_img);
            mName = itemView.findViewById(R.id.file_name);
            mFilesItemClickListener = listener;
            itemView.findViewById(R.id.more_button).setOnClickListener(this);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            System.out.println(v);
            if (v.getId() == R.id.more_button) {
                Log.d(TAG,"MORE");
                // тут передаем сигнал о том что жамкнули на кнопке MORE и заоодно передаем данные позиции
                if (mFilesItemClickListener != null) {
                    mFilesItemClickListener.onItemMoreClick(data.get(getAdapterPosition()));
                }
            } else {
                // тут передаем сигнал о том что жамкнули на самой карточке
                if (mFilesItemClickListener != null) {
                    mFilesItemClickListener.onItemClick(data.get(getAdapterPosition()));
                }
            }
        }
    }
}
