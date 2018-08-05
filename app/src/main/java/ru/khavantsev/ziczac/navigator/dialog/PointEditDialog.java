package ru.khavantsev.ziczac.navigator.dialog;

import android.support.v4.app.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.activity.PointListener;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;
import ru.khavantsev.ziczac.navigator.filter.DecimalInputTextWatcher;

public class PointEditDialog extends DialogFragment implements OnClickListener {

    private EditText etName;
    private EditText etLat;
    private EditText etLon;

    private long pointId;
    private Point point;

    PointService pointService = new PointService();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_point_add, null);
        getDialog().setTitle(R.string.title_dialog_point);
        v.findViewById(R.id.dialog_point_add_ok).setOnClickListener(this);
        v.findViewById(R.id.dialog_point_add_cancel).setOnClickListener(this);

        etName = (EditText) v.findViewById(R.id.dialog_point_add_name);

        etLat = (EditText) v.findViewById(R.id.dialog_point_add_lat);
        etLat.addTextChangedListener(new DecimalInputTextWatcher(etLat, 2, 8));

        etLon = (EditText) v.findViewById(R.id.dialog_point_add_lon);
        etLon.addTextChangedListener(new DecimalInputTextWatcher(etLon, 3, 8));


        pointId = getArguments().getLong("pointId", 0);
        if(pointId != 0){
            point = pointService.getPoint(pointId);
            etName.setText(point.name);
            etLat.setText(point.lat);
            etLon.setText(point.lon);
        }else{
            etName.setText(getArguments().getString("name"));
            etLat.setText(getArguments().getString("latitude"));
            etLon.setText(getArguments().getString("longitude"));
        }

        return v;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.dialog_point_add_ok) {
            if(point == null){
                point = new Point();
            }
            point.name = etName.getText().toString();
            point.lat = etLat.getText().toString();
            point.lon = etLon.getText().toString();

            PointService ps = new PointService();
            point.id = ps.savePoint(point);
            ((PointListener) getActivity()).pointResult(point);
        }
        dismiss();
    }
}