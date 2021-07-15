package com.amazon.domain;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.expression.ParseException;

@Entity
public class Sensor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long _id;

	public Long get_id() {
		return _id;
	}

	public void set_id(Long _id) {
		this._id = _id;
	}

	public String getEventid() {
		return eventid;
	}

	public void setEventid(String eventid) {
		this.eventid = eventid;
	}

	public String getDeviceimei() {
		return deviceimei;
	}

	public void setDeviceimei(String deviceimei) {
		this.deviceimei = deviceimei;
	}

	public Timestamp getDevicetimestamp() {
		return devicetimestamp;
	}

	public void setDevicetimestamp(Timestamp devicetimestamp) {
		this.devicetimestamp = devicetimestamp;
	}

	public void setDevicetimestamp(String devicetimestamp) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		java.util.Date parsedDate;
		try {
			parsedDate = dateFormat.parse(devicetimestamp);

			Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
			this.devicetimestamp = timestamp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getMachinename() {
		return machinename;
	}

	public void setMachinename(String machinename) {
		this.machinename = machinename;
	}

	public String getOti() {
		return oti;
	}

	public void setOti(String oti) {
		this.oti = oti;
	}

	public String getWti() {
		return wti;
	}

	public void setWti(String wti) {
		this.wti = wti;
	}

	public String getAti() {
		return ati;
	}

	public void setAti(String ati) {
		this.ati = ati;
	}

	public String getOli() {
		return oli;
	}

	public void setOli(String oli) {
		this.oli = oli;
	}

	public String getOltc_wti() {
		return oltc_wti;
	}

	public void setOltc_wti(String oltc_wti) {
		this.oltc_wti = oltc_wti;
	}

	public Integer getHum() {
		return hum;
	}

	public void setHum(Integer hum) {
		this.hum = hum;
	}

	public Integer getOtia() {
		return otia;
	}

	public void setOtia(Integer otia) {
		this.otia = otia;
	}

	public Integer getOtit() {
		return otit;
	}

	public void setOtit(Integer otit) {
		this.otit = otit;
	}

	public Integer getWtia() {
		return wtia;
	}

	public void setWtia(Integer wtia) {
		this.wtia = wtia;
	}

	public Integer getWtit() {
		return wtit;
	}

	public void setWtit(Integer wtit) {
		this.wtit = wtit;
	}

	public Integer getGora() {
		return gora;
	}

	public void setGora(Integer gora) {
		this.gora = gora;
	}

	public Integer getGort() {
		return gort;
	}

	public void setGort(Integer gort) {
		this.gort = gort;
	}

	public Integer getMoga() {
		return moga;
	}

	public void setMoga(Integer moga) {
		this.moga = moga;
	}

	public Integer getSrt() {
		return srt;
	}

	public void setSrt(Integer srt) {
		this.srt = srt;
	}

	public Integer getPrvt() {
		return prvt;
	}

	public void setPrvt(Integer prvt) {
		this.prvt = prvt;
	}

	public Integer getOltcsurge() {
		return oltcsurge;
	}

	public void setOltcsurge(Integer oltcsurge) {
		this.oltcsurge = oltcsurge;
	}

	public Integer getOltcprv() {
		return oltcprv;
	}

	public void setOltcprv(Integer oltcprv) {
		this.oltcprv = oltcprv;
	}

	public Integer getIn1() {
		return in1;
	}

	public void setIn1(Integer in1) {
		this.in1 = in1;
	}

	public Integer getIn2() {
		return in2;
	}

	public void setIn2(Integer in2) {
		this.in2 = in2;
	}

	public Integer getFan1() {
		return fan1;
	}

	public void setFan1(Integer fan1) {
		this.fan1 = fan1;
	}

	public Integer getFan2() {
		return fan2;
	}

	public void setFan2(Integer fan2) {
		this.fan2 = fan2;
	}

	public Integer getOut1() {
		return out1;
	}

	public void setOut1(Integer out1) {
		this.out1 = out1;
	}

	public Integer getOut2() {
		return out2;
	}

	public void setOut2(Integer out2) {
		this.out2 = out2;
	}

	public Integer getOut3() {
		return out3;
	}

	public void setOut3(Integer out3) {
		this.out3 = out3;
	}

	public Integer getOut4() {
		return out4;
	}

	public void setOut4(Integer out4) {
		this.out4 = out4;
	}

	private String eventid;
	private String deviceimei;
	private Timestamp devicetimestamp;
	private String machinename;
	private String oti;
	private String wti;
	private String ati;
	private String oli;
	private String oltc_wti;
	private Integer hum;
	private Integer otia;
	private Integer otit;
	private Integer wtia;
	private Integer wtit;
	private Integer gora;
	private Integer gort;
	private Integer moga;
	private Integer srt;
	private Integer prvt;
	private Integer oltcsurge;
	private Integer oltcprv;
	private Integer in1;
	private Integer in2;
	private Integer fan1;
	private Integer fan2;
	private Integer out1;
	private Integer out2;
	private Integer out3;
	private Integer out4;

	@Override
	public String toString() {
		return "Sensor [_id=" + _id + ", eventid=" + eventid + ", deviceimei=" + deviceimei + ", devicetimestamp="
				+ devicetimestamp + ", machinename=" + machinename + ", oti=" + oti + ", wti=" + wti + ", ati=" + ati
				+ ", oli=" + oli + ", oltc_wti=" + oltc_wti + ", hum=" + hum + ", otia=" + otia + ", otit=" + otit
				+ ", wtia=" + wtia + ", wtit=" + wtit + ", gora=" + gora + ", gort=" + gort + ", moga=" + moga
				+ ", srt=" + srt + ", prvt=" + prvt + ", oltcsurge=" + oltcsurge + ", oltcprv=" + oltcprv + ", in1="
				+ in1 + ", in2=" + in2 + ", fan1=" + fan1 + ", fan2=" + fan2 + ", out1=" + out1 + ", out2=" + out2
				+ ", out3=" + out3 + ", out4=" + out4 + "]";
	}

}
