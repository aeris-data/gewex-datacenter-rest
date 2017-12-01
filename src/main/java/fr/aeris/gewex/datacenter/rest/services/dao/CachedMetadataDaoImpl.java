package fr.aeris.gewex.datacenter.rest.services.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.sedoo.commons.util.ListUtil;

public class CachedMetadataDaoImpl implements MetadataDao{

	private static final String YEARS = "years";
	private static final String INSTRUMENTS = "instruments";

	private MetadataDao cachedDao;
	
	private LoadingCache<String,List<String>> cache = CacheBuilder.newBuilder()
		    .maximumSize(10)
		    .expireAfterWrite(6, TimeUnit.HOURS)
		    .build(new CacheLoader<String, List<String>>() {
		        @Override
		        public List<String> load(String key){
		        	if (key == INSTRUMENTS) {
		        		return cachedDao.getIntruments();
		        	}
		        	else if (key == YEARS) {
		        		return cachedDao.getYears();
		        	}
		        	else return new ArrayList<String>();
		        }
		    });
	
	

	public CachedMetadataDaoImpl(MetadataDao cachedDao) {
		this.cachedDao = cachedDao;
	}
	
	@Override
	public List<String> getYears() {
		try {
		List<String> years = cache.get(YEARS);
		if (ListUtil.isEmpty(years)) {
			cache.invalidate(YEARS);
			years = cache.get(YEARS);
		}
		return years;
		}
		catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@Override
	public List<String> getIntruments() {
		try {
			List<String> instruments = cache.get(INSTRUMENTS);
			if (ListUtil.isEmpty(instruments)) {
				cache.invalidate(INSTRUMENTS);
				instruments = cache.get(INSTRUMENTS);
			}
			return instruments;
			}
			catch (Exception e) {
				return new ArrayList<>();
			}
	}

}
