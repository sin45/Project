package com.starbucks;

import com.starbucks.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllProducts() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/products")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$[0].productId").exists())
              .andExpect(jsonPath("$[0].productName").exists());
    }

    @Test
    public void testGetProductById() throws Exception {
        // 假设数据库中有ID为1的商品
        ResultActions result = mockMvc.perform(get("/api/products/1")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.productId").value(1))
              .andExpect(jsonPath("$.productName").exists());
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        // 假设数据库中没有ID为9999的商品
        mockMvc.perform(get("/api/products/9999")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
} 