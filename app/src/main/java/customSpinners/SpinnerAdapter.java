package customSpinners;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lppapp.ioi.lpp.R;

import java.util.ArrayList;

import tables.Shape;

/**
 * Created by susni on 23. 12. 2017.
 */

public class SpinnerAdapter extends BaseAdapter {

    private ArrayList<Shape> shapes = new ArrayList<>();
    private Context context;

    public SpinnerAdapter(Context context, ArrayList<Shape> data) {
        this.context = context;
        this.shapes = data;
    }

    @Override
    public int getCount() {
        return this.shapes.size();
    }

    @Override
    public Shape getItem(int i) {
        return shapes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.spinneritem, null);
        TextView txt = (TextView) convertView.findViewById(R.id.spinnerTextItem);
        txt.setText(shapes.get(i).toString());
        return convertView;
    }
}
