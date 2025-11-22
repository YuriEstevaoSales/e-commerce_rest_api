package com.yuri.store.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.store.dtos.ProductDto;
import com.yuri.store.entities.Category;
import com.yuri.store.entities.Product;
import com.yuri.store.repositories.CategoryRepository;
import com.yuri.store.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;
    private Product testProduct;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testCategory = new Category("Eletrônicos");
        categoryRepository.save(testCategory);

        testProduct = Product.builder()
                .name("Notebook Dell")
                .description("Notebook 15 polegadas")
                .price(new BigDecimal("2500.00"))
                .category(testCategory)
                .build();
        productRepository.save(testProduct);

        productDto = new ProductDto();
        productDto.setName("Mouse Logitech");
        productDto.setDescription("Mouse sem fio");
        productDto.setPrice(new BigDecimal("150.00"));
        productDto.setCategoryId(testCategory.getId());
    }

    // Teste 6: Criar produto e listá-lo imediatamente
    @Test
    void testCreateProductAndListIt() throws Exception {
        // Criar produto
        MvcResult createResult = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo("Mouse Logitech")))
                .andExpect(jsonPath("$.price", equalTo(150.00)))
                .andReturn();

        // Listar todos os produtos
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", hasItems("Notebook Dell", "Mouse Logitech")));
    }

    // Teste 7: Listar produtos por categoria específica
    @Test
    void testGetProductsByCategoryAndVerifyFiltering() throws Exception {
        // Criar outra categoria
        Category outraCategoria = new Category("Livros");
        categoryRepository.save(outraCategoria);

        // Criar produto em outra categoria
        Product livro = Product.builder()
                .name("Clean Code")
                .description("Livro sobre código limpo")
                .price(new BigDecimal("80.00"))
                .category(outraCategoria)
                .build();
        productRepository.save(livro);

        // Listar produtos da categoria Eletrônicos
        mockMvc.perform(get("/products")
                .param("categoryId", String.valueOf(testCategory.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Notebook Dell")))
                .andExpect(jsonPath("$[0].category.name", equalTo("Eletrônicos")));

        // Listar produtos da categoria Livros
        mockMvc.perform(get("/products")
                .param("categoryId", String.valueOf(outraCategoria.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Clean Code")))
                .andExpect(jsonPath("$[0].category.name", equalTo("Livros")));
    }

    // Teste 8: Atualizar produto e verificar persistência com categoria
    @Test
    void testUpdateProductAndVerifyWithCategory() throws Exception {
        ProductDto updateDto = new ProductDto();
        updateDto.setName("Notebook Dell XPS");
        updateDto.setDescription("Notebook 15 polegadas - versão atualizada");
        updateDto.setPrice(new BigDecimal("3000.00"));
        updateDto.setCategoryId(testCategory.getId());

        // Atualizar produto
        mockMvc.perform(put("/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Notebook Dell XPS")))
                .andExpect(jsonPath("$.price", equalTo(3000.00)));

        // Verificar persistência
        mockMvc.perform(get("/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Notebook Dell XPS")))
                .andExpect(jsonPath("$.price", equalTo(3000.00)))
                .andExpect(jsonPath("$.category.name", equalTo("Eletrônicos")));
    }

    // Teste 9: Tentar criar produto com categoria inválida
    @Test
    void testCreateProductWithInvalidCategoryReturnsError() throws Exception {
        ProductDto invalidProductDto = new ProductDto();
        invalidProductDto.setName("Produto teste");
        invalidProductDto.setDescription("Descrição teste");
        invalidProductDto.setPrice(new BigDecimal("100.00"));
        invalidProductDto.setCategoryId((byte) 999); // Categoria inexistente

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto)))
                .andExpect(status().isBadRequest());
    }

    // Teste 10: Deletar produto e verificar que não está mais acessível
    @Test
    void testDeleteProductAndVerifyItNoLongerExists() throws Exception {
        Long productId = testProduct.getId();

        // Verificar que o produto existe
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Notebook Dell")));

        // Deletar o produto
        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNoContent());

        // Verificar que o produto não existe mais
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isNotFound());

        // Verificar que foi removido da lista
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
