package tables;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lppapp.ioi.lpp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by susni on 23. 12. 2017.
 */

public class ListViewAdapter extends BaseAdapter {

    private Context context = null;
    private List<String> fields = null;

    public ListViewAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        fields = data;
        //fields = new ArrayList<>();
        //fields.add("8 - prihod čez 4 min");
        //fields.add("13 - prihd čez 2 min");
        //fields.add("2 - prihod čez 1 min");
        //fields.add("3 - prihod čez 1 min");
        //fields.add("4 - prihod čez 1 min");

    }

    @Override
    public int getCount() {
        return fields.size();
    }

    @Override
    public Object getItem(int i) {
        return fields.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.listviewitem, null);
        TextView txt = (TextView) convertView.findViewById(R.id.viewItemText);
        TextView txtTime = (TextView) convertView.findViewById(R.id.viewItemTime);
        txt.setText(fields.get(i).split(" , ")[0]);
        txtTime.setText(fields.get(i).split(" , ")[1]);
        return convertView;
    }
}
