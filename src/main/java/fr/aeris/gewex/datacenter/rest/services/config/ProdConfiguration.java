package fr.aeris.gewex.datacenter.rest.services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.aeris.gewex.datacenter.rest.services.dao.CachedDataDaoImpl;
import fr.aeris.gewex.datacenter.rest.services.dao.CachedMetadataDaoImpl;
import fr.aeris.gewex.datacenter.rest.services.dao.DataDao;
import fr.aeris.gewex.datacenter.rest.services.dao.HistoricalDataDaoImpl;
import fr.aeris.gewex.datacenter.rest.services.dao.MetadataDao;
import fr.aeris.gewex.datacenter.rest.services.dao.MetadataDaoImpl;

@Configuration
public class ProdConfiguration {
	
	@Bean
	public GewexDatacenterConfig getGewexDatacenterConfig() {
		
		GewexDatacenterConfig result = new GewexDatacenterConfig();
		result.setWorkingDirectory("/tmp/actris");
		result.setMetadataService("https://sedoo.aeris-data.fr/catalogue/rest/metadatarecette/");
		return result;
	}
	
	@Bean
	public MetadataDao getMetadataDao() {
		MetadataDaoImpl aux = new MetadataDaoImpl();
		aux.setConfig(getGewexDatacenterConfig());
		return new CachedMetadataDaoImpl(aux);
	}
	
	@Bean
	public DataDao getDataDao() {
		DataDao aux = new HistoricalDataDaoImpl();
		return new CachedDataDaoImpl(aux);
	}

}
