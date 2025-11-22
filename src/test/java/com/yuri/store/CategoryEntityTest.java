package com.yuri.store.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryEntityTest {

    private Category category;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        category = new Category("Eletrônicos");
        category.setId((byte) 1);

        product1 = Product.builder()
                .id(1L)
                .name("Notebook")
                .price(new BigDecimal("2500.00"))
                .category(category)
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .price(new BigDecimal("50.00"))
                .category(category)
                .build();
    }

    @Test
    void testCategoryCreationWithName() {
        assertNotNull(category);
        assertEquals("Eletrônicos", category.getName());
        assertEquals((byte) 1, category.getId());
    }

    @Test
    void testCategoryCreationWithId() {
        Category cat = new Category((byte) 2);
        assertEquals((byte) 2, cat.getId());
        assertNull(cat.getName());
    }

    @Test
    void testCategoryInitialProductsIsEmpty() {
        Category newCategory = new Category("Livros");
        assertNotNull(newCategory.getProducts());
        assertTrue(newCategory.getProducts().isEmpty());
    }

    @Test
    void testAddProductsToCategory() {
        category.getProducts().add(product1);
        category.getProducts().add(product2);

        assertEquals(2, category.getProducts().size());
        assertTrue(category.getProducts().contains(product1));
        assertTrue(category.getProducts().contains(product2));
    }

    @Test
    void testCategoryWithMultipleProducts() {
        for (int i = 0; i < 5; i++) {
            Product p = Product.builder()
                    .id((long) i)
                    .name("Produto " + i)
                    .price(new BigDecimal("100.00"))
                    .category(category)
                    .build();
            category.getProducts().add(p);
        }

        assertEquals(5, category.getProducts().size());
    }

    @Test
    void testCategoryIdAssignment() {
        byte idToSet = 10;
        category.setId(idToSet);
        assertEquals(idToSet, category.getId());
    }

    @Test
    void testCategoryNameAssignment() {
        String newName = "Roupas";
        category.setName(newName);
        assertEquals(newName, category.getName());
    }

    @Test
    void testRemoveProductFromCategory() {
        category.getProducts().add(product1);
        category.getProducts().add(product2);
        
        category.getProducts().remove(product1);

        assertEquals(1, category.getProducts().size());
        assertTrue(category.getProducts().contains(product2));
        assertFalse(category.getProducts().contains(product1));
    }

    @Test
    void testCategoryNoArgsConstructor() {
        Category cat = new Category();
        assertNull(cat.getId());
        assertNull(cat.getName());
        assertNotNull(cat.getProducts());
    }
}
