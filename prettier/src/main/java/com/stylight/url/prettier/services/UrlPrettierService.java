package com.stylight.url.prettier.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.stylight.url.prettier.services.interfaces.UrlPrettierServiceInterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.stylight.url.prettier.datasource.UrlMappingsDatasource;
import com.stylight.url.prettier.models.RequestDTO;
import com.stylight.url.prettier.models.ResponseDTO;

@Service
public class UrlPrettierService implements UrlPrettierServiceInterface{

    @Autowired
    private UrlMappingsDatasource urlMappingsDatasource;

    public ResponseDTO reverseLookup(RequestDTO requestDTO) {
        List<String> reverseMatches = new ArrayList<String>();
        requestDTO.urls.forEach(uri -> {
            // Pretty url can only contain path segments else raise error
            // TODO: Create error here if the url contains query params or if path segments length == 0.
            List<String> pathSegments = UriComponentsBuilder.fromUriString(uri).build().getPathSegments();
            // First check if the path has a direct route mapping correspondence
            String reverseUrl = extractReversedUrl(pathSegments);

            if (reverseUrl == null) {
                List<String> segmentsAccumulator = new ArrayList<String>();
                for (String segment: pathSegments) {
                    segmentsAccumulator.add(segment);
                    String reversedSegment = extractReversedUrl(segmentsAccumulator);
                    // Since matches are initiated from left to right, we can end comparing segments when a null is encountered
                    if (reversedSegment == null) {
                        segmentsAccumulator.remove(segment);
                        // Keep unmatched part as is.
                        reverseUrl = reverseUrl + "/" + String.join("/", 
                            pathSegments.stream().filter(x -> !segmentsAccumulator.contains(x)).collect(Collectors.toList()));
                        break;
                    }
                    reverseUrl = reversedSegment;
                }
            }
            reverseMatches.add(reverseUrl);
        });
        return new ResponseDTO(reverseMatches);
    }

    private String extractReversedUrl(List<String> pathSegments) {
        String reverseUrl = urlMappingsDatasource.getRouteToPrettyUri().inverse().get("/" + String.join("/", pathSegments) + "/");
        
        if (reverseUrl == null) {
            // Reverse get by url
            String queryParams = urlMappingsDatasource.getQueryParamsToPrettyUri().inverse().get("/" + String.join("/", pathSegments) + "/");
            if (queryParams != null) {
                reverseUrl = urlMappingsDatasource.getQueryParamsToRoute().get(queryParams);
                reverseUrl = reverseUrl != null ? reverseUrl + "?" + queryParams: reverseUrl;
            }
            // Add the delimeter for query params
        }
        return reverseUrl;
    }
}
