package com.stylight.url.prettier.datasource;

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

    public UrlMappingsDatasource() {
        logger.info("Initialized database for mappings");
        this.routeToPrettyUri = HashBiMap.create();
        this.queryParamsToPrettyUri = HashBiMap.create();
        this.queryParamsToRoute = new HashMap<String, String>();
    }

    @PostConstruct
    private void populateMappings() {
        logger.info("Begin inserting default entries into BiMap");
        this.queryParamsToPrettyUri.put("gender=female&tag=123&tag=1234", "/Women/Shoes/");
        this.queryParamsToRoute.put("gender=female&tag=123&tag=1234", "/products");

        this.queryParamsToPrettyUri.put("brand=123", "/Adidas/");
        this.queryParamsToRoute.put("brand=123", "/products");        
        // this.insertMapping("/products", "/Fashion");
        // this.insertMapping("/products?gender=female", "/Women/");
        // this.insertMapping("/products?gender=female&tag=123&tag=1234", "/Women/Shoes/");
        // this.insertMapping("/products?tag=5678", "/Boat--Shoes/");
        // this.insertMapping("/products?brand=123", "/Adidas/");
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
}
