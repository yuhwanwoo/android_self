package multi.android.infortainmentw.music;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import multi.android.infortainmentw.MainActivity;
import multi.android.infortainmentw.R;

public class MusicAdapter extends BaseAdapter {

    ArrayList<MusicDTO> list;
    LayoutInflater inflater;
    Activity activity;
    MusicFragment musicFragment;

    public MusicAdapter(){

    }


    public MusicAdapter(ArrayList<MusicDTO> list, Activity activity) {
        this.list = list;
        this.activity = activity;
        Log.d("확인하는중","되는지");
        inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    /*public MusicAdapter(MusicFragment fragment,ArrayList<MusicDTO> list){
        this.list=list;
        this.musicFragment=fragment;
        inflater=fragment.getLayoutInflater();
    }*/




    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView=inflater.inflate(R.layout.musiclistview_item,parent,false);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            convertView.setLayoutParams(layoutParams);
        }

        ImageView imageView=convertView.findViewById(R.id.album_item);
        Bitmap albumImage=getAlbumImage(activity,Integer.parseInt((list.get(position)).getAlbumId()),170);
        imageView.setImageBitmap(albumImage);

        TextView title=convertView.findViewById(R.id.title_item);
        Log.d("확인하는중",title+":::::::title은?");
        title.setText(list.get(position).getTitle());

        TextView artist=convertView.findViewById(R.id.artist_item);
        artist.setText(list.get(position).getArtist());


        return convertView;
    }

    private static final BitmapFactory.Options options=new BitmapFactory.Options();

    private static Bitmap getAlbumImage(Context context,int album_id,int MAX_IMAGE_SIZE) {
        ContentResolver res = context.getContentResolver();
        Uri uri = Uri.parse("content://medis/external/audio/albumart/" + album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");

                options.inJustDecodeBounds=true;
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,options);

                int scale=0;
                if(options.outHeight>MAX_IMAGE_SIZE || options.outWidth>MAX_IMAGE_SIZE){
                    scale=(int)Math.pow(2,(int)Math.round(Math.log(MAX_IMAGE_SIZE)/(double)Math.max(options.outHeight,options.outWidth))/Math.log(0.5));
                }
                options.inJustDecodeBounds=false;
                options.inSampleSize=scale;

                Bitmap b=BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,options);

                if (b!=null){
                    if(options.outWidth!=MAX_IMAGE_SIZE || options.outHeight != MAX_IMAGE_SIZE){
                        Bitmap tmp=Bitmap.createScaledBitmap(b,MAX_IMAGE_SIZE,MAX_IMAGE_SIZE,true);
                        b.recycle();
                        b=tmp;
                    }
                }
                return b;

            } catch (FileNotFoundException e) {

            } finally {
                try {
                    if (fd != null) {
                        fd.close();
                    }
                } catch (IOException e) {

                }
            }

        }
        return null;
    }
}
