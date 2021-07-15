package com.amazon.converters;

import com.amazon.commands.SensorForm;
import com.amazon.domain.Sensor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by jt on 1/10/17.
 */
@Component
public class SensorFormToSensor implements Converter<SensorForm, Sensor> {

	@Override
	public Sensor convert(SensorForm sensorForm) {
		Sensor sensor = new Sensor();
		if (sensorForm.getId() != null && !StringUtils.isEmpty(sensorForm.getId())) {
			sensor.set_id(sensorForm.getId());
		}
		sensor.setEventid(sensorForm.getEventid());
		sensor.setDeviceimei(sensorForm.getDeviceimei());
		sensor.setDevicetimestamp(sensorForm.getDevicetimestamp());
		sensor.setMachinename(sensorForm.getMachinename());
		sensor.setOti(sensorForm.getOti());
		sensor.setWti(sensorForm.getWti());
		sensor.setAti(sensorForm.getAti());
		sensor.setOli(sensorForm.getOli());
		sensor.setOltc_wti(sensorForm.getOltc_wti());
		sensor.setHum(sensorForm.getHum());
		sensor.setOtia(sensorForm.getOtia());
		sensor.setOtit(sensorForm.getOtit());
		sensor.setWtia(sensorForm.getWtia());
		sensor.setWtit(sensorForm.getWtit());
		sensor.setGora(sensorForm.getGora());
		sensor.setGort(sensorForm.getGort());
		sensor.setMoga(sensorForm.getMoga());
		sensor.setSrt(sensorForm.getSrt());
		sensor.setPrvt(sensorForm.getPrvt());
		sensor.setOltcsurge(sensorForm.getOltcsurge());
		sensor.setOltcprv(sensorForm.getOltcprv());
		sensor.setIn1(sensorForm.getIn1());
		sensor.setIn2(sensorForm.getIn2());
		sensor.setFan1(sensorForm.getFan1());
		sensor.setFan2(sensorForm.getFan2());
		sensor.setOut1(sensorForm.getOut1());
		sensor.setOut2(sensorForm.getOut2());
		sensor.setOut3(sensorForm.getOut3());
		sensor.setOut4(sensorForm.getOut4());

		return sensor;
	}
}
