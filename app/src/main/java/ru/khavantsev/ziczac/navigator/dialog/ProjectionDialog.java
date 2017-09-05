package ru.khavantsev.ziczac.navigator.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import java.math.BigDecimal;

import ru.khavantsev.ziczac.navigator.R;
import ru.khavantsev.ziczac.navigator.activity.PointListener;
import ru.khavantsev.ziczac.navigator.db.model.Point;
import ru.khavantsev.ziczac.navigator.db.service.PointService;
import ru.khavantsev.ziczac.navigator.filter.DecimalInputTextWatcher;
import ru.khavantsev.ziczac.navigator.geo.GeoCalc;
import ru.khavantsev.ziczac.navigator.geo.LatLon;

public class ProjectionDialog extends DialogFragment implements View.OnClickListener {

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String NAME = "name";
    public static final String DECLINATION = "declination";
    private EditText etName;
    private EditText etLat;
    private EditText etLon;

    private EditText etDistance;
    private EditText etAngle;
    private CheckBox cbUseDeclination;

    private float declination;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_projection, null);
        getDialog().setTitle(R.string.title_dialog_projection);
        v.findViewById(R.id.dialog_projection_ok).setOnClickListener(this);
        v.findViewById(R.id.dialog_projection_cancel).setOnClickListener(this);

        etName = v.findViewById(R.id.dialog_projection_name);
        etName.setText(getArguments().getString(NAME));

        etLat = v.findViewById(R.id.dialog_projection_lat);
        etLat.addTextChangedListener(new DecimalInputTextWatcher(etLat, 2, 8));
        etLat.setText(getArguments().getString(LATITUDE));

        etLon = v.findViewById(R.id.dialog_projection_lon);
        etLon.addTextChangedListener(new DecimalInputTextWatcher(etLon, 2, 8));
        etLon.setText(getArguments().getString(LONGITUDE));

        etDistance = v.findViewById(R.id.dialog_projection_distance);
        etDistance.addTextChangedListener(new DecimalInputTextWatcher(etDistance, 8, 0));

        etAngle = v.findViewById(R.id.dialog_projection_angle);
        etAngle.addTextChangedListener(new DecimalInputTextWatcher(etAngle, 3, 2));

        cbUseDeclination = v.findViewById(R.id.dialog_projection_use_declination);
        declination = getArguments().getFloat(DECLINATION, 0);

        return v;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.dialog_projection_ok) {
            Point point = new Point();
            point.name = etName.getText().toString();

            double lat = Double.parseDouble(etLat.getText().toString());
            double lon = Double.parseDouble(etLon.getText().toString());
            double distance = GeoCalc.distanceToRadians(Long.parseLong(etDistance.getText().toString()));
            boolean useDeclination = cbUseDeclination.isChecked();

            double angleDegrees = Double.parseDouble(etAngle.getText().toString());
            if (useDeclination) {
                angleDegrees = GeoCalc.correctDeclination(angleDegrees, declination);
            }
            double angleRadians = Math.toRadians(angleDegrees);

            LatLon projectionLatLon = GeoCalc.projection(new LatLon(lat, lon), distance, angleRadians);

            double doubleLat = new BigDecimal(projectionLatLon.latitude).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            double doubleLon = new BigDecimal(projectionLatLon.longitude).setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();

            point.lat = String.valueOf(doubleLat);
            point.lon = String.valueOf(doubleLon);

            PointService ps = new PointService();
            ps.savePoint(point);
            ((PointListener) getActivity()).pointResult(point);
        }
        dismiss();
    }
}
