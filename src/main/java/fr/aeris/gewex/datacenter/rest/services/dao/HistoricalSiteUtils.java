package fr.aeris.gewex.datacenter.rest.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.aeris.commons.metadata.util.UuidUtils;

public class HistoricalSiteUtils {
	public static final String HISTORICAL_SITE_ROOT_URL = "http://climserv.ipsl.polytechnique.fr/gewexca/DATA/instruments/";
	
	public static List<String> getInstrumentNames() throws Exception {
		List<String> instrumentNames = getFolderNamesFromUrl(HISTORICAL_SITE_ROOT_URL);
		return instrumentNames;
	}
	
	public static List<String> getFolderNamesFromUrl(String url) throws Exception {
		List<String> result = new ArrayList<>();
		Document doc = Jsoup.connect(url).get();
		Elements images = doc.select("img[alt='[DIR]']");
		
		for (Element image : images) {
			String imageIconName = image.attr("src");
			if (imageIconName.endsWith("folder.gif")) {
				Elements links = image.parent().parent().select("a[href]");
				for (Element link : links) {
					result.add(link.attr("href").replace('/', ' ').trim());
				}
			}
		}
		return result;
	}
	
	public static List<Integer> getYearsForInstrument(String name, String rootUrl) throws Exception {
		String url = rootUrl+name+"/years";
		List<String> folders = HistoricalSiteUtils.getFolderNamesFromUrl(url);
		List<Integer> result = new ArrayList<>();
		for (String folder : folders) {
			try {
				Integer aux = new Integer(folder);
			if ((aux>1000) && (aux < 3000)) {
				result.add(new Integer(folder));
			}
			}
			catch (Exception e) {
				
			}
		}
		
		Collections.sort(result);
		
		return result;
		
	}

	public static String getUuidFromInstrumentName(String instrumentName) {
		return UuidUtils.getUuidFromText("GEWEXCA"+instrumentName);
	}

}
