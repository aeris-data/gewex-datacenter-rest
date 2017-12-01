package fr.aeris.gewex.datacenter.rest.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import fr.aeris.commons.metadata.domain.Metadata;
import fr.aeris.commons.metadata.domain.identification.data.dataset.AbstractDataMetadata;
import fr.aeris.commons.metadata.domain.identification.data.dataset.CollectionMetadata;
import fr.aeris.commons.metadata.domain.identification.data.dataset.Instrument;
import fr.aeris.commons.metadata.domain.identification.data.dataset.InstrumentList;
import fr.aeris.commons.metadata.domain.identification.data.dataset.Plateform;
import fr.aeris.commons.metadata.domain.identification.data.dataset.PlateformList;
import fr.aeris.commons.metadata.util.json.JsonUtils;
import fr.aeris.gewex.datacenter.rest.services.config.GewexDatacenterConfig;
import fr.aeris.gewex.maintenance.SslUtils;
import fr.sedoo.commons.util.ListUtil;

public class MetadataDaoImpl implements MetadataDao{

	JsonUtils jsonUtils = new JsonUtils("fr.aeris");

	@Autowired
	private
	GewexDatacenterConfig config;


	private List<Metadata> getGewexMetadata() throws Exception {
		ArrayList<Metadata> result = new ArrayList<>();

		String url = getConfig().getMetadataService()+"request?program=GEWEX";

		Client clientForUrl = SslUtils.getClientForUrl(url);

		Response response = clientForUrl.target(url).request().post(Entity.entity("{}", MediaType.APPLICATION_JSON));
		if (response.getStatus() == HttpStatus.SC_OK) {
			List<String> uuids = new ArrayList<>();
			String json  = response.readEntity(String.class);
			JSONParser parser = new JSONParser();
			JSONArray summaries = null;
			try {
				summaries = (JSONArray) parser.parse(json);
			} catch (ParseException e) {
				System.out.println("Erreur parsing");
			}
			{
				for (Object summary : summaries) {
					uuids.add((String) ((JSONObject) summary).get("id"));
				}
			}

			for (String uuid : uuids) {

				String idRequestUrl = getConfig().getMetadataService()+"id/"+uuid;

				clientForUrl = SslUtils.getClientForUrl(idRequestUrl);

				response = clientForUrl.target(idRequestUrl).request().get();
				if (response.getStatus() == HttpStatus.SC_OK) {
					json  = response.readEntity(String.class);
					Metadata aux = jsonUtils.parse(json);
					result.add(aux);
				}
			}

		}

		return result;
	}

	@Override
	public List<String> getYears() {






		List<String> years = new ArrayList<>();

		years.add("1000");
		years.add("2001");
		years.add("2002");
		years.add("2003");
		return years;
	}

	@Override
	public List<String> getIntruments() {
		
		List<String> result = new ArrayList<>();
		try {
			List<Metadata> gewexMetadata = getGewexMetadata();
			for (Metadata metadata : gewexMetadata) {
				if (metadata instanceof CollectionMetadata) {
					CollectionMetadata aux = (CollectionMetadata) metadata;
					PlateformList plateforms = aux.getPlateforms();
					if (ListUtil.isNotEmpty(plateforms)) {
						Plateform plateform = plateforms.get(ListUtil.FIRST_INDEX);
						InstrumentList instruments = plateform.getInstruments();
						if (ListUtil.isNotEmpty(instruments)) {
							Instrument instrument = instruments.get(ListUtil.FIRST_INDEX);
							result.add(instrument.getType());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(result);
		return result;
	}

	public GewexDatacenterConfig getConfig() {
		return config;
	}

	public void setConfig(GewexDatacenterConfig config) {
		this.config = config;
	}

}
