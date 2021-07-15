package com.amazon.converters;

import com.amazon.commands.SensorForm;
import com.amazon.domain.Sensor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 1/10/17.
 */
@Component
public class SensorToSensorForm implements Converter<Sensor, SensorForm> {
	@Override
	public SensorForm convert(Sensor sensor) {
		SensorForm sensorForm = new SensorForm();

		sensorForm.setId(sensor.get_id());
		sensorForm.setEventid(sensor.getEventid());
		sensorForm.setDeviceimei(sensor.getDeviceimei());
		sensorForm.setDevicetimestamp(sensor.getDevicetimestamp());
		sensorForm.setMachinename(sensor.getMachinename());
		sensorForm.setOti(sensor.getOti());
		sensorForm.setWti(sensor.getWti());
		sensorForm.setAti(sensor.getAti());
		sensorForm.setOli(sensor.getOli());
		sensorForm.setOltc_wti(sensor.getOltc_wti());
		sensorForm.setHum(sensor.getHum());
		sensorForm.setOtia(sensor.getOtia());
		sensorForm.setOtit(sensor.getOtit());
		sensorForm.setWtia(sensor.getWtia());
		sensorForm.setWtit(sensor.getWtit());
		sensorForm.setGora(sensor.getGora());
		sensorForm.setGort(sensor.getGort());
		sensorForm.setMoga(sensor.getMoga());
		sensorForm.setSrt(sensor.getSrt());
		sensorForm.setPrvt(sensor.getPrvt());
		sensorForm.setOltcsurge(sensor.getOltcsurge());
		sensorForm.setOltcprv(sensor.getOltcprv());
		sensorForm.setIn1(sensor.getIn1());
		sensorForm.setIn2(sensor.getIn2());
		sensorForm.setFan1(sensor.getFan1());
		sensorForm.setFan2(sensor.getFan2());
		sensorForm.setOut1(sensor.getOut1());
		sensorForm.setOut2(sensor.getOut2());
		sensorForm.setOut3(sensor.getOut3());
		sensorForm.setOut4(sensor.getOut4());

		return sensorForm;
	}
}
