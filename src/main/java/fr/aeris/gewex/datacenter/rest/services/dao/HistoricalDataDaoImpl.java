package fr.aeris.gewex.datacenter.rest.services.dao;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import fr.aeris.commons.metadata.domain.temporal.DateUtil;
import fr.aeris.commons.model.elements.os.OSEntry;

public class HistoricalDataDaoImpl implements DataDao{

	Map<String, List<OSEntry>> osEntriesMap = new HashMap<>();
	private List<String> instrumentNames;

	@Override
	public void init() throws Exception {
		instrumentNames = HistoricalSiteUtils.getInstrumentNames();
		for (String instrumentName : instrumentNames) {
			String uuid = HistoricalSiteUtils.getUuidFromInstrumentName(instrumentName);
			List<Integer> years = HistoricalSiteUtils.getYearsForInstrument(instrumentName, HistoricalSiteUtils.HISTORICAL_SITE_ROOT_URL);
			List<OSEntry> aux = new ArrayList<>();
			for (Integer year : years) {
				aux.add(getOSEntryFromYearFolder(instrumentName, year));
			}
			osEntriesMap.put(uuid, aux);
		}
	}
	
	private String getInstrumentNameFromUuid(String uuid) throws Exception {
		for (String instrumentName : instrumentNames) {
			String aux = HistoricalSiteUtils.getUuidFromInstrumentName(instrumentName);
			if (aux.compareToIgnoreCase(uuid)==0) {
				return instrumentName;
			}
		}
		
		throw new Exception ("No instrument for uuid "+uuid);
	}

	private OSEntry getOSEntryFromYearFolder(String instrumentName, Integer year) throws Exception {
		OSEntry result = new OSEntry();
		result.setDate(DateUtil.calendarFromYMD(year, 1, 1).getTime());
		result.setFileNumber(0);
		result.setTotalSize(0l);
		String rootUrl = HistoricalSiteUtils.HISTORICAL_SITE_ROOT_URL+instrumentName+"/years/"+year;
		Document doc = Jsoup.connect(rootUrl).get();
		Elements liens = doc.select("a[href$='nc.gz']");
		for (Element lien : liens) {
			Elements trs = lien.parent().parent().select("tr");
			for (Element tr : trs) {
				Node node = tr.childNodes().get(tr.childNodeSize()-1);
				if (node instanceof Element) {
					Element aux = (Element) node;
					result.setFileNumber(result.getFileNumber()+1);
					String value = aux.text().toLowerCase();
					if (value.endsWith("m")) {
						value = value.replace('m', ' ').trim();
						Long size = new Float(1024*1024*new Float(value)).longValue();
						result.setTotalSize(result.getTotalSize()+size);
					}
					else if (value.endsWith("k")) {
						value = value.replace('k', ' ').trim();
						Long size = new Float(1024*new Float(value)).longValue();
						result.setTotalSize(result.getTotalSize()+size);
					}
					else {
						result.setTotalSize(result.getTotalSize()+new Integer(value));
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<OSEntry> getOsEntryListFromUuid(String uuid) {
		List<OSEntry> list = osEntriesMap.get(uuid);
		if (list == null) {
			return new ArrayList<>();
		}
		else {
			return list;
		}
	}

	@Override
	public void downloadYear(String uuid, String year, File localDirectory) throws Exception {
		String instrumentName = getInstrumentNameFromUuid(uuid);
		String rootUrl = HistoricalSiteUtils.HISTORICAL_SITE_ROOT_URL+instrumentName+"/years/"+year;
		Document doc = Jsoup.connect(rootUrl).get();
		Elements liens = doc.select("a[href$='nc.gz']");
		File yearFolder = new File(localDirectory, year);
		yearFolder.mkdirs();
		for (Element lien : liens) {
			String fileName = lien.attr("href");
			File localFile = new File (yearFolder, fileName);
			String fileUrl = rootUrl+"/"+fileName;
			FileUtils.copyURLToFile(new URL(fileUrl), localFile);
		}
	}


}
