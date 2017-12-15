package tables;

/**
 * Created by Citrus on 15.12.2017.
 */

public class Stop {

    public int stop_id;
    public Double latitude;
    public Double longitude;
    public String stop_name;

    public Stop(int stop_id, Double latitude, Double longitude, String stop_name) {
        this.stop_id        = stop_id;
        this.latitude       = latitude;
        this.longitude      = longitude;
        this.stop_name      = stop_name;
    }
}
