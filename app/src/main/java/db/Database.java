package db;

import android.os.Environment;

import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.Properties;

/**
 * Created by Citrus on 13.12.2017.
 */

public class Database {
    final static String url = "jdbc:sqlite:\\app\\src\\main\\database\\lppDB.db";
    
    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        conn.setReadOnly(true);
        return conn;
    }

    public static void select() {
        String sql = "SELECT * FROM routes r" +
                "WHERE r.int_id = '332'";
        //TODO can't open db -- read only
        String extStorageState = Environment.getExternalStorageState();
        System.out.println(Environment.MEDIA_MOUNTED.equals(extStorageState));
        System.out.println(Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState));

        try (Connection conn = connect()) {
            System.out.println("Connected");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        /*if(conn != null) {
            try (Statement stm = conn.createStatement();
                 ResultSet rs = stm.executeQuery(sql)) {

                while (rs.next()) {
                    System.out.println(rs.getString("int_id") + " " +
                            rs.getString("parent_name"));
                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        else {
            System.out.println("Error with connection.");
        }*/
    }
}
