package com.stylight.url.prettier.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlPrettierController {
    
	@GetMapping("/lookup")
	public void lookup(@RequestParam List<String> values) {
		throw new UnsupportedOperationException();
	}

    @GetMapping("/reverseLookup")
    public void reverseLookup(@RequestParam List<String> values) {
		throw new UnsupportedOperationException();
	}
}
