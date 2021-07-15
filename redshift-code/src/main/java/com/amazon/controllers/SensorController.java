package com.amazon.controllers;

import java.io.IOException;
import java.util.Map;

import com.amazon.clusters.RedshiftExecutor;
//import com.amazon.spark.QMRReport;
//import com.spark.amazon.qmr.QMRReport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.amazon.commands.SensorForm;
import com.amazon.converters.SensorToSensorForm;
import com.amazon.domain.Sensor;
import com.amazon.services.SensorService;

/**
 * Created by jt on 1/10/17.
 */
@RestController
@Controller
public class SensorController {
	private SensorService sensorService;

	private SensorToSensorForm sensorTosensorForm;

	@Autowired
	public void setsensorTosensorForm(SensorToSensorForm sensorTosensorForm) {
		this.sensorTosensorForm = sensorTosensorForm;
	}

	@Autowired
	public void setsensorService(SensorService sensorService) {
		this.sensorService = sensorService;
	}

	// @RequestMapping("/")
	// public String redirToList() {
	// return "redirect:/sensor/list";
	// }

	// @RequestMapping("/login")
	// public String redirToLogin() {
	// return "redirect:/login";
	// }

	@RequestMapping({ "/sensor/list", "/sensor" })
	public String listsensors(Model model) {
		model.addAttribute("sensors", sensorService.listAll());
		return "sensor/list";
	}

	@RequestMapping("/sensor/show/{id}")
	public String getsensor(@PathVariable String id, Model model) {
		model.addAttribute("sensor", sensorService.getById(Long.valueOf(id)));
		return "sensor/show";
	}

	@RequestMapping("sensor/edit/{id}")
	public String edit(@PathVariable String id, Model model) {
		Sensor sensor = sensorService.getById(Long.valueOf(id));
		SensorForm sensorForm = sensorTosensorForm.convert(sensor);

		model.addAttribute("sensorForm", sensorForm);
		return "sensor/sensorform";
	}

	@RequestMapping("/sensor/new")
	public String newsensor(Model model) {
		model.addAttribute("sensorForm", new SensorForm());
		return "sensor/sensorform";
	}

	@RequestMapping(value = "/sensor", method = RequestMethod.POST)
	public String saveOrUpdatesensor(SensorForm sensorForm, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return "sensor/sensorform";
		}

		Sensor savedsensor = sensorService.saveOrUpdateSensorForm(sensorForm);

		return "redirect:/sensor/show/" + savedsensor.get_id();
	}

	@RequestMapping("/sensor/delete/{id}")
	public String delete(@PathVariable String id) {
		sensorService.delete(Long.valueOf(id));
		return "redirect:/sensor/list";
	}

	@GetMapping(value = "/saveSensorData")
	@ResponseBody
	public String saveSensorData(@RequestParam Map<String, String> sensorForm) {
		System.out.println(sensorForm);
		final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		final Sensor sensor = mapper.convertValue(sensorForm, Sensor.class);
		sensorService.saveOrUpdate(sensor);
		return "{'get': 'ok'}";
	}

	// @PostMapping(value = "/saveSensorDataPOST")
	@RequestMapping(value = "/saveSensorDataPOST", method = RequestMethod.POST, headers = "Accept=application/json")
	@ResponseBody
	public String saveSensorDataPOST(@RequestBody String sensorForm) {
		try {
			System.out.println("sensorForm : " + sensorForm);

			sensorForm = sensorForm.replaceAll("'", "\"");
			System.out.println("sensorForm after update : " + sensorForm);
			JSONObject input = new JSONObject(sensorForm);
			final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			final Sensor sensor = mapper.readValue(sensorForm, Sensor.class);
			System.out.println("sensor: " + sensor.toString());
			sensorService.saveOrUpdate(sensor);
			return "{'post': 'ok'}";
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "{'post': '" + e.getMessage() + "'}";

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "{'post': '" + e.getMessage() + "'}";
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "{'post': '" + e.getMessage() + "'}";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "{'post': '" + e.getMessage() + "'}";
		}
	}

	@GetMapping("/allSensor")
	@ResponseBody
	public String allSensor() {
		JSONArray json = new JSONArray(sensorService.listAll());
		return json.toString();
	}

	@GetMapping("/allUnits")
	@ResponseBody
	public String allUnits() {
		JSONArray json = new JSONArray(sensorService.listUnits());
		return json.toString();
	}

	@GetMapping(value = "/getDataByUnit")
	@ResponseBody
	public String getDataByUnit(@RequestParam Map<String, String> inputData) {
		System.out.println(inputData);
		JSONArray json = new JSONArray(sensorService.getDataByUnit(inputData.get("unit")));
		return json.toString();
	}

	@GetMapping(value = "/getStatsByUnit")
	@ResponseBody
	public String getStatsByUnit(@RequestParam Map<String, String> inputData) {
		System.out.println(inputData);
		return sensorService.getStatsByUnit(inputData.get("unit")).toString();
		// return json.toString();
	}

	@GetMapping(value = "/getRodentStatsByUnit")
	@ResponseBody
	public String getRodentStatsByUnit(@RequestParam Map<String, String> inputData) {

		return new JSONArray(sensorService.getRodentStatsByUnit(inputData.get("unit"))).toString();
		// return json.toString();
	}

	@GetMapping(value = "/getCurrentStatsByUnit")
	@ResponseBody
	public String getCurrentStatsByUnit(@RequestParam Map<String, String> inputData) {
		System.out.println(inputData);
		return sensorService.getCurrentStatsByUnit(inputData.get("unit")).toString();
		// return json.toString();
	}

	@GetMapping(value = "/getCurrentStatsAllUnit")
	@ResponseBody
	public String getCurrentStatsAllUnit(@RequestParam Map<String, String> inputData) {
		return sensorService.getCurrentStatsAllUnit().toString();
		// return json.toString();
	}

	@GetMapping("/dbstatus")
	@ResponseBody
	public String dbstatus() {
		return sensorService.listAll().size() + "";
	}

	@GetMapping("/getPower")
	@ResponseBody
	public String getPower() {
		return sensorService.getPower() + "";
	}
//
//	// start of code
//	1. get database list
//	2. get to tables with size
//	3. describe table
//	4.

	@GetMapping(value = "/getClusters")
	@ResponseBody
	public String getClusters(@RequestParam Map<String, String> inputData) {

		return new JSONArray(RedshiftExecutor.getClusters()).toString();
	}

	@GetMapping(value = "/getLargeTables")
	@ResponseBody
	public String getLargeTables(@RequestParam Map<String, String> inputData) {
		return RedshiftExecutor.getLargeTabes(inputData.get("cluster") ,inputData.get("limit"),inputData.get("refresh")).toString();
	}

	@GetMapping(value = "/getTableInfo")
	@ResponseBody
	public String getTableInfo(@RequestParam Map<String, String> inputData) {
		return RedshiftExecutor.getTableInfo(inputData.get("schema") ,inputData.get("table"),inputData.get("cluster")).toString();
	}

	@GetMapping(value = "/getDataRetention")
	@ResponseBody
	public String getDataRetention(@RequestParam Map<String, String> inputData) {
		return RedshiftExecutor.getDataRetention(inputData.get("schema") ,inputData.get("table"),inputData.get("cluster"), inputData.get("columnList")).toString();
	}

//
//	@GetMapping(value = "/executeSparkQuery")
//	@ResponseBody
//	public String executeSparkQuery(@RequestParam Map<String, String> inputData) {
//		return QMRReport.execute(inputData.get("query")).toString();
//	}
//	result: {"max(dw_last_updated)":"2021-03-15 00:46:49.292186","min(dw_last_updated)":"2020-01-01 02:10:58.936271"}



}
