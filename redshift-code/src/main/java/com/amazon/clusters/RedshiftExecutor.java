package com.amazon.clusters;


import com.amazon.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class RedshiftExecutor {

    private static JSONObject config = null;

    public static Set<String> getClusters() {
        try {
            return RedshiftExecutor.readConfig().getJSONObject("clusters").keySet();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject readConfig() throws FileNotFoundException {
        if (config == null) {
            BufferedReader bf = new BufferedReader(new FileReader("src/main/resources/config.json"));
            config = new JSONObject(bf.lines().collect(Collectors.joining()));
        }
        return config;
    }

    static Connection connection = null;

    public static Connection getConnection(JSONObject json) throws SQLException, ClassNotFoundException {
        if (connection == null) {
            String redshiftUrl = "jdbc:redshift://" + json.get("host") + ":" + json.get("port") + "/" + json.get("db") + "?ssl=true";
            String masterUsername = json.getString("username");
            String password = json.getString("password");

            Class.forName("com.amazon.redshift.jdbc42.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", masterUsername);
            properties.setProperty("password", password);
            connection = DriverManager.getConnection(redshiftUrl, properties);
        }

        return connection;
    }

    public static JSONArray getPK(Connection con, String schema, String table) throws SQLException {
        String query = String.format(Constants.PK_TEMPLATE, schema, table);

        Statement stmt = con.createStatement();
        stmt.execute("SET search_path TO " + schema + ", public");
        ResultSet rs = stmt.executeQuery(query);
        JSONArray result = new JSONArray();
        System.out.println(query);
        while (rs.next()) {
            JSONObject row = new JSONObject();
            row.put("number", rs.getString(1));
            row.put("table_name", rs.getString(2));
            row.put("column_name", rs.getString(3));

            result.put(row);
        }
        return result;
    }

    public static JSONArray getDKSK(Connection con, String schema, String table) throws SQLException {
        String query = String.format(Constants.DK_SK_TEMPLATE, schema, table);

        Statement stmt = con.createStatement();
        stmt.execute("SET search_path TO " + schema + ", public");
        ResultSet rs = stmt.executeQuery(query);
        JSONArray result = new JSONArray();
        while (rs.next()) {
            JSONObject row = new JSONObject();
            row.put("column", rs.getString(1));
            row.put("type", rs.getString(2));
            row.put("distkey", rs.getString(3));
            row.put("sortkey", rs.getString(4));

            result.put(row);
        }
        return result;
    }

    public static JSONArray gettables(String cluster) {
        Connection con = null;
        ResultSet rs = null;
        JSONArray output = new JSONArray();

        try {

            JSONObject json = readConfig().getJSONObject("clusters").getJSONObject(cluster);

            con = getConnection(json);

           String query = "select table_schema||'.'||table_name as table from information_schema.tables where table_schema not in ('information_schema', 'pg_catalog') and table_type = 'BASE TABLE' order by 1;";
            // System.out.println("executing query: " + query);

            Statement stmt = con.createStatement();
             rs = stmt.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();

            while(rs.next()) {
                int numColumns = rsmd.getColumnCount();
                JSONObject obj = new JSONObject();

                for (int i=1; i<numColumns+1; i++) {
                    String column_name = rsmd.getColumnName(i);
                    if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                        output.put(rs.getNString(column_name));
                    }
                    else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                        output.put(rs.getString(column_name));
                    }
                }
            }

            return output;

        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        }
        finally {
//            try {
//                if (con != null) con.close();
//                System.out.println("Disconnected!!");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        return output;
    }

    public static JSONObject getDataRetention(String schema, String table, String cluster, String column) {
        Connection con = null;
        JSONObject output = new JSONObject();

        try {

            JSONObject json = readConfig().getJSONObject("clusters").getJSONObject(cluster);

            con = getConnection(json);

            String updatedColumns = Arrays.stream(column.split(",")).map(value -> "'" + value.trim() + "'").collect(Collectors.joining(","));
            String query = String.format(Constants.DATETIME_TEMPLATE, schema, table, updatedColumns);
           // System.out.println("executing query: " + query);

            Statement stmt = con.createStatement();
            stmt.execute("SET search_path TO " + schema + ", public");
            ResultSet rs = stmt.executeQuery(query);
            StringBuilder sb = new StringBuilder("select ");
            JSONArray columns = new JSONArray();
            while (rs.next()) {
                String columnName = rs.getString(1);
                sb.append("min(" + columnName + "), ");
                sb.append("max(" + columnName + "), ");
                columns.put("min(" + columnName + ")");
                columns.put("max(" + columnName + ")");

            }
            if (sb.toString().equals("select ")) {
                output.put("error", "column not present");
                return output;
            }
            sb.deleteCharAt(sb.length() - 2);
            sb.append(" from " + schema + "." + table);
            String queryForRetention = sb.toString();

//            System.out.println("executing query: " + queryForRetention);
            ResultSet rs1 = stmt.executeQuery(queryForRetention);

            while (rs1.next()) {
                for (int i = 0; i < columns.length(); i++) {
                    output.put(columns.getString(i), rs1.getString(i + 1));
                }
            }
            return output;

        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        } finally {
//            try {
//                if (con != null) con.close();
//                System.out.println("Disconnected!!");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        return output;
    }

    public static JSONArray getLargeTabes(String cluster, String limit, String refresh) {
        Connection con = null;
        JSONArray result = new JSONArray();

        String query = Constants.SIZE_TEMPLATE + " limit " + limit;

        if (refresh != "Y") {
            result = loadFromCache(cluster);
        }

        if (result.length() != 0) {
            return result;
        }

        ResultSet rs;
        try {

            JSONObject json = readConfig().getJSONObject("clusters").getJSONObject(cluster);

            con = getConnection(json);
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("schema", rs.getString(1));
                row.put("table", rs.getString(2));
                row.put("diststyle", rs.getString(3));
                row.put("sortkey1", rs.getString(4));
                row.put("sortkey1_enc", rs.getString(5));
                row.put("sortkey_num", rs.getString(6));
                row.put("size", rs.getString(7));
                row.put("size_in_GB", rs.getString(8));
                row.put("tbl_rows", rs.getString(9));
                row.put("skew_sortkey1", rs.getString(10));
                row.put("vacuum_sort_benefit", rs.getString(11));

                result.put(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
                System.out.println("Disconnected!!");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    private static JSONArray loadFromCache(String cluster) {
        try {
            String location = "src/main/resources/" + cluster + "_large_tables.json";
            File f = new File(location);
            if (f.exists() && !f.isDirectory()) {
                BufferedReader bf = new BufferedReader(new FileReader(location));
                return new JSONArray(bf.lines().collect(Collectors.joining()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray();

    }


    public static JSONObject getTableInfo(String schema, String table, String cluster) {

        Connection con = null;
        JSONObject output = new JSONObject();

        try {

            JSONObject json = readConfig().getJSONObject("clusters").getJSONObject(cluster);

            con = getConnection(json);
            output.put("pk", getPK(con, schema, table));
            output.put("dksk", getDKSK(con, schema, table));

        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        } finally {
            try {
                if (con != null) con.close();
                System.out.println("Disconnected!!");
            } catch (Exception e) {
                e.printStackTrace();
                ;
            }
        }
        return output;
    }



    public static void main(String... args) throws FileNotFoundException, SQLException, ClassNotFoundException {
        Connection con = null;
        try {
            //JSONArray result = getLargeTabes("pandora_na", "10", "N");
//            JSONObject result = getTableInfo("tcda", "trans_cust_shipment_items", "pandora_na");
            String date_col = "loaded_at,snapshot_day,creation_date,dw_last_updated,last_updated,dw_last_updated_date,ship_day,last_updated_date,dw_creation_date";
            JSONObject result = new JSONObject();//getDataRetention("tcda", "trans_cust_shipment_items", "pandora_na", "pawan");
            JSONArray tableArray =gettables("bitsstaging1");
            for(int i = 0 ; i < tableArray.length() ; i++){
                String table = tableArray.get(i).toString();
                String schema = table.split("\\.")[0];
                String tableName = table.split("\\.")[1];
                result = getDataRetention(schema, tableName, "bitsstaging1", date_col);
                System.out.println( table + ": " + result);
            }

        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        } finally {
            if (con != null) con.close();
            System.out.println("Disconnected!!");
        }
    }
}
