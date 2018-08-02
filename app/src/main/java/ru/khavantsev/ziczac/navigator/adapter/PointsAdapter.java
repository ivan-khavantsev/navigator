package ru.khavantsev.ziczac.navigator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.activity.PointsActivity;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PointsAdapter extends SimpleAdapter implements CompoundButton.OnCheckedChangeListener {

    private LayoutInflater mInflater = null;
    private int magnetVisibility = View.GONE;
    private PointService pointService;
    private PointsActivity pointsActivity;

    public PointsAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        pointsActivity = (PointsActivity) context;
        mInflater = LayoutInflater.from(context);
        pointService = new PointService();
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
            holder.mCheckBox = (CheckBox) iConvertView.findViewById(R.id.tvPointCheckboxLine);
            iConvertView.setTag(holder);

        } else {
            holder = (Holder) iConvertView.getTag();
        }

        holder.mImageView.setVisibility(magnetVisibility);
        holder.mCheckBox.setOnCheckedChangeListener(this);

        return super.getView(iPosition, iConvertView, iViewGroup);
    }

    private class Holder {
        ImageView mImageView = null;
        CheckBox mCheckBox = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Integer id = Integer.parseInt(((TextView)((LinearLayout) compoundButton.getParent().getParent()).findViewById(R.id.tvPointId)).getText().toString());
        Point point = pointService.getPoint(id);
        int drawLine = compoundButton.isChecked()?1:0;
        if(point.drawLine != drawLine){
            point.drawLine = drawLine;
            pointService.savePoint(point);
            pointsActivity.pointResult(point);
        }
    }

}
