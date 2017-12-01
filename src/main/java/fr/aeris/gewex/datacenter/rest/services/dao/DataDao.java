package fr.aeris.gewex.datacenter.rest.services.dao;

import java.util.List;

import fr.aeris.commons.model.elements.os.OSEntry;

public interface DataDao {

	List<OSEntry> getOsEntryListFromUuid(String uuid);

	void init() throws Exception;

}
