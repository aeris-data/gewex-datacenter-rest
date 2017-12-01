package fr.aeris.gewex.datacenter.rest.services.config;

public class GewexDatacenterConfig {
	
	private String metadataService;

	public String getMetadataService() {
		return metadataService;
	}

	public void setMetadataService(String metadataService) {
		this.metadataService = metadataService;
	}
	
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	private String workingDirectory;

	
}
