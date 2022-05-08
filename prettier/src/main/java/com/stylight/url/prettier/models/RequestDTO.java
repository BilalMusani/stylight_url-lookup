package com.stylight.url.prettier.models;

import java.util.List;

public class RequestDTO {
    public List<String> urls;

    public RequestDTO() {
    }

    public RequestDTO(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
