package com.stylight.url.prettier.controllers;

import com.stylight.url.prettier.models.RequestDTO;
import com.stylight.url.prettier.models.ResponseDTO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.stylight.url.prettier.services.UrlPrettierService;
@RestController
public class UrlPrettierController {
    
    @Autowired
    private UrlPrettierService urlPrettierService;

	@PostMapping("/lookup")
	public void lookup(@RequestBody RequestDTO request) {
		throw new UnsupportedOperationException();
	}

    @PostMapping("/reverseLookup")
    public ResponseDTO reverseLookup(@RequestBody RequestDTO request) {
		return this.urlPrettierService.reverseLookup(request);
	}
}
