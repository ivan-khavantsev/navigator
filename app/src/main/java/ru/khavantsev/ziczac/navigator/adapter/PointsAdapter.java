package ru.khavantsev.ziczac.navigator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import ru.khavantsev.ziczac.navigator.R;

import java.util.List;
import java.util.Map;

public class PointsAdapter extends SimpleAdapter {

    private LayoutInflater mInflater = null;
    private int magnetVisibility = View.GONE;

    public PointsAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mInflater = LayoutInflater.from(context);
    }

    public void setMagnetDeclinationUsing(boolean using) {
        if (using) {
            magnetVisibility = View.VISIBLE;
        } else {
            magnetVisibility = View.GONE;
        }
    }

    @Override
    public View getView(int iPosition, View iConvertView, ViewGroup iViewGroup) {
        Holder holder = new Holder();

        if (iConvertView == null) {
            iConvertView = mInflater.inflate(R.layout.point_item, null);
            holder.mImageView = (ImageView) iConvertView.findViewById(R.id.icPointMagnet);
            iConvertView.setTag(holder);

        } else {
            holder = (Holder) iConvertView.getTag();
        }

        holder.mImageView.setVisibility(magnetVisibility);

        return super.getView(iPosition, iConvertView, iViewGroup);
    }

    private class Holder {
        ImageView mImageView = null;
    }

}
