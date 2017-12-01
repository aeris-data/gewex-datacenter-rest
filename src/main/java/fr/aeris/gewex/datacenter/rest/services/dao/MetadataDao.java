package fr.aeris.gewex.datacenter.rest.services.dao;

import java.util.List;

public interface MetadataDao {

	List<String> getYears();
	List<String> getIntruments();
}
