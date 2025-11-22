package com.yuri.store.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class UserEntityTest {

    private User user;
    private Address address;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("João");
        user.setEmail("joao@test.com");
        user.setPassword("senha123");

        address = Address.builder()
                .id(1L)
                .street("Rua A")
                .city("São Paulo")
                .state("SP")
                .zip("01234-567")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Produto teste")
                .price(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void testAddAddressToUser() {
        user.addAddress(address);

        assertTrue(user.getAddresses().contains(address));
        assertEquals(user, address.getUser());
    }

    @Test
    void testRemoveAddressFromUser() {
        user.addAddress(address);
        user.removeAddress(address);

        assertFalse(user.getAddresses().contains(address));
        assertNull(address.getUser());
    }

    @Test
    void testAddFavoriteProduct() {
        user.addFavoriteProduct(product);

        assertTrue(user.getFavoriteProducts().contains(product));
    }

    @Test
    void testAddMultipleAddresses() {
        Address address2 = Address.builder()
                .id(2L)
                .street("Rua B")
                .city("Rio de Janeiro")
                .state("RJ")
                .zip("98765-432")
                .build();

        user.addAddress(address);
        user.addAddress(address2);

        assertEquals(2, user.getAddresses().size());
    }

    @Test
    void testAddMultipleFavoriteProducts() {
        Product product2 = Product.builder()
                .id(2L)
                .name("Produto 2")
                .price(new BigDecimal("200.00"))
                .build();

        user.addFavoriteProduct(product);
        user.addFavoriteProduct(product2);

        assertEquals(2, user.getFavoriteProducts().size());
    }

    @Test
    void testUserToString() {
        String result = user.toString();

        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("João"));
        assertTrue(result.contains("joao@test.com"));
    }

    @Test
    void testUserInitialValues() {
        User newUser = new User();

        assertNull(newUser.getId());
        assertNull(newUser.getName());
        assertNull(newUser.getEmail());
        assertNull(newUser.getPassword());
        assertNotNull(newUser.getAddresses());
        assertNotNull(newUser.getFavoriteProducts());
        assertTrue(newUser.getAddresses().isEmpty());
        assertTrue(newUser.getFavoriteProducts().isEmpty());
    }

    @Test
    void testRemoveNonExistentAddress() {
        user.removeAddress(address);

        assertFalse(user.getAddresses().contains(address));
    }

    @Test
    void testAddSameAddressTwice() {
        user.addAddress(address);
        user.addAddress(address);

        assertEquals(2, user.getAddresses().size());
    }

    @Test
    void testAddAddressAndVerifyUserReference() {
        user.addAddress(address);

        assertSame(user, address.getUser());
    }
}
