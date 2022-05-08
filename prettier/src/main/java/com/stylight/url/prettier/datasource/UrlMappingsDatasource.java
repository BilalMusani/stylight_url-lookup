package com.stylight.url.prettier.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import com.google.common.collect.HashBiMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UrlMappingsDatasource {
    private static final Logger logger = LoggerFactory.getLogger(UrlMappingsDatasource.class);
    private HashBiMap<String, String> routeToPrettyUri;
    private HashBiMap<String, String> queryParamsToPrettyUri;
    private HashMap<String, String> queryParamsToRoute;
    private HashMap<String, List<String>> routeToQueryParams;

    public UrlMappingsDatasource() {
        logger.info("Initialized database for mappings");
        this.routeToPrettyUri = HashBiMap.create();
        this.queryParamsToPrettyUri = HashBiMap.create();
        this.queryParamsToRoute = new HashMap<String, String>();
        this.routeToQueryParams = new HashMap<String, List<String>>();
    }

    @PostConstruct
    private void populateMappings() {
        logger.info("Begin inserting default entries into BiMap");

        this.routeToQueryParams.put("/products", new ArrayList<String>(
            Arrays.asList("gender=female", "gender=female&tag=123&tag=1234", "brand=123", "tag=5678")));

        this.queryParamsToRoute.put("gender=female&tag=123&tag=1234", "/products");
        this.queryParamsToRoute.put("gender=female", "/products");
        this.queryParamsToRoute.put("brand=123", "/products");
        this.queryParamsToRoute.put("tag=5678", "/products");
        this.queryParamsToRoute.put("brand=123", "/products");

        this.queryParamsToPrettyUri.put("gender=female", "/Women/");
        this.queryParamsToPrettyUri.put("brand=123", "/Adidas/");
        this.queryParamsToPrettyUri.put("tag=5678", "/Boat--Shoes/");        
        this.queryParamsToPrettyUri.put("gender=female&tag=123&tag=1234", "/Women/Shoes/");

        this.routeToPrettyUri.put("/products", "/Fashion/");
        logger.info("End inserting default entries into BiMap");
    }


    public HashBiMap<String, String> getRouteToPrettyUri() {
        return routeToPrettyUri;
    }

    public HashBiMap<String, String> getQueryParamsToPrettyUri() {
        return queryParamsToPrettyUri;
    }

    public HashMap<String, String> getQueryParamsToRoute() {
        return queryParamsToRoute;
    }

    public HashMap<String, List<String>> getRouteToQueryParams() {
        return routeToQueryParams;
    }
}
