package com.yuri.store.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuri.store.dtos.ChangePasswordRequest;
import com.yuri.store.dtos.RegisterUserRequest;
import com.yuri.store.dtos.UpdateUserRequest;
import com.yuri.store.entities.User;
import com.yuri.store.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private RegisterUserRequest registerRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("João Silva");
        testUser.setEmail("joao@test.com");
        testUser.setPassword("senha123");
        userRepository.save(testUser);

        registerRequest = new RegisterUserRequest();
        registerRequest.setName("Maria Santos");
        registerRequest.setEmail("maria@test.com");
        registerRequest.setPassword("senha456");
    }

    // Teste 1: Fluxo completo de criar usuário e recuperá-lo
    @Test
    void testCreateUserAndRetrieveItFlow() throws Exception {
        // Criar usuário
        MvcResult createResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo("Maria Santos")))
                .andExpect(jsonPath("$.email", equalTo("maria@test.com")))
                .andReturn();

        // Extrair ID do resultado
        String responseBody = createResult.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseBody).get("id").asLong();

        // Recuperar o usuário criado
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(userId.intValue())))
                .andExpect(jsonPath("$.name", equalTo("Maria Santos")))
                .andExpect(jsonPath("$.email", equalTo("maria@test.com")));
    }

    // Teste 2: Listar usuários e verificar ordenação
    @Test
    void testGetAllUsersWithSortingByEmail() throws Exception {
        // Adicionar mais um usuário
        User user2 = new User();
        user2.setName("Ana Costa");
        user2.setEmail("ana@test.com");
        user2.setPassword("senha789");
        userRepository.save(user2);

        // Listar com ordenação por email
        mockMvc.perform(get("/users")
                .param("sort", "email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", equalTo("ana@test.com")))
                .andExpect(jsonPath("$[1].email", equalTo("joao@test.com")));
    }

    // Teste 3: Atualizar usuário e verificar persistência
    @Test
    void testUpdateUserAndVerifyPersistence() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("João Silva Atualizado");
        updateRequest.setEmail("joao.atualizado@test.com");

        // Atualizar usuário
        mockMvc.perform(put("/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("João Silva Atualizado")))
                .andExpect(jsonPath("$.email", equalTo("joao.atualizado@test.com")));

        // Verificar se os dados foram persistidos no banco
        mockMvc.perform(get("/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("João Silva Atualizado")))
                .andExpect(jsonPath("$.email", equalTo("joao.atualizado@test.com")));
    }

    // Teste 4: Mudar senha e tentar login com senha anterior (comportamento esperado)
    @Test
    void testChangePasswordAndVerifyOldPasswordNoLongerWorks() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword("senha123");
        changePasswordRequest.setNewPassword("novaSenha999");

        // Mudar senha com sucesso
        mockMvc.perform(post("/users/{id}/change-password", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isNoContent());

        // Tentar mudar senha novamente com a senha antiga (deve falhar)
        ChangePasswordRequest failChangePasswordRequest = new ChangePasswordRequest();
        failChangePasswordRequest.setOldPassword("senha123");
        failChangePasswordRequest.setNewPassword("outraSenha");

        mockMvc.perform(post("/users/{id}/change-password", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(failChangePasswordRequest)))
                .andExpect(status().isUnauthorized());
    }

    // Teste 5: Deletar usuário e verificar que não está mais acessível
    @Test
    void testDeleteUserAndVerifyItNoLongerExists() throws Exception {
        Long userId = testUser.getId();

        // Verificar que o usuário existe
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk());

        // Deletar o usuário
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Verificar que o usuário não existe mais
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
