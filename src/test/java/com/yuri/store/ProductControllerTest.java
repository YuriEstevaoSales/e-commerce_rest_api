package com.yuri.store.controllers;

import com.yuri.store.dtos.ProductDto;
import com.yuri.store.entities.Category;
import com.yuri.store.entities.Product;
import com.yuri.store.mappers.ProductMapper;
import com.yuri.store.repositories.CategoryRepository;
import com.yuri.store.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category((byte) 1);
        testCategory.setName("Eletrônicos");

        testProduct = Product.builder()
                .id(1L)
                .name("Notebook")
                .description("Notebook gamer")
                .price(new BigDecimal("2500.00"))
                .category(testCategory)
                .build();

        testProductDto = new ProductDto();
        testProductDto.setId(1L);
        testProductDto.setName("Notebook");
        testProductDto.setDescription("Notebook gamer");
        testProductDto.setPrice(new BigDecimal("2500.00"));
        testProductDto.setCategoryId((byte) 1);
    }

    @Test
    void testGetAllProductsWithoutCategory() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findAllWithCategory()).thenReturn(products);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        var result = productController.getAllProducts(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAllWithCategory();
    }

    @Test
    void testGetAllProductsByCategory() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findByCategoryId((byte) 1)).thenReturn(products);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        var result = productController.getAllProducts((byte) 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByCategoryId((byte) 1);
    }

    @Test
    void testGetProductByIdSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        var response = productController.getProduct(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProductDto, response.getBody());
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        var response = productController.getProduct(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateProductSuccess() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));
        when(productMapper.toEntity(testProductDto)).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        var response = productController.createProduct(testProductDto, uriBuilder);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProductInvalidCategory() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        testProductDto.setCategoryId((byte) 999);
        when(categoryRepository.findById((byte) 999)).thenReturn(Optional.empty());

        var response = productController.createProduct(testProductDto, uriBuilder);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateProductSuccess() {
        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productMapper).update(testProductDto, testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        var response = productController.updateProduct(1L, testProductDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProductInvalidCategory() {
        testProductDto.setCategoryId((byte) 999);
        when(categoryRepository.findById((byte) 999)).thenReturn(Optional.empty());

        var response = productController.updateProduct(1L, testProductDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateProductNotFound() {
        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        var response = productController.updateProduct(999L, testProductDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteProductSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);

        var response = productController.deleteProduct(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    void testDeleteProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        var response = productController.deleteProduct(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
