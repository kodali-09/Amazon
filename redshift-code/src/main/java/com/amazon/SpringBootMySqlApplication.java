package com.amazon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;

@SpringBootApplication
public class SpringBootMySqlApplication {

    public static void main(String[] args) throws FileNotFoundException {
        //QMRReport$.MODULE$.createQMRReport();
      //  com.spark.amazon.qmr.QMRReport.createQMRReport(RedshiftExecutor.readConfig().getJSONObject("qmr"));
        SpringApplication.run(SpringBootMySqlApplication.class, args);
    }
}
