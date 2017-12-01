package fr.aeris.gewex.datacenter.rest.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.aeris.gewex.datacenter.rest.services.dao.MetadataDao;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Component
@Path("/metadata")
@Api(value = "/metadata")
public class MetadataService  {

	public MetadataService() {
	}
	
	@Autowired
	MetadataDao metadataDao;

	@GET
	@Path("/isalive")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Tests the availability of the whole service")
	public Response isAlive() {
		String answer = "Yes";
		return Response.status(Response.Status.OK).entity(answer).build();
	}

	@GET
	@Path("/years")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getYears() {
		return Response.ok(metadataDao.getYears()).type(MediaType.APPLICATION_JSON).build();
	}

	
	@GET
	@Path("/instruments")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInstruments() {
		return Response.ok(metadataDao.getIntruments()).type(MediaType.APPLICATION_JSON).build();
	}

	




}
