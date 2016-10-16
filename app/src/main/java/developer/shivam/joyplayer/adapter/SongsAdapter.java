package developer.shivam.joyplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import developer.shivam.joyplayer.R;
import developer.shivam.joyplayer.activity.MainActivity;
import developer.shivam.joyplayer.listener.OnClickListener;
import developer.shivam.joyplayer.model.Songs;
import developer.shivam.joyplayer.util.Collector;
import developer.shivam.joyplayer.util.HelperMethods;

public class SongsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    List<Songs> mSongsList = new ArrayList<>();
    MainActivity activity = null;
    OnClickListener listener;

    /**
     * This means after how many items ad should come.
     */
    final int AD_AFTER_ITEMS = 10;

    final int TYPE_SONG_ITEM = 0;

    final int TYPE_AD_ITEM = 1;

    public SongsAdapter(Context context, List<Songs> songsList) {
        mContext = context;
        mSongsList = songsList;
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
            Picasso.with(mContext).load(Collector.getAlbumArtUri(Long.parseLong(mSongsList.get(position).getAlbumId()))).placeholder(R.drawable.default_album_art).into(songHolder.ivAlbumArt);
        } else if (holder instanceof AdHolder) {
            AdHolder adHolder = (AdHolder) holder;
            adHolder.adView.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_SONG_ITEM;
        } else {
            return position % 20 == 0 ? TYPE_AD_ITEM : TYPE_SONG_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mSongsList.size();
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

    class AdHolder extends RecyclerView.ViewHolder {

        NativeExpressAdView adView;

        public AdHolder(View itemView) {
            super(itemView);
            adView = (NativeExpressAdView) itemView.findViewById(R.id.nativeExpressAdView);
        }
    }
}
