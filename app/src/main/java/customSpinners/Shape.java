package customSpinners;

/**
 * Created by Citrus on 15.12.2017.
 */

public class Shape {

    public String shape_id      = "";
    public String route_name    = "";
    public String trip_headsign = "";

    public Shape(String shape_id, String route_name, String trip_headsign) {
        this.shape_id       = shape_id;
        this.route_name     = route_name;
        this.trip_headsign  = trip_headsign;
    }

    public String toString() {
        return this.route_name + " " + this.trip_headsign;
    }
}
