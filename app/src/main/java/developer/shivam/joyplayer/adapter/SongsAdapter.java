package developer.shivam.joyplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.listener.OnClickListener;
import developer.shivam.joyplayer.pojo.Songs;
import developer.shivam.joyplayer.util.Retriever;
import developer.shivam.joyplayer.util.ConnectionDetector;
import developer.shivam.joyplayer.util.HelperMethods;

public class SongsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Songs> mSongsList = new ArrayList<>();
    private OnClickListener listener;

    /**
     * This means after how many items ad should come.
     */
    private int AD_AFTER_ITEMS = 20;

    private final int TYPE_SONG_ITEM = 0;

    private final int TYPE_AD_ITEM = 1;

    public SongsAdapter(Context context, List<Songs> songsList) {
        mContext = context;
        mSongsList = songsList;

        if (mSongsList.size() > 0 && mSongsList.size() < 100) {
            AD_AFTER_ITEMS = 20;
        } else if (mSongsList.size() < 200){
            AD_AFTER_ITEMS = 40;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SONG_ITEM) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_row_song_item, null));
        } else {
            return new AdHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_row_ad_item, null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder songHolder = (ViewHolder) holder;
            songHolder.tvSongName.setText(mSongsList.get(position).getName());
            songHolder.tvSingerName.setText(mSongsList.get(position).getSingerName() + " · " + HelperMethods.getSongDuration(Integer.valueOf(mSongsList.get(position).getDuration())));
            Glide.with(mContext).load(Retriever.getAlbumArtUri(Long.parseLong(mSongsList.get(position).getAlbumId()))).placeholder(R.drawable.default_album_art).into(songHolder.ivAlbumArt);
        } else if (holder instanceof AdHolder) {
            AdHolder adHolder = (AdHolder) holder;
            //adHolder.adView.loadAd(new AdRequest.Builder().build());
            adHolder.adView.loadAd(new AdRequest.Builder().addTestDevice("C506057DBAF31FD9D2D08AE17D432321").build());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (ConnectionDetector.hasNetworkConnection(mContext)) {
            return position % AD_AFTER_ITEMS == 0 ? TYPE_AD_ITEM : TYPE_SONG_ITEM;
        } else {
            return TYPE_SONG_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mSongsList.size();
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvSongName;
        TextView tvSingerName;
        ImageView ivAlbumArt;

        ViewHolder(View itemView) {
            super(itemView);
            tvSongName = (TextView) itemView.findViewById(R.id.tvSongName);
            tvSingerName = (TextView) itemView.findViewById(R.id.tvSingerName);
            ivAlbumArt = (ImageView) itemView.findViewById(R.id.ivAlbumArt);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(ivAlbumArt, getAdapterPosition());
        }
    }

    private class AdHolder extends RecyclerView.ViewHolder {

        NativeExpressAdView adView;

        AdHolder(View itemView) {
            super(itemView);
            adView = (NativeExpressAdView) itemView.findViewById(R.id.nativeExpressAdView);
        }
    }
}
