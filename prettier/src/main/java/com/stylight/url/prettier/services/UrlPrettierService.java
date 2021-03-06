package com.stylight.url.prettier.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.stylight.url.prettier.services.interfaces.UrlPrettierServiceInterface;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

import com.stylight.url.prettier.datasource.UrlMappingsDatasource;
import com.stylight.url.prettier.models.RequestDTO;
import com.stylight.url.prettier.models.ResponseDTO;

@Service
@RequiredArgsConstructor
public class UrlPrettierService implements UrlPrettierServiceInterface{

    private final UrlMappingsDatasource urlMappingsDatasource;

    public ResponseDTO lookup(RequestDTO requestDTO) {
        List<String> matches = new ArrayList<String>();
        requestDTO.urls.forEach(uri -> {
            if(uri == null) {
                matches.add(uri);
                return;
            }
            List<String> pathSegments = UriComponentsBuilder.fromUriString(uri).build().getPathSegments();
            String matchedUrl = null;
            List<String> accumulatedSegments = new ArrayList<String>();
             
            for(String segment: pathSegments) {
                accumulatedSegments.add(segment);
                String match = this.urlMappingsDatasource.getRouteToPrettyUri().get(("/" + String.join("/", accumulatedSegments)));
                matchedUrl = match != null ? match : matchedUrl != null ? matchedUrl + "/" + segment : "/" + segment;
            }

            String querySegments = this.getQuerySegments(uri);
                        
            if (!querySegments.isEmpty()) {
                matchedUrl = matchedUrl + "?" + querySegments;

                List<String> routeLinkedQuerySegments = this.urlMappingsDatasource.getRouteToQueryParams().get("/" + String.join("/", pathSegments));

                if (routeLinkedQuerySegments != null) {
                    Optional<String> longestRouteLinkedQuerySegment = routeLinkedQuerySegments.stream().filter(x -> querySegments.indexOf(x) == 0).max(Comparator.comparingInt(String::length));
                    
                    if (longestRouteLinkedQuerySegment.isPresent()) {
                        String unmatched = querySegments.substring(longestRouteLinkedQuerySegment.get().length(), querySegments.length());
                        matchedUrl = this.urlMappingsDatasource.getQueryParamsToPrettyUri().get(longestRouteLinkedQuerySegment.get());
                        if (unmatched.length() > 0) {
                            // Remove any starting ampersands
                            matchedUrl = matchedUrl + "?" + unmatched.replaceFirst("&", "");
                        }
                    }
                }
            }
            matches.add(matchedUrl == null ? uri : matchedUrl);
        });

        return new ResponseDTO(matches);
    }

    public String getQuerySegments(String uri) {
        return String.join(
                "&", UriComponentsBuilder.fromUriString(uri).build().getQueryParams()
                .entrySet()
                .stream()
                .map(e -> String.join("&", e.getValue().stream().map(x -> e.getKey() + "=" + x).collect(Collectors.toList())))
                .collect(Collectors.toList()));
    }

    public ResponseDTO reverseLookup(RequestDTO requestDTO) {
        List<String> reverseMatches = new ArrayList<String>();
        requestDTO.urls.forEach(uri -> {
            if(uri == null) {
                reverseMatches.add(uri);
                return;
            }
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
                        if (segmentsAccumulator.size() > 0) {
                            reverseUrl = reverseUrl + "/" + String.join("/", 
                                pathSegments.stream().filter(x -> !segmentsAccumulator.contains(x)).collect(Collectors.toList()));
                        }
                        break;
                    }
                    reverseUrl = reversedSegment;
                }
            }

            if(reverseUrl != null) {
                String querySegments = this.getQuerySegments(uri);
                // If the reverseUrl already has query segments, append the query segments from the uri to the reverseUrl
                String reverseUrlQuerySegments = this.getQuerySegments(reverseUrl);
                reverseUrl = querySegments.length() > 0 ? reverseUrl + (reverseUrlQuerySegments.length() > 0 ? "&": "?") + querySegments : reverseUrl;
            }

            reverseMatches.add(reverseUrl == null ? uri : reverseUrl);
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
        }
        return reverseUrl;
    }
}
