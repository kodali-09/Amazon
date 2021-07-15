package com.amazon.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.amazon.domain.Sensor;

/**
 * Created by jt on 1/10/17.
 */
public interface SensorRepository extends CrudRepository<Sensor, Long> {

	@Query(value = "select distinct EventId from sensor order by EventId", nativeQuery = true)
	List<String> listUnits();

	// @Query(value = "select * from sensor where DATE_FORMAT(devicetimestamp,
	// '%Y-%m-%e') = 2014-01-02 and eventId = ?", nativeQuery = true)
	@Query(value = "select *, DATE_FORMAT(devicetimestamp, '%Y-%m-%d %H:%i:%s') as Date from sensor where  DATE_FORMAT(devicetimestamp, '%Y-%m-%d') = current_date() and eventId = ? order by devicetimestamp limit 25", nativeQuery = true)
	List<Sensor> getDataByUnit(String unit);

	@Query(value = "select avg(oti) as temperature, avg(wti) as humidity, avg(oli)  from (select *  from sensor where  DATE_FORMAT(devicetimestamp, '%Y-%m-%d') = current_date() and eventId = ?) as t1 ", nativeQuery = true)
	List<String> getStatsByUnit(String unit);

	@Query(value = "select DATE_FORMAT(devicetimestamp, '%H:%i:%s'), CASE WHEN ati = 'Rodent' THEN 1  ELSE 0 END as Rodent from sensor  where  DATE_FORMAT(devicetimestamp, '%Y-%m-%d') = current_date() and eventId = ? order by devicetimestamp desc limit 10", nativeQuery = true)
	List<List<String>> getRodentStatsByUnit(String unit);

	@Query(value = " select oti as temperature, wti as humidity, oli as smoke from sensor  where  DATE_FORMAT(devicetimestamp, '%Y-%m-%d') = current_date() and eventId = ? order by devicetimestamp desc limit 1", nativeQuery = true)
	List<String> getCurrentStatsByUnit(String unit);

	@Query(value = "select eventid,oti, wti, oli, CASE WHEN ati = 'Rodent' THEN 1  ELSE 0 END as Rodent from sensor where _id in (select max(_id) from sensor group by eventid) order by eventid", nativeQuery = true)
	List<List<String>> getCurrentStatsAllUnit();

	@Query(value = "delete from sensor where DATE_FORMAT(devicetimestamp, '%Y%m%d') < current_date() -3", nativeQuery = true)
	int deleteOldRecords();

	@Query(value = "select fan1 from sensor  where  DATE_FORMAT(devicetimestamp, '%Y-%m-%d') = current_date() order by devicetimestamp desc limit 1", nativeQuery = true)
	int getPower();

}
// DATE_FORMAT(devicetimestamp, '%Y-%m-%d %H:%i:%s')