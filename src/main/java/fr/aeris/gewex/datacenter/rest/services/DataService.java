package fr.aeris.gewex.datacenter.rest.services;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.aeris.catalog.shared.domain.ShoppingCartConstants;
import fr.aeris.commons.model.elements.os.OSEntry;
import fr.aeris.commons.model.elements.os.OSResponse;
import fr.aeris.commons.util.JsonUtil;
import fr.aeris.gewex.datacenter.rest.services.config.GewexDatacenterConfig;
import fr.aeris.gewex.datacenter.rest.services.dao.DataDao;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;



@Component
@Path("/data")
@Api(value = "/data")
public class DataService implements  ShoppingCartConstants {

	private static Logger log = LoggerFactory.getLogger(DataService.class);

	private static HashMap<String, String> paths = new HashMap<>();


	@Autowired
	GewexDatacenterConfig gewexConfig;
	
	@Autowired
	DataDao dataDao;

	@GET
	@Path("/isalive")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Tests the availability of the whole service")
	public Response isAlive() {
		String answer = "Yes";
		return Response.status(Response.Status.OK).entity(answer).build();
	}

	@GET
	@ApiOperation(value = "Return the download command line")
	@Path("/toscript") 
	@Produces(MediaType.TEXT_PLAIN )
	public Response toScript(@ApiParam(name = "collectionId", value = "id of the collection to be scripted", required = true) @QueryParam(COLLECTION_ID_PARAM_NAME) String collectionId, @QueryParam(FILTER_PARAM_NAME) String filter,  @Context UriInfo info) {
		log.info("GEWEX cart content: collection " + collectionId+" - filter "+filter);
		String baseUrl = info.getBaseUri().toString();
		String encodedFilter = "";
		try {
			encodedFilter = URLEncoder.encode(filter, "UTF-8");
		}
		catch (Exception e) {
		}
		String command = "curl -X GET " + baseUrl +  "data/download?collectionId=" + collectionId + "&filter="+encodedFilter+" --output dataset" + collectionId + ".zip";
		return Response.status(Response.Status.OK).entity(command).build();

	}

	@HEAD
	@Path("/download")
	public Response downloadHead(@QueryParam("collectionId") String uuid) {
		Response.ResponseBuilder builder = Response.ok();
		ContentDisposition contentDisposition = ContentDisposition.type("attachment")
				.fileName(getZipFileNameFromUuid(uuid))
				.build();
		builder.header("Content-Disposition", contentDisposition);
		return builder.build();
	}

	@GET
	@Path("/download") 
	@Produces("application/zip")
	public Response download(@QueryParam("collectionId") String uuid, @QueryParam("filter") String filter) {
		return null;
	}	

	private String getZipFileNameFromUuid(String uuid) {
		return "dataset-"+uuid+".zip";
	}

	@GET
	@Path("/request")
	@Produces(MediaType.APPLICATION_JSON)
	public Response request(@QueryParam("collection") String uuid) {
		OSResponse response = new OSResponse();
		List<OSEntry> entries = new ArrayList<>();
		response.setEntries(entries);
		List<OSEntry> aux = dataDao.getOsEntryListFromUuid(uuid);
		if (aux != null) {
			entries.addAll(aux);
		}
		
		try {
			return Response.status(200).entity(JsonUtil.toJson(response)).build();
		} catch (JsonProcessingException e) {
			return Response.status(500).entity("").build();
		}
	}
	

}
