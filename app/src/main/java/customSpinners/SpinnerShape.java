package customSpinners;

import android.view.View;
import android.widget.AdapterView;


/**
 * Created by Citrus on 15.12.2017.
 */

public class SpinnerShape implements AdapterView.OnItemSelectedListener {

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Shape sh = (Shape) adapterView.getSelectedItem();
        System.out.println(sh.shape_id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
