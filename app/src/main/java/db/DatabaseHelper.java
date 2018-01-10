package db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import tables.BusLocation;
import tables.Stop;

public class DatabaseHelper extends SQLiteOpenHelper {

    //private static String DB_PATH = "/data/data/com.lppapp.ioi.lpp/databases/";
    private static String DB_NAME = "lppDB.db";

    private SQLiteDatabase db;
    private final Context myContext;
    private ContextWrapper contextWrapper;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
        this.contextWrapper = new ContextWrapper(this.myContext);
    }

    /**
     * Creates an empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{
            //By calling this method an empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                Log.e("ERROR", "Error copying database.");
            } finally {
                this.close();
            }
        }

    }

    /**
     * Check if the database already exists to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
        File dbFile = myContext.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        //String outFileName = DB_PATH + DB_NAME;
        String outFileName = contextWrapper.getDatabasePath(DB_NAME).toString();

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //Transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    private void openDB() throws SQLException {
        //db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
        db = SQLiteDatabase.openDatabase(contextWrapper.getDatabasePath(DB_NAME).toString(), null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if(db != null)
            db.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Selects all stops from database, returns ArrayList<String>
     * */
    public ArrayList<Dictionary<String, String>> selectAllStops() {
        ArrayList<Dictionary<String, String>> list = new ArrayList<>();

        try {
            openDB();
            Cursor res = db.rawQuery("SELECT * FROM stops", null);
            res.moveToFirst();

            while(!res.isAfterLast()) {
                Dictionary<String, String> d = new Hashtable<>();

                d.put("stop_id", res.getString(res.getColumnIndex("stop_id")));
                d.put("stop_name", res.getString(res.getColumnIndex("stop_name")));
                d.put("latitude", res.getString(res.getColumnIndex("latitude")));
                d.put("longitude", res.getString(res.getColumnIndex("longitude")));
                d.put("stop_buses", res.getString(res.getColumnIndex("stop_buses")));

                list.add(d);
                res.moveToNext();
            }

            res.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return list;
    }

    public int numRows(String tableName) {
        int numRows = -1;

        try {
            openDB();
            numRows = (int) DatabaseUtils.queryNumEntries(db, tableName);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return numRows;
    }

    public ArrayList<Dictionary<String, String>> selectAllShapes() {
        ArrayList<Dictionary<String, String>> list = new ArrayList<>();

        try {
            openDB();
            Cursor res = db.rawQuery("SELECT * FROM shapes ORDER BY cast(shapes.route_name as integer)", null);
            res.moveToFirst();

            while(!res.isAfterLast()) {
                Dictionary<String, String> d = new Hashtable<>();

                d.put("shape_id", res.getString(res.getColumnIndex("shape_id")));
                d.put("route_name", res.getString(res.getColumnIndex("route_name")));
                d.put("trip_headsign", res.getString(res.getColumnIndex("trip_headsign")));

                list.add(d);
                res.moveToNext();
            }

            res.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }


        return list;
    }

    public String getShapePointsByShapeId(String shapeId) {
        String result = "";

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT points FROM shapes WHERE shape_id = ?", new String[] {shapeId});
            cursor.moveToFirst();

            result = cursor.getString(0);
            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }


        return result;
    }

    public String getStopsByShapeId(String shapeId) {
        String result = "";

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT stops FROM shapes_stops WHERE shape_id = ?", new String[] {shapeId});
            cursor.moveToFirst();

            result = cursor.getString(0);
            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return result;
    }

    /**
     * Returns json_encoded String of all buses on stop_id
     * */
    public String getBusesOnStop(int stopId) {
        String result = "";

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT stop_buses FROM stops WHERE stop_id = ?", new String[] {Integer.toString(stopId)});
            cursor.moveToFirst();

            result = cursor.getString(0);

            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }


        return result;
    }

    public HashMap<Integer, BusLocation> getBusLocationByRouteId(String routeId, String currTime, String nextTime) {
        //ArrayList<BusLocation> list = new ArrayList<>();
        HashMap<Integer, BusLocation> list = new HashMap<>();

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT * FROM locations " +
                                        "WHERE strftime('%d %H:%M:%S', locations.local_time) between " +
                                        "? and ? and locations.route_int_id = ? " +
                                        "GROUP BY locations.bus_id " +
                                        "ORDER BY locations.local_time ",
                                    new String[] {currTime, nextTime, routeId});

            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                BusLocation bus = new BusLocation(cursor.getInt(cursor.getColumnIndex("bus_id")),
                                                cursor.getString(cursor.getColumnIndex("reg_number")),
                                                cursor.getInt(cursor.getColumnIndex("speed")),
                                                cursor.getInt(cursor.getColumnIndex("route_int_id")),
                                                cursor.getInt(cursor.getColumnIndex("station_int_id")),
                                                cursor.getDouble(cursor.getColumnIndex("lat")),
                                                cursor.getDouble(cursor.getColumnIndex("lon")),
                                                cursor.getString(cursor.getColumnIndex("local_time")));

                list.put(cursor.getInt(cursor.getColumnIndex("bus_id")), bus);

                cursor.moveToNext();
            }

            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return list;
    }

    public ArrayList<String> getRouteIdByRouteName(String route_name) {
        ArrayList<String> list = new ArrayList<>();

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT * FROM routes " +
                                        "WHERE group_name = ? ",
                    new String[] {route_name});

            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                list.add(cursor.getString(cursor.getColumnIndex("int_id")));

                cursor.moveToNext();
            }

            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return list;
    }

    public ArrayList<String> getRouteIdByHeadsign(String trip_headsign) {
        ArrayList<String> list = new ArrayList<>();

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT * FROM routes " +
                            "WHERE parent_name = ? ",
                    new String[] {trip_headsign});

            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                list.add(cursor.getString(cursor.getColumnIndex("int_id")));

                cursor.moveToNext();
            }

            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return list;
    }

    public Stop getStopById(int stopId) {
        Stop stop = null;

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT * FROM stops " +
                                        "WHERE stop_id = ? ",
                    new String[] {Integer.toString(stopId)});

            cursor.moveToFirst();

            stop = new Stop(cursor.getInt(cursor.getColumnIndex("stop_id")),
                            cursor.getDouble(cursor.getColumnIndex("latitude")),
                            cursor.getDouble(cursor.getColumnIndex("longitude")),
                            cursor.getString(cursor.getColumnIndex("stop_name")));

            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return stop;
    }

    public boolean containsStop(int stopId) {
        Stop stop = null;

        try {
            openDB();
            Cursor cursor = db.rawQuery("SELECT * FROM stops " +
                                        "WHERE stop_id = ? ",
                    new String[] {Integer.toString(stopId)});

            cursor.moveToFirst();

            if(cursor.getCount() > 0) {
                return true;
            }

            cursor.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            this.close();
        }

        return false;
    }
}