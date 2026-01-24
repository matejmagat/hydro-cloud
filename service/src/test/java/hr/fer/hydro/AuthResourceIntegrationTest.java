package hr.fer.hydro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import hr.fer.hydro.auth.auth.dto.AuthResponse;
import hr.fer.hydro.auth.auth.dto.LoginReq;
import hr.fer.hydro.auth.auth.dto.RegisterReq;
import hr.fer.hydro.auth.auth.service.AuthService;
import hr.fer.hydro.auth.google2fa.dto.Verify2FAReq;
import hr.fer.hydro.auth.google2fa.service.GoogleAuthService;
import hr.fer.hydro.auth.persistence.entities.User2FAEntity;
import hr.fer.hydro.auth.persistence.entities.UserEntity;
import hr.fer.hydro.auth.persistence.repositories.User2FADao;
import hr.fer.hydro.auth.persistence.repositories.User2FAScratchCodeDao;
import hr.fer.hydro.auth.persistence.repositories.UserDao;
import hr.fer.hydro.config.core.UserCoreLocalThread;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication Integration Tests")
class AuthResourceIntegrationTest {

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
    private UserDao userRepository;

    @Autowired
    private User2FADao twoFactorAuthRepository;

    @Autowired
    private User2FAScratchCodeDao scratchCodeRepository;

    @Autowired
    private AuthService authenticationService;

    @Autowired
    private GoogleAuthService googleTwoFactorService;

    @Autowired
    private GoogleAuthenticator totpGenerator;

    @AfterEach
    void cleanupDatabase() {
        UserCoreLocalThread.deleteUserInfo();
        scratchCodeRepository.deleteAll();
        twoFactorAuthRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("User Registration Scenarios")
    class RegistrationTests {
        @Test
        @DisplayName("Rejects registration when username is taken")
        void rejectsRegistration_whenUsernameAlreadyExists() throws Exception {
            RegisterReq initialUser = buildRegistrationRequest(
                    "Marko", "Horvat", "mhorvat", "marko.horvat@example.hr"
            );
            authenticationService.signUp(initialUser);

            RegisterReq duplicateUser = buildRegistrationRequest(
                    "Petra", "Novak", "mhorvat", "petra.novak@example.hr"
            );

            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(duplicateUser)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Rejects registration with malformed email address")
        void rejectsRegistration_whenEmailFormatIsInvalid() throws Exception {
            RegisterReq invalidEmailRequest = buildRegistrationRequest(
                    "Ivan", "Jurić", "ijuric", "not-an-email-address"
            );

            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(invalidEmailRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rejects registration with missing required fields")
        void rejectsRegistration_whenMandatoryFieldsAreMissing() throws Exception {
            String incompletePayload = "{\"firstName\":\"Luka\",\"lastName\":\"Babić\"}";

            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(incompletePayload))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("User Authentication Scenarios")
    class AuthenticationTests {

        @Test
        @DisplayName("Authenticates user successfully with correct credentials")
        void authenticatesUser_whenCredentialsAreCorrect() throws Exception {
            RegisterReq userAccount = buildRegistrationRequest(
                    "Filip", "Tomić", "ftomic", "filip.tomic@example.hr"
            );
            authenticationService.signUp(userAccount);

            LoginReq authRequest = new LoginReq("ftomic", "SecurePass123!");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.pendingToken").doesNotExist())
                    .andExpect(jsonPath("$.is2FAEnabled").value(false));
        }

        @Test
        @DisplayName("Denies access with incorrect password")
        void deniesAccess_whenPasswordIsWrong() throws Exception {
            LoginReq wrongCredentials = new LoginReq("nonexistent_user", "WrongPass999");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(wrongCredentials)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Returns pending token when two-factor authentication is active")
        void returnsPendingToken_whenTwoFactorAuthIsEnabled() throws Exception {
            String accountUsername = "dsimunovic";
            createAccountWithActiveTwoFactor(
                    accountUsername, "djuro.simunovic@example.hr"
            );

            LoginReq authRequest = new LoginReq(accountUsername, "SecurePass123!");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").doesNotExist())
                    .andExpect(jsonPath("$.pendingToken").isNotEmpty())
                    .andExpect(jsonPath("$.is2FAEnabled").value(true));
        }

        @Test
        @DisplayName("Rejects authentication with empty credentials")
        void rejectsAuthentication_whenCredentialsAreEmpty() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Two-Factor Authentication Setup")
    class TwoFactorSetupTests {

        @Test
        @DisplayName("Generates QR code for authenticated user enabling 2FA")
        void generatesQrCode_whenUserActivatesTwoFactor() throws Exception {
            RegisterReq userAccount = buildRegistrationRequest(
                    "Maja", "Barišić", "mbarisic", "maja.barisic@example.hr"
            );
            AuthResponse authTokens = authenticationService.signUp(userAccount);

            UserEntity registeredUser = userRepository.findByUsername("mbarisic").orElseThrow();
            UserCoreLocalThread.setUserInfo(registeredUser.getId());

            mockMvc.perform(post("/google-2fa/activate")
                            .header("Authorization", "Bearer " + authTokens.accessToken())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.qrCodeBase64").isNotEmpty());
        }

    }

    @Nested
    @DisplayName("Two-Factor Code Verification")
    class TwoFactorVerificationTests {

        @Test
        @DisplayName("Completes 2FA setup with valid TOTP code")
        void completes2FASetup_whenTotpCodeIsValid() throws Exception {
            String accountUsername = "tkraljic";
            RegisterReq userAccount = buildRegistrationRequest(
                    "Tomislav", "Kraljić", accountUsername, "tomislav.kraljic@example.hr"
            );
            authenticationService.signUp(userAccount);

            UserEntity registeredUser = userRepository.findByUsername(accountUsername).orElseThrow();
            UserCoreLocalThread.setUserInfo(registeredUser.getId());

            String authToken = authenticationService.login(
                    new LoginReq(accountUsername, "SecurePass123!")
            ).accessToken();

            googleTwoFactorService.activate2FA();

            User2FAEntity twoFactorConfig = twoFactorAuthRepository.findByUser(registeredUser).orElseThrow();
            int validTotpCode = totpGenerator.getTotpPassword(twoFactorConfig.getSecret());
            Verify2FAReq verificationPayload = new Verify2FAReq(validTotpCode);

            mockMvc.perform(post("/google-2fa/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + authToken)
                            .content(jsonMapper.writeValueAsString(verificationPayload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.scratchCodes").isArray())
                    .andExpect(jsonPath("$.scratchCodes", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("Rejects 2FA setup with incorrect TOTP code")
        void rejects2FASetup_whenTotpCodeIsIncorrect() throws Exception {
            String accountUsername = "zmatic";
            RegisterReq userAccount = buildRegistrationRequest(
                    "Zvonimir", "Matić", accountUsername, "zvonimir.matic@example.hr"
            );
            authenticationService.signUp(userAccount);

            UserEntity registeredUser = userRepository.findByUsername(accountUsername).orElseThrow();
            UserCoreLocalThread.setUserInfo(registeredUser.getId());

            String authToken = authenticationService.login(
                    new LoginReq(accountUsername, "SecurePass123!")
            ).accessToken();
            googleTwoFactorService.activate2FA();

            Verify2FAReq invalidVerification = new Verify2FAReq(123456);

            mockMvc.perform(post("/google-2fa/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + authToken)
                            .content(jsonMapper.writeValueAsString(invalidVerification)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.scratchCodes").isEmpty());
        }

        @Test
        @DisplayName("Returns empty scratch codes when verification code is missing")
        void returnsEmptyScratchCodes_whenCodeIsMissing() throws Exception {
            String accountUsername = "isusic";
            RegisterReq userAccount = buildRegistrationRequest(
                    "Iva", "Sušić", accountUsername, "iva.susic@example.hr"
            );
            authenticationService.signUp(userAccount);

            UserEntity registeredUser = userRepository.findByUsername(accountUsername).orElseThrow();
            UserCoreLocalThread.setUserInfo(registeredUser.getId());

            String authToken = authenticationService.login(
                    new LoginReq(accountUsername, "SecurePass123!")
            ).accessToken();
            googleTwoFactorService.activate2FA();

            String nullCodePayload = "{\"validationCode\": null}";

            mockMvc.perform(post("/google-2fa/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + authToken)
                            .content(nullCodePayload))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.scratchCodes").isEmpty());
        }
    }

    @Nested
    @DisplayName("Two-Factor Login Verification")
    class TwoFactorLoginTests {

        @Test
        @DisplayName("Grants access after successful 2FA verification during login")
        void grantsAccess_whenLoginTwoFactorVerificationSucceeds() throws Exception {
            String accountUsername = "npetrovic";
            UserEntity securedAccount = createAccountWithActiveTwoFactor(
                    accountUsername, "nikola.petrovic@example.hr"
            );

            String temporaryToken = authenticationService.login(
                    new LoginReq(accountUsername, "SecurePass123!")
            ).pendingToken();

            User2FAEntity twoFactorConfig = twoFactorAuthRepository.findByUser(securedAccount).orElseThrow();
            int currentTotpCode = totpGenerator.getTotpPassword(twoFactorConfig.getSecret());
            Verify2FAReq verificationPayload = new Verify2FAReq(currentTotpCode);

            mockMvc.perform(post("/google-2fa/login/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + temporaryToken)
                            .content(jsonMapper.writeValueAsString(verificationPayload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.is2FAEnabled").value(true));
        }

        @Test
        @DisplayName("Denies access when 2FA verification fails during login")
        void deniesAccess_whenLoginTwoFactorVerificationFails() throws Exception {
            String accountUsername = "lsaric";
            createAccountWithActiveTwoFactor(
                    accountUsername, "lana.saric@example.hr"
            );

            String temporaryToken = authenticationService.login(
                    new LoginReq(accountUsername, "SecurePass123!")
            ).pendingToken();

            Verify2FAReq invalidVerification = new Verify2FAReq(0);

            mockMvc.perform(post("/google-2fa/login/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + temporaryToken)
                            .content(jsonMapper.writeValueAsString(invalidVerification)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").doesNotExist())
                    .andExpect(jsonPath("$.is2FAEnabled").value(true));
        }

        @Test
        @DisplayName("Handles multiple failed 2FA attempts gracefully")
        void handlesMultipleFailedAttempts_gracefully() throws Exception {
            String accountUsername = "gbabic";
            createAccountWithActiveTwoFactor(
                    accountUsername, "goran.babic@example.hr"
            );

            String temporaryToken = authenticationService.login(
                    new LoginReq(accountUsername, "SecurePass123!")
            ).pendingToken();

            // First failed attempt
            mockMvc.perform(post("/google-2fa/login/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + temporaryToken)
                            .content(jsonMapper.writeValueAsString(new Verify2FAReq(111111))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").doesNotExist());

            // Second failed attempt
            mockMvc.perform(post("/google-2fa/login/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + temporaryToken)
                            .content(jsonMapper.writeValueAsString(new Verify2FAReq(222222))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Two-Factor Deactivation")
    class TwoFactorDeactivationTests {

        @Test
        @DisplayName("Successfully disables 2FA for authenticated user")
        void disables2FA_whenUserIsAuthenticated() throws Exception {
            String accountUsername = "mnovak";
            RegisterReq userAccount = buildRegistrationRequest(
                    "Mirna", "Novak", accountUsername, "mirna.novak@example.hr"
            );
            AuthResponse authTokens = authenticationService.signUp(userAccount);

            UserEntity registeredUser = userRepository.findByUsername(accountUsername).orElseThrow();
            UserCoreLocalThread.setUserInfo(registeredUser.getId());

            googleTwoFactorService.activate2FA();
            User2FAEntity twoFactorConfig = twoFactorAuthRepository.findByUser(registeredUser).orElseThrow();
            int validTotpCode = totpGenerator.getTotpPassword(twoFactorConfig.getSecret());
            googleTwoFactorService.verify2FA(new Verify2FAReq(validTotpCode));

            mockMvc.perform(put("/google-2fa/disable")
                            .header("Authorization", "Bearer " + authTokens.accessToken())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            UserEntity updatedUser = userRepository.findByUsername(accountUsername).orElseThrow();
            assertThat(updatedUser.getIs2FAEnabled()).isFalse();
        }

        @Test
        @DisplayName("Allows user to re-enable 2FA after disabling it")
        void allowsReEnabling2FA_afterDisabling() throws Exception {
            String accountUsername = "bknezevic";
            RegisterReq userAccount = buildRegistrationRequest(
                    "Boris", "Knežević", accountUsername, "boris.knezevic@example.hr"
            );
            AuthResponse authTokens = authenticationService.signUp(userAccount);

            UserEntity registeredUser = userRepository.findByUsername(accountUsername).orElseThrow();
            UserCoreLocalThread.setUserInfo(registeredUser.getId());

            // First activation
            googleTwoFactorService.activate2FA();
            User2FAEntity firstConfig = twoFactorAuthRepository.findByUser(registeredUser).orElseThrow();
            int firstCode = totpGenerator.getTotpPassword(firstConfig.getSecret());
            googleTwoFactorService.verify2FA(new Verify2FAReq(firstCode));

            // Disable
            mockMvc.perform(put("/google-2fa/disable")
                            .header("Authorization", "Bearer " + authTokens.accessToken())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Re-enable
            mockMvc.perform(post("/google-2fa/activate")
                            .header("Authorization", "Bearer " + authTokens.accessToken())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.qrCodeBase64").isNotEmpty());
        }
    }

    // Helper methods

    private RegisterReq buildRegistrationRequest(String firstName, String lastName,
                                                 String username, String email) {
        return new RegisterReq(firstName, lastName, username, email, "SecurePass123!");
    }

    private UserEntity createAccountWithActiveTwoFactor(String username, String email) {
        String capitalizedName = username.substring(0, 1).toUpperCase() + username.substring(1);
        RegisterReq userAccount = buildRegistrationRequest(capitalizedName, "Testni", username, email);
        authenticationService.signUp(userAccount);

        UserEntity registeredUser = userRepository.findByUsername(username).orElseThrow();
        UserCoreLocalThread.setUserInfo(registeredUser.getId());

        googleTwoFactorService.activate2FA();
        User2FAEntity twoFactorConfig = twoFactorAuthRepository.findByUser(registeredUser).orElseThrow();
        int validTotpCode = totpGenerator.getTotpPassword(twoFactorConfig.getSecret());
        googleTwoFactorService.verify2FA(new Verify2FAReq(validTotpCode));

        UserCoreLocalThread.deleteUserInfo();

        return registeredUser;
    }
}