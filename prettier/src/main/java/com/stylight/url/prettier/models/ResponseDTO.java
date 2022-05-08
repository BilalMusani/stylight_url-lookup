package com.stylight.url.prettier.models;

import java.util.List;

public class ResponseDTO {
    public List<String> urls;

    public ResponseDTO() {
    }

    public ResponseDTO(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
