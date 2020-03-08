package tk.cavinc.veter1805disk.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tk.cavinc.veter1805disk.R;
import tk.cavinc.veter1805disk.data.models.FileModels;
import tk.cavinc.veter1805disk.utils.ConstantManager;

/**
 * Created by cav on 07.03.20.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private ArrayList<FileModels> data;
    private Context mContext;


    public FilesAdapter(Context context,ArrayList<FileModels> data){
        mContext = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_items,parent,false);
        return new ViewHolder(view);
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


    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView mImageView;
        private TextView mName;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.file_img);
            mName = itemView.findViewById(R.id.file_name);
        }
    }
}
