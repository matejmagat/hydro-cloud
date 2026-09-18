package hr.fer.hydro.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.hydro.auth.auth.dto.LoginReq;
import hr.fer.hydro.auth.auth.dto.RegisterReq;
import hr.fer.hydro.auth.auth.service.AuthService;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.repositories.User2FADao;
import hr.fer.hydro.auth.persistence.repositories.User2FAScratchCodeDao;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import hr.fer.hydro.auth.persistence.repositories.UserRoleDao;
import hr.fer.hydro.config.core.UserCoreLocalThread;
import hr.fer.hydro.user.dto.req.UpdateUserReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User Management Integration Tests")
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:15-3.3")
                    .asCompatibleSubstituteFor("postgres")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
        registry.add("spring.sql.init.data-locations", () -> "classpath:data.sql");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private User2FADao user2FADao;

    @Autowired
    private User2FAScratchCodeDao user2FAScratchCodeDao;

    @Autowired
    private AuthService authService;



    @AfterEach
    void cleanup() {
        UserCoreLocalThread.deleteUserInfo();
        userDao.findByUsername("regular_user").ifPresent(user -> {
            user2FAScratchCodeDao.deleteAll(user2FAScratchCodeDao.findAllByUser(user));
            user2FADao.findByUser(user).ifPresent(user2FADao::delete);
            userDao.delete(user);
        });
        userDao.findByUsername("delete_me").ifPresent(user -> {
            user2FAScratchCodeDao.deleteAll(user2FAScratchCodeDao.findAllByUser(user));
            user2FADao.findByUser(user).ifPresent(user2FADao::delete);
            userDao.delete(user);
        });
    }

    private String getAdminToken() {
        return authService.login(new LoginReq("admin", "hydroAdmin")).accessToken();
    }

    private String getUserManagerToken() {
        return authService.login(new LoginReq("user_manager", "hydroUserManager")).accessToken();
    }

    private String getDataManagerToken() {
        return authService.login(new LoginReq("data_manager", "hydroDataManager")).accessToken();
    }

    private String createAndGetRegularUserToken() {
        RegisterReq regularUser = new RegisterReq(
                "Regular", "User", "regular_user",
                "regular@example.hr", "SecurePass123!"
        );
        return authService.signUp(regularUser).accessToken();
    }

    @Nested
    @DisplayName("Get All Users Scenarios")
    class GetAllUsersTests {

        @Test
        @DisplayName("Admin can retrieve all users")
        void adminCanRetrieveAllUsers() throws Exception {
            String adminToken = getAdminToken();
            mockMvc.perform(get("/user/all-users")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(3))); // admin, user_manager, data_manager
        }

        @Test
        @DisplayName("User manager can retrieve all users")
        void userManagerCanRetrieveAllUsers() throws Exception {
            String userManagerToken = getUserManagerToken();
            mockMvc.perform(get("/user/all-users")
                            .header("Authorization", "Bearer " + userManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("Data manager cannot retrieve all users")
        void dataManagerCannotRetrieveAllUsers() throws Exception {
            String dataManagerToken = getDataManagerToken();

            mockMvc.perform(get("/user/all-users")
                            .header("Authorization", "Bearer " + dataManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Regular user cannot retrieve all users")
        void regularUserCannotRetrieveAllUsers() throws Exception {
            String regularUserToken = createAndGetRegularUserToken();

            mockMvc.perform(get("/user/all-users")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated request is rejected")
        void unauthenticatedRequestIsRejected() throws Exception {
            mockMvc.perform(get("/user/all-users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get Detailed User Info Scenarios")
    class GetUserInfoTests {

        @Test
        @DisplayName("Admin can retrieve detailed user information")
        void adminCanRetrieveDetailedUserInfo() throws Exception {
            String adminToken = getAdminToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            mockMvc.perform(get("/user/" + regularUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value("regular_user"))
                    .andExpect(jsonPath("$.email").value("regular@example.hr"))
                    .andExpect(jsonPath("$.firstName").value("Regular"))
                    .andExpect(jsonPath("$.lastName").value("User"));
        }

        @Test
        @DisplayName("User manager can retrieve detailed user information")
        void userManagerCanRetrieveDetailedUserInfo() throws Exception {
            String userManagerToken = getUserManagerToken();
            String regularUserToken = createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            mockMvc.perform(get("/user/" + regularUser.getId())
                            .header("Authorization", "Bearer " + userManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("regular_user"));
        }

        @Test
        @DisplayName("Data manager cannot retrieve detailed user information")
        void dataManagerCannotRetrieveDetailedUserInfo() throws Exception {
            String dataManagerToken = getDataManagerToken();
            String regularUserToken = createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            mockMvc.perform(get("/user/" + regularUser.getId())
                            .header("Authorization", "Bearer " + dataManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 404 when user does not exist")
        void returns404WhenUserDoesNotExist() throws Exception {
            String adminToken = getAdminToken();

            mockMvc.perform(get("/user/99999")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Update User Scenarios")
    class UpdateUserTests {

        @Test
        @DisplayName("Admin can update user information")
        void adminCanUpdateUserInfo() throws Exception {
            String adminToken = getAdminToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            UpdateUserReq updateRequest = new UpdateUserReq();
            updateRequest.setId(regularUser.getId());
            updateRequest.setFirstName("Updated");
            updateRequest.setLastName("Name");
            updateRequest.setEmail("updated@example.hr");
            updateRequest.setUsername(regularUser.getUsername());

            mockMvc.perform(put("/user")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("Name"))
                    .andExpect(jsonPath("$.email").value("updated@example.hr"))
                    .andExpect(jsonPath("$.username").value(regularUser.getUsername()));
        }

        @Test
        @DisplayName("User manager can update user information")
        void userManagerCanUpdateUserInfo() throws Exception {
            String userManagerToken = getUserManagerToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            UpdateUserReq updateRequest = new UpdateUserReq();
            updateRequest.setId(regularUser.getId());
            updateRequest.setFirstName("Manager");
            updateRequest.setLastName("Updated");
            updateRequest.setEmail(regularUser.getEmail());
            updateRequest.setUsername(regularUser.getUsername());

            mockMvc.perform(put("/user")
                            .header("Authorization", "Bearer " + userManagerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Manager"))
                    .andExpect(jsonPath("$.lastName").value("Updated"))
                    .andExpect(jsonPath("$.email").value(regularUser.getEmail()))
                    .andExpect(jsonPath("$.username").value(regularUser.getUsername()));
        }

        @Test
        @DisplayName("Data manager cannot update user information")
        void dataManagerCannotUpdateUserInfo() throws Exception {
            String dataManagerToken = getDataManagerToken();
            String regularUserToken = createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            UpdateUserReq updateRequest = new UpdateUserReq();
            updateRequest.setId(regularUser.getId());
            updateRequest.setFirstName("Should");
            updateRequest.setLastName("Fail");

            mockMvc.perform(put("/user")
                            .header("Authorization", "Bearer " + dataManagerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 404 when updating non-existent user")
        void returns404WhenUpdatingNonExistentUser() throws Exception {
            String adminToken = getAdminToken();

            UpdateUserReq updateRequest = new UpdateUserReq();
            updateRequest.setId(99999);
            updateRequest.setFirstName("NonExistent");

            mockMvc.perform(put("/user")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Validates update request with missing required fields")
        void validatesUpdateRequestWithMissingFields() throws Exception {
            String adminToken = getAdminToken();
            String invalidPayload = "{\"id\": null}";

            mockMvc.perform(put("/user")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPayload))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Delete User Scenarios")
    class DeleteUserTests {

        @Test
        @DisplayName("Admin can delete user")
        void adminCanDeleteUser() throws Exception {
            String adminToken = getAdminToken();

            RegisterReq userToDelete = new RegisterReq(
                    "ToDelete", "User", "delete_me",
                    "delete@example.hr", "SecurePass123!"
            );
            authService.signUp(userToDelete);
            UserEntity deletableUser = userDao.findByUsername("delete_me").orElseThrow();

            mockMvc.perform(delete("/user/" + deletableUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify user is deleted
            assertThat(userDao.findByUsername("delete_me").orElse(null)).isNull();

        }

        @Test
        @DisplayName("User manager can delete user")
        void userManagerCannotDeleteUser() throws Exception {
            String userManagerToken = getUserManagerToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            mockMvc.perform(delete("/user/" + regularUser.getId())
                            .header("Authorization", "Bearer " + userManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Data manager cannot delete user")
        void dataManagerCannotDeleteUser() throws Exception {
            String dataManagerToken = getDataManagerToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            mockMvc.perform(delete("/user/" + regularUser.getId())
                            .header("Authorization", "Bearer " + dataManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Regular user cannot delete user")
        void regularUserCannotDeleteUser() throws Exception {
            String regularUserToken = createAndGetRegularUserToken();
            UserEntity adminUser = userDao.findByUsername("admin").orElseThrow();

            mockMvc.perform(delete("/user/" + adminUser.getId())
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 404 when deleting non-existent user")
        void returns404WhenDeletingNonExistentUser() throws Exception {
            String adminToken = getAdminToken();

            mockMvc.perform(delete("/user/99999")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Authorization Edge Cases")
    class AuthorizationEdgeCaseTests {

        @Test
        @DisplayName("Expired token is rejected")
        void expiredTokenIsRejected() throws Exception {
            String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

            mockMvc.perform(get("/user/all-users")
                            .header("Authorization", "Bearer " + expiredToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Malformed token is rejected")
        void malformedTokenIsRejected() throws Exception {
            mockMvc.perform(get("/user/all-users")
                            .header("Authorization", "Bearer invalid.token.here")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Missing Authorization header is rejected")
        void missingAuthorizationHeaderIsRejected() throws Exception {
            mockMvc.perform(get("/user/all-users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get Logged In User Info Scenarios")
    class GetLoggedInUserInfoTests {

        @Test
        @DisplayName("Admin can retrieve their own user information")
        void adminCanRetrieveOwnInfo() throws Exception {
            String adminToken = getAdminToken();

            mockMvc.perform(get("/user/me")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value("admin"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("User manager can retrieve their own user information")
        void userManagerCanRetrieveOwnInfo() throws Exception {
            String userManagerToken = getUserManagerToken();

            mockMvc.perform(get("/user/me")
                            .header("Authorization", "Bearer " + userManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("user_manager"))
                    .andExpect(jsonPath("$.role").value("USER_MANAGER"));
        }

        @Test
        @DisplayName("Data manager can retrieve their own user information")
        void dataManagerCanRetrieveOwnInfo() throws Exception {
            String dataManagerToken = getDataManagerToken();

            mockMvc.perform(get("/user/me")
                            .header("Authorization", "Bearer " + dataManagerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("data_manager"))
                    .andExpect(jsonPath("$.role").value("DATA_MANAGER"));
        }

        @Test
        @DisplayName("Regular user can retrieve their own user information")
        void regularUserCanRetrieveOwnInfo() throws Exception {
            String regularUserToken = createAndGetRegularUserToken();

            mockMvc.perform(get("/user/me")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("regular_user"))
                    .andExpect(jsonPath("$.email").value("regular@example.hr"))
                    .andExpect(jsonPath("$.firstName").value("Regular"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("Unauthenticated request is rejected")
        void unauthenticatedRequestIsRejected() throws Exception {
            mockMvc.perform(get("/user/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Update User Role Scenarios")
    class UpdateUserRoleTests {

        @Test
        @DisplayName("Admin can update regular user role to USER_MANAGER")
        void adminCanUpdateRegularUserToUserManager() throws Exception {
            String adminToken = getAdminToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            String updateRolePayload = String.format(
                    "{\"userId\": %d, \"role\": \"USER_MANAGER\"}",
                    regularUser.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isOk());

            // Verify role was updated
            mockMvc.perform(get("/user/" + regularUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("USER_MANAGER"));
        }

        @Test
        @DisplayName("Admin can update regular user role to DATA_MANAGER")
        void adminCanUpdateRegularUserToDataManager() throws Exception {
            String adminToken = getAdminToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            String updateRolePayload = String.format(
                    "{\"userId\": %d, \"role\": \"DATA_MANAGER\"}",
                    regularUser.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Admin cannot update another admin's role")
        void adminCannotUpdateAnotherAdminRole() throws Exception {
            String adminToken = getAdminToken();
            UserEntity adminUser = userDao.findByUsername("admin").orElseThrow();

            String updateRolePayload = String.format(
                    "{\"userId\": %d, \"role\": \"USER\"}",
                    adminUser.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin cannot downgrade user to their own role level")
        void adminCannotDowngradeToOwnLevel() throws Exception {
            String adminToken = getAdminToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            String updateRolePayload = String.format(
                    "{\"userId\": %d, \"role\": \"ADMIN\"}",
                    regularUser.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("User manager can update user roles")
        void userManagerCannotUpdateUserRoles() throws Exception {
            String userManagerToken = getUserManagerToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            String updateRolePayload = String.format(
                    "{\"userId\": %d, \"role\": \"DATA_MANAGER\"}",
                    regularUser.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + userManagerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Data manager cannot update user roles")
        void dataManagerCannotUpdateUserRoles() throws Exception {
            String dataManagerToken = getDataManagerToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            String updateRolePayload = String.format(
                    "{\"userId\": %d, \"role\": \"USER_MANAGER\"}",
                    regularUser.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + dataManagerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Regular user cannot update user roles")
        void regularUserCannotUpdateUserRoles() throws Exception {
            String regularUserToken = createAndGetRegularUserToken();
            UserEntity dataManager = userDao.findByUsername("data_manager").orElseThrow();

            String updateRolePayload = String.format(
                    "{\"userId\": %d, \"role\": \"USER\"}",
                    dataManager.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + regularUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 404 when updating role for non-existent user")
        void returns404WhenUpdatingRoleForNonExistentUser() throws Exception {
            String adminToken = getAdminToken();

            String updateRolePayload = "{\"userId\": 99999, \"role\": \"USER_MANAGER\"}";

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRolePayload))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Validates update role request with invalid role")
        void validatesUpdateRoleRequestWithInvalidRole() throws Exception {
            String adminToken = getAdminToken();
            createAndGetRegularUserToken();
            UserEntity regularUser = userDao.findByUsername("regular_user").orElseThrow();

            String invalidPayload = String.format(
                    "{\"userId\": %d, \"role\": \"INVALID_ROLE\"}",
                    regularUser.getId()
            );

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPayload))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Validates update role request with missing fields")
        void validatesUpdateRoleRequestWithMissingFields() throws Exception {
            String adminToken = getAdminToken();
            String invalidPayload = "{\"userId\": null, \"role\": null}";

            mockMvc.perform(put("/user/update-role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPayload))
                    .andExpect(status().isBadRequest());
        }
    }

}