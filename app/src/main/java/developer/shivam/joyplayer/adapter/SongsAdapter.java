package developer.shivam.joyplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.activity.MainActivity;
import developer.shivam.joyplayer.listener.OnClickListener;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.util.Collector;
import developer.shivam.joyplayer.util.HelperMethods;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    Context mContext;
    List<Songs> mSongsList = new ArrayList<>();
    MainActivity activity = null;
    OnClickListener listener;

    public SongsAdapter(Context context, List<Songs> songsList) {
        mContext = context;
        mSongsList = songsList;
    }

    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.song_item_row_layout, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongsAdapter.ViewHolder holder, int position) {
        holder.tvSongName.setText(mSongsList.get(position).getName());
        holder.tvSingerName.setText(mSongsList.get(position).getSingerName() + " · " + HelperMethods.getSongDuration(Integer.valueOf(mSongsList.get(position).getDuration())));
        Picasso.with(mContext).load(Collector.getAlbumArtUri(Long.parseLong(mSongsList.get(position).getAlbumId()))).placeholder(R.drawable.default_album_art).into(holder.ivAlbumArt);
    }

    @Override
    public int getItemCount() {
        return mSongsList.size();
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvSongName;
        TextView tvSingerName;
        ImageView ivAlbumArt;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSongName = (TextView) itemView.findViewById(R.id.tvSongName);
            tvSingerName = (TextView) itemView.findViewById(R.id.tvSingerName);
            ivAlbumArt = (ImageView) itemView.findViewById(R.id.ivAlbumArt);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(ivAlbumArt, getPosition());
        }
    }
}
