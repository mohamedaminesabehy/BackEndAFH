package com.afh.gescomp.controller;

import com.afh.gescomp.dto.UserSignupDTO;
import com.afh.gescomp.payload.response.AuthenticationRequest;
import com.afh.gescomp.payload.response.AuthenticationResponse;
import com.afh.gescomp.service.JwtTokenService;
import com.afh.gescomp.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthController
 * Compatible Java 7 et JUnit 4
 */
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthController authController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAuthenticateSuccess() {
        // Arrange
        Long matricule = 12345L;
        String password = "password";
        String token = "jwt.token.example";
        
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setMatricule(matricule);
        authRequest.setPassword(password);
        
        AuthenticationResponse mockResponse = new AuthenticationResponse();
        mockResponse.setAuthenticated(true);
        mockResponse.setResultat("Authentification réussie");
        
        when(userService.authenticateUser(matricule, password)).thenReturn(mockResponse);
        when(jwtTokenService.generateToken(matricule)).thenReturn(token);
        
        // Act
        AuthenticationResponse result = authController.authenticate(authRequest);
        
        // Assert
        assertTrue("L'authentification devrait être réussie", result.isAuthenticated());
        assertEquals("Le token JWT devrait être défini", token, result.getJwtToken());
        verify(userService).authenticateUser(matricule, password);
        verify(jwtTokenService).generateToken(matricule);
    }

    @Test
    public void testAuthenticateFailure() {
        // Arrange
        Long matricule = 12345L;
        String password = "wrong_password";
        
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setMatricule(matricule);
        authRequest.setPassword(password);
        
        AuthenticationResponse mockResponse = new AuthenticationResponse();
        mockResponse.setAuthenticated(false);
        mockResponse.setResultat("Authentification échouée");
        
        when(userService.authenticateUser(matricule, password)).thenReturn(mockResponse);
        
        // Act
        AuthenticationResponse result = authController.authenticate(authRequest);
        
        // Assert
        assertFalse("L'authentification devrait échouer", result.isAuthenticated());
        assertEquals("Le message d'erreur JWT devrait être défini", 
                "absence du token. Veuillez vérifier votre authentification", result.getJwtToken());
        verify(userService).authenticateUser(matricule, password);
        verify(jwtTokenService, never()).generateToken(anyLong());
    }

    @Test
    public void testSignupSuccess() {
        // Arrange
        UserSignupDTO userSignupDTO = new UserSignupDTO();
        userSignupDTO.setId(12345L);
        userSignupDTO.setNom("Doe");
        userSignupDTO.setPrenom("John");
        userSignupDTO.setPasswordHash("password");
        
        when(userService.createUser(userSignupDTO)).thenReturn("Utilisateur créé avec succès");
        
        // Act
        ResponseEntity<Map<String, String>> response = authController.signup(userSignupDTO);
        
        // Assert
        assertEquals("Le statut HTTP devrait être CREATED", HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Le message de succès devrait être correct", 
                "Utilisateur créé avec succès", response.getBody().get("message"));
        verify(userService).createUser(userSignupDTO);
    }

    @Test
    public void testSignupUserExists() {
        // Arrange
        UserSignupDTO userSignupDTO = new UserSignupDTO();
        userSignupDTO.setId(12345L);
        
        when(userService.createUser(userSignupDTO)).thenReturn("Utilisateur avec ce Matricule existe déjà");
        
        // Act
        ResponseEntity<Map<String, String>> response = authController.signup(userSignupDTO);
        
        // Assert
        assertEquals("Le statut HTTP devrait être CONFLICT", HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Le message d'erreur devrait être correct", 
                "Utilisateur avec ce Matricule existe déjà", response.getBody().get("message"));
        verify(userService).createUser(userSignupDTO);
    }

    @Test
    public void testSignupBadRequest() {
        // Arrange
        UserSignupDTO userSignupDTO = new UserSignupDTO();
        userSignupDTO.setId(12345L);
        
        when(userService.createUser(userSignupDTO)).thenReturn("Structure introuvable");
        
        // Act
        ResponseEntity<Map<String, String>> response = authController.signup(userSignupDTO);
        
        // Assert
        assertEquals("Le statut HTTP devrait être BAD_REQUEST", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Le message d'erreur devrait être correct", 
                "Structure introuvable", response.getBody().get("message"));
        verify(userService).createUser(userSignupDTO);
    }
}