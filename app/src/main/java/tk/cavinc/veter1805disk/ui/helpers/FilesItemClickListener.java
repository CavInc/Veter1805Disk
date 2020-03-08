package tk.cavinc.veter1805disk.ui.helpers;

import tk.cavinc.veter1805disk.data.models.FileModels;

/**
 * Created by cav on 08.03.20.
 */

public interface FilesItemClickListener {
    void onItemClick(int position);
    void onItemClick(FileModels items);
    void onItemMoreClick(FileModels fileModels);
}
