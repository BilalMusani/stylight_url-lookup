package com.stylight.url.prettier.services.interfaces;

import com.stylight.url.prettier.models.RequestDTO;
import com.stylight.url.prettier.models.ResponseDTO;

public interface UrlPrettierServiceInterface {
    public ResponseDTO reverseLookup(RequestDTO requestDTO);
}
