package fr.aeris.gewex.datacenter.rest.services.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.aeris.commons.model.elements.os.OSEntry;
import fr.sedoo.commons.util.ListUtil;

public class CachedDataDaoImpl implements DataDao{

	private DataDao cachedDao;
	
	private LoadingCache<String,List<OSEntry>> cache = CacheBuilder.newBuilder()
		    .maximumSize(10)
		    .expireAfterWrite(6, TimeUnit.HOURS)
		    .build(new CacheLoader<String, List<OSEntry>>() {
		        @Override
		        public List<OSEntry> load(String uuid){
		        	List<OSEntry> aux = cachedDao.getOsEntryListFromUuid(uuid);
		        	if (aux == null) {
		        		return new ArrayList<>(); 
		        	}
		        	else {
		        		return aux;
		        	}
		        }
		    });
	
	public CachedDataDaoImpl(DataDao cachedDao) {
		this.cachedDao = cachedDao;
	}
	
	@Override
	public List<OSEntry> getOsEntryListFromUuid(String uuid) {
		try {
		List<OSEntry> aux = cache.get(uuid);
		if (ListUtil.isEmpty(aux)) {
			cache.invalidate(uuid);
			aux = cache.get(uuid);
		}
		return aux;
		}
		catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@Override
	@PostConstruct
	public void init() throws Exception {
		cachedDao.init();
	}

	@Override
	public void downloadYear(String uuid, String year, File localDirectory) throws Exception {
		cachedDao.downloadYear(uuid, year, localDirectory);
	}

}
