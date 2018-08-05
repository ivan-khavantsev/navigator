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
            holder.magnetImageView = (ImageView) iConvertView.findViewById(R.id.icPointMagnet);
            holder.drawLineCheckBox = (CheckBox) iConvertView.findViewById(R.id.tvPointCheckboxLine);
            holder.drawLineCheckBox = (CheckBox) iConvertView.findViewById(R.id.tvPointCheckboxLine);
            holder.enableCheckBox = (CheckBox) iConvertView.findViewById(R.id.tvPointCheckboxEnable);
            iConvertView.setTag(holder);

        } else {
            holder = (Holder) iConvertView.getTag();
        }

        holder.magnetImageView.setVisibility(magnetVisibility);
        holder.drawLineCheckBox.setOnCheckedChangeListener(this);
        holder.enableCheckBox.setOnCheckedChangeListener(this);

        return super.getView(iPosition, iConvertView, iViewGroup);
    }

    private class Holder {
        ImageView magnetImageView = null;
        CheckBox drawLineCheckBox = null;
        CheckBox enableCheckBox = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.tvPointCheckboxLine) {
            Integer id = Integer.parseInt(((TextView) ((LinearLayout) compoundButton.getParent().getParent()).findViewById(R.id.tvPointId)).getText().toString());
            Point point = pointService.getPoint(id);
            int drawLine = compoundButton.isChecked() ? 1 : 0;
            if (point.drawLine != drawLine) {
                point.drawLine = drawLine;
                pointService.savePoint(point);
                pointsActivity.pointResult(point);
            }
        }else if(compoundButton.getId() == R.id.tvPointCheckboxEnable){
            Integer id = Integer.parseInt(((TextView) ((LinearLayout) compoundButton.getParent().getParent()).findViewById(R.id.tvPointId)).getText().toString());
            Point point = pointService.getPoint(id);
            int enable = compoundButton.isChecked() ? 1 : 0;
            if (point.enable != enable) {
                point.enable = enable;
                pointService.savePoint(point);
                pointsActivity.pointResult(point);
            }
        }


    }

}
