package com.stylight.url.prettier.services;

import java.util.ArrayList;

import com.stylight.url.prettier.datasource.UrlMappingsDatasource;
import com.stylight.url.prettier.models.RequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class UrlPrettierServiceTests {

	private UrlMappingsDatasource dataSource = new UrlMappingsDatasource();

	private UrlPrettierService urlPrettierService;
  
	@BeforeEach
	void initUseCase() {
		this.dataSource.populateMappings();
		this.urlPrettierService = new UrlPrettierService(dataSource);
	}
  
	@Test
	void lookupPrettyRouteBySinglePathSegment() {
		RequestDTO requestDTO = new RequestDTO(new ArrayList<String>(Arrays.asList("/products")));
		assertThat(urlPrettierService.lookup(requestDTO).urls).isEqualTo(Arrays.asList("/Fashion/"));
	}

	@Test
	void lookupPrettyRouteByPathAndQueryParams() {
		RequestDTO requestDTO = new RequestDTO(new ArrayList<String>(Arrays.asList("/products?gender=female")));
		assertThat(urlPrettierService.lookup(requestDTO).urls).isEqualTo(Arrays.asList("/Women/"));
	}

	@Test
	void lookupPrettyRouteByPartialMatch() {
		RequestDTO requestDTO = new RequestDTO(new ArrayList<String>(Arrays.asList("/products?gender=female&tag=123&tag=1234&tag=5678&brand=123")));
		assertThat(urlPrettierService.lookup(requestDTO).urls).isEqualTo(Arrays.asList("/Women/Shoes/?tag=5678&brand=123"));
	}

	@Test
	void lookupPrettyRouteWithNoMatch() {
		RequestDTO requestDTO = new RequestDTO(new ArrayList<String>(Arrays.asList("/lookup?brand=123")));
		assertThat(urlPrettierService.lookup(requestDTO).urls).isEqualTo(Arrays.asList("/lookup?brand=123"));
	}

	@Test
	void reverseLookupRoutePathSegmentsOnly() {
		RequestDTO requestDTO = new RequestDTO(new ArrayList<String>(Arrays.asList("/Fashion/")));
		assertThat(urlPrettierService.reverseLookup(requestDTO).urls).isEqualTo(Arrays.asList("/products"));
	}

	@Test
	void reverseLookupRouteWithQueryParams() {
		RequestDTO requestDTO = new RequestDTO(new ArrayList<String>(Arrays.asList("/Boat--Shoes/")));
		assertThat(urlPrettierService.reverseLookup(requestDTO).urls).isEqualTo(Arrays.asList("/products?tag=5678"));
	}

	@Test
	void reverseLookupRoutePartialMatch() {
		RequestDTO requestDTO = new RequestDTO(new ArrayList<String>(Arrays.asList("/Boat--Shoes/Products")));
		assertThat(urlPrettierService.reverseLookup(requestDTO).urls).isEqualTo(Arrays.asList("/products?tag=5678/Products"));
	}
}
