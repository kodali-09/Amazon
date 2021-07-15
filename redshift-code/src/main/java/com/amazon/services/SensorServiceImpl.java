package com.amazon.services;

import com.amazon.converters.SensorFormToSensor;
import com.amazon.repositories.SensorRepository;
import com.amazon.commands.SensorForm;
import com.amazon.domain.Sensor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jt on 1/10/17.
 */
@Service
public class SensorServiceImpl implements SensorService {

	private SensorRepository sensorRepository;
	private SensorFormToSensor productFormToProduct;

	@Autowired
	public SensorServiceImpl(SensorRepository productRepository, SensorFormToSensor productFormToProduct) {
		this.sensorRepository = productRepository;
		this.productFormToProduct = productFormToProduct;
	}

	@Override
	public List<Sensor> listAll() {
		List<Sensor> products = new ArrayList<>();
		sensorRepository.findAll().forEach(products::add); // fun with Java 8
		return products;
	}

	@Override
	public Sensor getById(Long id) {
		return sensorRepository.findById(id).orElse(null);
	}

	@Override
	public Sensor saveOrUpdate(Sensor product) {
		sensorRepository.save(product);
		return product;
	}

	@Override
	public void delete(Long id) {
		sensorRepository.deleteById(id);

	}

	@Override
	public Sensor saveOrUpdateSensorForm(SensorForm productForm) {
		Sensor savedProduct = saveOrUpdate(productFormToProduct.convert(productForm));

		System.out.println("Saved Product Id: " + savedProduct.get_id());
		return savedProduct;
	}

	@Override
	public List<String> listUnits() {
		return sensorRepository.listUnits();
	}

	@Override
	public List<Sensor> getDataByUnit(String unit) {
		return sensorRepository.getDataByUnit(unit);
	}

	@Override
	public List<String> getStatsByUnit(String unit) {
		return sensorRepository.getStatsByUnit(unit);
	}

	@Override
	public List<List<String>> getRodentStatsByUnit(String unit) {
		return sensorRepository.getRodentStatsByUnit(unit);
	}

	@Override
	public List<String> getCurrentStatsByUnit(String unit) {
		return sensorRepository.getCurrentStatsByUnit(unit);
	}

	@Override
	public List<List<String>> getCurrentStatsAllUnit() {
		return sensorRepository.getCurrentStatsAllUnit();

	}

	@Override
	public int getPower() {
		return sensorRepository.getPower();
	}
}
