package com.amazon.clusters;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class RedshiftConnector {

    public static final String QUERY_TEMPLATE = "select *,  case when(end_datetime_utc < SLA_DATETIME) then 'YES' else 'NO' end as WITHIN_SLA from (   select datediff(MINUTE ,min(start_datetime_utc) , max(end_datetime_utc )) as duration,     convert_timezone(case when timezone_description != 'Europe/London' then 'PST' else 'GMT' end, min(start_datetime_utc)) start_datetime_utc, convert_timezone(case when timezone_description != 'Europe/London' then 'PST' else 'GMT' end, max(end_datetime_utc)) end_datetime_utc,   cast(concat(TRUNC(convert_timezone(case when timezone_description != 'Europe/London' then 'PST' else 'GMT' end, max(end_datetime_utc))),' 06:00:00') as timestamp) SLA_DATETIME, TRUNC(a.dataset_day) as dataset_date, timezone_description, a.job_id from booker.d_dwp_job_runs a where a.job_id in (JOB_ID) and TRUNC(a.dataset_day) between 'START_DATE' and 'END_DATE' group by TRUNC(a.dataset_day), timezone_description,a.job_id order by TRUNC(a.dataset_day) )";




    public static void main(String[] args) {
        getJSONResult();
    }

    public static void getJSONResult() {
        Connection connection = null;
        Statement statement = null;

        try {
            JSONObject json = RedshiftExecutor.readConfig().getJSONObject("clusters").getJSONObject("bitsstaging2");
            System.out.println(json);
            String redshiftUrl = "jdbc:redshift://"+json.get("host")+":"+json.get("port")+"/" + json.get("db")+"?ssl=true" ;
            String masterUsername = json.getString("username");
            String password = json.getString("password");
            String start_date = json.getString("start_date");
            String end_date = json.getString("end_date");
            String job_id = json.getString("ids");

            Class.forName("com.amazon.redshift.jdbc42.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", masterUsername);
            properties.setProperty("password", password);
            connection = DriverManager.getConnection(redshiftUrl, properties);



            String query = QUERY_TEMPLATE.replace("JOB_ID",job_id).replace("START_DATE",start_date).replace("END_DATE",end_date);

            System.out.println("Connected!! " + query);

            ResultSet rs = connection.createStatement().executeQuery(query);
            JSONArray result = new JSONArray();
            while(rs.next()){
                JSONObject row = new JSONObject();
                row.put("duration", rs.getString(1));
                row.put("start_datetime_utc", rs.getString(2));
                row.put("end_datetime_utc", rs.getString(3));
                row.put("sla_datetime", rs.getString(4));
                row.put("dataset_date", rs.getString(5));
                row.put("timezone_description", rs.getString(6));
                row.put("job_id", rs.getLong(7));
                row.put("within_sla", rs.getString(8));

                result.put(row);
            }

            System.out.println(result);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
