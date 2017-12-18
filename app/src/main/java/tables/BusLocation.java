package tables;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Citrus on 18.12.2017.
 */

public class BusLocation {

    public int bus_id;
    public String reg_number;
    public int speed;
    public int route_int_id;
    public int station_int_id;
    public Double lat;
    public Double lon;
    public Date local_time;

    public BusLocation(int bus_id, String reg, int speed, int route, int station, Double lat, Double lon, String time) {
        this.bus_id = bus_id;
        this.reg_number = reg;
        this.speed = speed;
        this.route_int_id = route;
        this.station_int_id = station;
        this.lat = lat;
        this.lon = lon;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            this.local_time = df.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
