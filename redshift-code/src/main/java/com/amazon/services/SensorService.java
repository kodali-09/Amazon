package com.amazon.services;

import com.amazon.commands.SensorForm;
import com.amazon.domain.Sensor;

import java.util.List;

/**
 * Created by jt on 1/10/17.
 */
public interface SensorService {

	List<Sensor> listAll();

	List<String> listUnits();

	Sensor getById(Long id);

	Sensor saveOrUpdate(Sensor product);

	void delete(Long id);

	Sensor saveOrUpdateSensorForm(SensorForm productForm);

	List<Sensor> getDataByUnit(String unit);

	List<String> getStatsByUnit(String unit);

	List<List<String>> getRodentStatsByUnit(String unit);

	List<String> getCurrentStatsByUnit(String unit);

	List<List<String>> getCurrentStatsAllUnit();

	int getPower();

}
