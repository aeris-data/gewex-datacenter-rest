package fr.aeris.gewex.maintenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.aeris.catalog.shared.domain.parameter.Parameter;
import fr.aeris.commons.metadata.domain.Metadata;
import fr.aeris.commons.metadata.domain.distribution.DistributionInformation;
import fr.aeris.commons.metadata.domain.identification.contact.Contact;
import fr.aeris.commons.metadata.domain.identification.contact.ContactList;
import fr.aeris.commons.metadata.domain.identification.contact.RoleConstants;
import fr.aeris.commons.metadata.domain.identification.contact.Roles;
import fr.aeris.commons.metadata.domain.identification.data.AerisDataCenter;
import fr.aeris.commons.metadata.domain.identification.data.dataset.CollectionMetadata;
import fr.aeris.commons.metadata.domain.identification.data.dataset.DataProcessingLevel;
import fr.aeris.commons.metadata.domain.identification.data.dataset.DefaultParameter;
import fr.aeris.commons.metadata.domain.identification.data.dataset.Format;
import fr.aeris.commons.metadata.domain.identification.data.dataset.Formats;
import fr.aeris.commons.metadata.domain.identification.data.dataset.Instrument;
import fr.aeris.commons.metadata.domain.identification.data.dataset.InstrumentList;
import fr.aeris.commons.metadata.domain.identification.data.dataset.MetadataStatus;
import fr.aeris.commons.metadata.domain.identification.data.dataset.ParameterList;
import fr.aeris.commons.metadata.domain.identification.data.dataset.ParameterType;
import fr.aeris.commons.metadata.domain.identification.data.dataset.Plateform;
import fr.aeris.commons.metadata.domain.identification.data.dataset.PlateformList;
import fr.aeris.commons.metadata.domain.identification.data.dataset.PlatformType;
import fr.aeris.commons.metadata.domain.identification.lang.InternationalString;
import fr.aeris.commons.metadata.domain.identification.lang.Language;
import fr.aeris.commons.metadata.domain.identification.link.Links;
import fr.aeris.commons.metadata.domain.identification.link.impl.InformationLink;
import fr.aeris.commons.metadata.domain.identification.link.impl.OpenSearchLink;
import fr.aeris.commons.metadata.domain.temporal.Instant;
import fr.aeris.commons.metadata.domain.temporal.TemporalExtents;
import fr.aeris.commons.metadata.domain.temporal.TemporalInterval;
import fr.aeris.commons.metadata.util.UuidUtils;
import fr.aeris.commons.metadata.util.json.JsonUtils;
import fr.aeris.gewex.datacenter.rest.services.dao.HistoricalSiteUtils;

public class MetadataCreation {

	static JsonUtils jsonUtils = new JsonUtils("fr.aeris");
	
	public static void main(String[] args) throws Exception {
		MetadataCreation aux = new MetadataCreation();
		aux.run();
	}

	private void run() throws Exception {
		
		List<String> instrumentNames = HistoricalSiteUtils.getInstrumentNames();
		for (String instrumentName : instrumentNames) {
			Metadata aux = createMetadaForInstrument(instrumentName, HistoricalSiteUtils.HISTORICAL_SITE_ROOT_URL);
			save(aux);
		}
	}
	
	private void save(Metadata metadata) throws Exception {
		String json = jsonUtils.toJson(metadata);
		
		String uuid = metadata.getIdentifier();
		String metadataService = "https://sedoo.aeris-data.fr/catalogue/rest/metadatarecette/";
		String idRequestUrl = metadataService+"id/"+uuid;
		
		Client clientForUrl = SslUtils.getClientForUrl(metadataService);
		Response response = clientForUrl.target(idRequestUrl).request().post(Entity.entity(json, MediaType.APPLICATION_JSON));
		if (response.getStatus() != HttpStatus.SC_OK) {
			System.out.println("Impossible de sauver "+uuid);
			throw new Exception("Impossible de sauver "+uuid);
		}
		else {
			System.out.println("Sauvegarde "+uuid+" OK");
		}
		
	}
	
	
	private List<String> getParametersForInstrument(String name, String rootUrl) throws Exception {
		String url = rootUrl+name+"/variables";
		List<String> result = HistoricalSiteUtils.getFolderNamesFromUrl(url);
		Collections.sort(result);
		return result;
		
	}

	private Metadata createMetadaForInstrument(String instrumentName, String rootUrl) throws Exception {
		
		CollectionMetadata result = new CollectionMetadata();
		
		
		result.setProgramName("GEWEX");
		result.setCollectionName("Cloud Assessment");
		result.setAerisDataCenter(AerisDataCenter.ESPRI);
		result.setStatus(MetadataStatus.Public);
		
		String uuid = HistoricalSiteUtils.getUuidFromInstrumentName(instrumentName);
		
		result.setId(uuid);
		result.setIdentifier(uuid);
		
		InternationalString ressourceTitle = new InternationalString();
		
		String englishTitle ="GEWEX Cloud Assessment for instrument "+instrumentName;
		String frenchTitle ="Produit nuageux Gewex pour l'instrument "+instrumentName;
		
		ressourceTitle.addValue(Language.ENGLISH, englishTitle);
		ressourceTitle.addValue(Language.FRENCH, frenchTitle);
		
		result.setResourceTitle(ressourceTitle);
		
		ContactList contactList = new ContactList();
		
		contactList.add(getClaudia());
		contactList.add(getCathy());
		
		InternationalString distributionDescription	= new InternationalString();
		String englishDistributionDescription ="If the data from the GEWEX Cloud Assessment are used in any publication, we request referencing the article of Stubenrauch et al. 2012. In addition, each dataset used should be specifically referenced (see Table 1 of the article or dataset descriptions for details). We also request to include the following acknowledgment:\n\n> *The GEWEX Cloud Assessment data were obtained from the ClimServ Data Center of IPSL/CNRS*";
		String frenchDistributionDescription =englishDistributionDescription;
		
		distributionDescription.addValue(Language.ENGLISH, englishDistributionDescription);
		distributionDescription.addValue(Language.FRENCH, frenchDistributionDescription);
		
		
		DistributionInformation distributionInformation = new DistributionInformation();
		distributionInformation.setDescription(distributionDescription);
		result.setDistributionInformation(distributionInformation);
		
		result.setContacts(contactList);
		
		List<Integer> years = HistoricalSiteUtils.getYearsForInstrument(instrumentName, rootUrl);
		
		TemporalExtents extents = new TemporalExtents();
		TemporalInterval temporalInterval = new TemporalInterval(new Instant(years.get(0), 1, 1), new Instant(years.get(years.size()-1), 12, 31));
		extents.add(temporalInterval);
		result.setTemporalExtents(extents);

		result.setDataLevel(DataProcessingLevel.L3);
		
		Format netCDF = new Format("NetCDF");
		Formats formats = new Formats();
		formats.add(netCDF);
		result.setFormats(formats);
		
		String wordpressSite = "https://www7.obs-mip.fr/gewexca/";
		
		Links links = new Links();
		InformationLink link = new InformationLink();
		link.setUrl(wordpressSite+instrumentName+"-dataset");
		
		InternationalString informationLinkDescription = new InternationalString();
		informationLinkDescription.addValue(Language.ENGLISH, "Complementary information");
		informationLinkDescription.addValue(Language.FRENCH, "Informations complémentaires");
		
		link.setDescription(informationLinkDescription);
		
		links.add(link);
		
		OpenSearchLink osLink = new OpenSearchLink();
		osLink.setUrl("https://sedoo.aeris-data.fr/gewex-datacenter-rest/rest/data");
		
		links.add(osLink);
		
		result.setLinks(links);
		
		
		ParameterList parameters = new ParameterList();
		List<String> aux = getParametersForInstrument(instrumentName, rootUrl);
		Collections.sort(aux);
		for (String current : aux) {
			DefaultParameter parameter = new DefaultParameter();
			parameter.setShortName(current);
			parameter.setType(ParameterType.CLOUD_PROPERTIES);
			parameters.add(parameter);	
		}
		
		result.setParameters(parameters);
		
		PlateformList plateformList = new PlateformList();
		Plateform plateform = new Plateform();
		plateform.setType(PlatformType.Eos);
		plateform.setName("OTHER_EOS");
		plateformList.add(plateform);
		result.setPlateforms(plateformList);
	
		InstrumentList instruments = new InstrumentList();
		Instrument instrument = new Instrument(instrumentName);
		
		instruments.add(instrument);
		
		plateform.setInstruments(instruments);

		result.setClientTemplateName("md-template-gewexca");
		
		return result;
	}

	private Contact getClaudia() {
		Contact result = new Contact();
		result.setName("Claudia Stubenrauch");
		result.setOrganisation("LMD/CNRS/UPMC");
		result.setEmail("stubenrauch@lmd.polytechnique.fr");
		Roles roles = new Roles();
		roles.add(RoleConstants.POINT_OF_CONTACT);
		result.setRoles(roles);
		return result;
	}
	
	private Contact getCathy() {
		Contact result = new Contact();
		result.setName("Cathy Boonne");
		result.setOrganisation("AERIS");
		result.setEmail("Cathy.Boonne@ipsl.fr");
		Roles roles = new Roles();
		roles.add(RoleConstants.METADATA_POINT_OF_CONTACT);
		result.setRoles(roles);
		return result;
	}

	

}
