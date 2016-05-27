package ru.khavantsev.ziczac.navigator.dialog;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;

public class PointAddDialog extends DialogFragment implements OnClickListener {

    final String LOG_TAG = "myLogs";

    EditText etName;
    EditText etLat;
    EditText etLon;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Point data");
        View v = inflater.inflate(R.layout.dialog_point_add, null);
        v.findViewById(R.id.btnOk).setOnClickListener(this);
        v.findViewById(R.id.btnCancel).setOnClickListener(this);

        etName = (EditText) v.findViewById(R.id.dialog_point_add_name);
        etLat = (EditText) v.findViewById(R.id.dialog_point_add_lat);
        etLon = (EditText) v.findViewById(R.id.dialog_point_add_lon);


        return v;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btnOk) {


            Point point = new Point();
            point.name = etName.getText().toString();
            point.lat = etLat.getText().toString();
            point.lon = etLon.getText().toString();

            PointService ps = new PointService();
            ps.savePoint(point);

        }

        dismiss();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(LOG_TAG, "Dialog 1: onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(LOG_TAG, "Dialog 1: onCancel");
    }
}