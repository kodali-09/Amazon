var controller = function($scope) {
    $.$scope = $scope

    $scope.appContext = {
        activeMenu :"home",
        cluster :"pandora_na",
        tableInformation: true,
        largeTables: false,
        columnList: "loaded_at,dw_last_updated,snapshot_day,creation_date",
        tableInfo : {
    "dksk": [{
        "sortkey": "0",
        "column": "fulfillment_shipment_id",
        "type": "numeric(38,0)",
        "distkey": "true"
    }, {
        "sortkey": "2",
        "column": "partition_key",
        "type": "timestamp without time zone",
        "distkey": "false"
    }, {
        "sortkey": "-1",
        "column": "dw_last_updated",
        "type": "timestamp without time zone",
        "distkey": "false"
    }],
    "pk": [{
        "number": "1",
        "column_name": "trans_cust_shipment_item_id",
        "table_name": "trans_cust_shipment_items"
    }, {
        "number": "25",
        "column_name": "fulfillment_shipment_id",
        "table_name": "trans_cust_shipment_items"
    }, {
        "number": "75",
        "column_name": "record_version_number",
        "table_name": "trans_cust_shipment_items"
    }]
}
    }


    $scope.exportExcel = function(){
        var uri = 'data:application/vnd.ms-excel;base64,'
        , template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--><meta http-equiv="content-type" content="text/plain; charset=UTF-8"/></head><body><table>{table}</table></body></html>'
        , base64 = function(s) { return window.btoa(unescape(encodeURIComponent(s))) }
        , format = function(s, c) { return s.replace(/{(\w+)}/g, function(m, p) { return c[p]; }) }
        
        table = document.getElementById("table")
        var ctx = {worksheet: name || 'Worksheet', table: table.innerHTML}
        window.location.href = uri + base64(format(template, ctx))

    }

    $scope.getClass = function(menu){
      if($scope.appContext.activeMenu  == menu){
        return 'activeMenu'
      }
      return 'inactive'
    }

    $scope.updateCluster = function(cluster){
      $scope.appContext.cluster = cluster || $scope.appContext.cluster
    }

    $scope.allowPrint = false
    $scope.errorMessage = ""



    $scope.getClusters = function() {

        $scope.appContext.loading = true;
        $.get("/getClusters",
            function(result, status) {
                        $scope.appContext.loading = false;
                $scope.appContext.clusters = JSON.parse(result)
                $scope.$apply()
            })

    }

    $scope.getLargeTables = function(refresh) {
            $scope.appContext.largeTables = true

      $scope.appContext.loading = true;
      $.get("/getLargeTables?cluster="+$scope.appContext.cluster + "&limit=200&refresh=" + refresh || 'N',
            function(result, status) {
                      $scope.appContext.loading = false;
                $scope.appContext.largeTables = JSON.parse(result)
                $scope.$apply()
            })

    }

    $scope.getTableInfo = function(schema, table) {
      $scope.appContext.tableInformation = true
      $scope.appContext.schema = schema
      $scope.appContext.table = table

      $scope.appContext.loading = true;
      $.get("/getTableInfo?cluster="+$scope.appContext.cluster + "&schema="+schema + "&table="+table,
        function(result, status) {
                $scope.appContext.loading = false;
                $scope.appContext.tableInfo = JSON.parse(result)
                $scope.appContext.columnStats = {}
                $scope.appContext.columnStatsCols = []
                $scope.$apply()
              })
      }
    
    $scope.getDataRetention = function(schema, table) {
      $scope.appContext.tableInformation = true
      $scope.appContext.schema = schema
      $scope.appContext.table = table

      $scope.appContext.loading = true;
      $.get("/getDataRetention?columnList="+ $scope.appContext.columnList +"&cluster="+$scope.appContext.cluster + "&schema="+schema + "&table="+table,
        function(result, status) {
                $scope.appContext.loading = false;
                $scope.appContext.columnStats = JSON.parse(result)
                $scope.appContext.columnStatsCols = Object.keys($scope.appContext.columnStats)

                $scope.$apply()
              })
      }

    $scope.init = function(unit) {
        // $scope.records = sample;
        $scope.getClusters()
    }


    

  $scope.init()

  // method for display table
  $scope.table_data = []
  $scope.table_columns = []

  $scope.setTableData = function (data) {
    $scope.table_data = JSON.parse(data)
    $scope.table_columns = Object.keys($scope.table_data[0])
  }


  $scope.executeSparkQuery = function() {
      $scope.errorMessage = ""

      $scope.appContext.loading = true;
      $.get("/executeSparkQuery?query="+ $scope.appContext.sparkQuery,
        function(result, status) {
                $scope.appContext.loading = false;
                var sparkdata = JSON.parse(result)
                if(sparkdata.error !=undefined) {
                  $scope.setTableData(sparkdata.success)
                } else{
                  $scope.errorMessage = sparkdata.error
                }

                $scope.$apply()
              })
      }

  $.$scope = $scope

}

 
       
 
