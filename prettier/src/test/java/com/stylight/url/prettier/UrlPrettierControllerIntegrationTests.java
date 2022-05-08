package com.stylight.url.prettier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylight.url.prettier.models.RequestDTO;
import com.stylight.url.prettier.models.ResponseDTO;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
public class UrlPrettierControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void prettifyUrl() throws Exception {
      RequestDTO requestDTO = new RequestDTO(Arrays.asList(
        "/products"));

      mockMvc.perform(post("/lookup")
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(requestDTO)))
              .andExpect(jsonPath("$.urls").value(Matchers.contains("/Fashion/")))
              .andExpect(status().isOk());
    
    }

    @Test
    void reverseLookupUrl() throws Exception {
      RequestDTO requestDTO = new RequestDTO(Arrays.asList(
        "/Fashion"));

      mockMvc.perform(post("/reverseLookup")
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(requestDTO)))
              .andExpect(jsonPath("$.urls").value(Matchers.contains("/products")))
              .andExpect(status().isOk());
    
    }

    @Test
    void prettifyAndReverseLookupUrl() throws Exception {
        RequestDTO requestDTO = new RequestDTO(Arrays.asList(
            "/products", 
            "/products?gender=female", 
            "/products?gender=female&tag=123&tag=1234", 
            "/products?gender=female&tag=123&tag=1234&tag=5678&brand=123"));

        mockMvc.perform(post("/lookup")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(jsonPath("$.urls").value(Matchers.contains("/Fashion/","/Women/","/Women/Shoes/","/Women/Shoes/?tag=5678&brand=123")))
            .andExpect(status().isOk())
            .andDo(result ->  {
                String json = result.getResponse().getContentAsString();
                mockMvc.perform(post("/reverseLookup")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(new RequestDTO(objectMapper.readValue(json, ResponseDTO.class).urls))))
                    .andExpect(jsonPath("$.urls").value(Matchers.contains(requestDTO.urls.toArray())))
                    .andExpect(status().isOk());
            });
    }
}
