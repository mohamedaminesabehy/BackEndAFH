package com.afh.gescomp.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests unitaires pour JwtTokenService
 * Compatible Java 7 et JUnit 4
 */
public class JwtTokenServiceTest {

    @InjectMocks
    private JwtTokenService jwtTokenService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateToken() {
        // Arrange
        Long matricule = 12345L;

        // Act
        String token = jwtTokenService.generateToken(matricule);

        // Assert
        assertNotNull("Le token généré ne devrait pas être null", token);
        assertTrue("Le token devrait être une chaîne non vide", token.length() > 0);
    }

    @Test
    public void testExtractMatricule() {
        // Arrange
        Long expectedMatricule = 12345L;
        String token = jwtTokenService.generateToken(expectedMatricule);

        // Act
        Long extractedMatricule = jwtTokenService.extractMatricule(token);

        // Assert
        assertEquals("Le matricule extrait devrait correspondre au matricule utilisé pour générer le token", 
                expectedMatricule, extractedMatricule);
    }

    @Test
    public void testIsTokenValid() {
        // Arrange
        Long matricule = 12345L;
        String token = jwtTokenService.generateToken(matricule);

        // Act
        boolean isValid = jwtTokenService.isTokenValid(token, matricule);

        // Assert
        assertTrue("Le token devrait être valide pour le matricule utilisé pour le générer", isValid);
    }

    @Test
    public void testIsTokenValidWithWrongMatricule() {
        // Arrange
        Long correctMatricule = 12345L;
        Long wrongMatricule = 54321L;
        String token = jwtTokenService.generateToken(correctMatricule);

        // Act
        boolean isValid = jwtTokenService.isTokenValid(token, wrongMatricule);

        // Assert
        assertFalse("Le token ne devrait pas être valide pour un matricule différent", isValid);
    }

    @Test
    public void testIsTokenExpired() throws Exception {
        // Arrange - Créer un token avec une très courte durée de vie (par exemple, 100 ms)
        Long matricule = 12345L;
        String token = jwtTokenService.generateTokenWithShortExpiration(matricule, 100L); // 100 millisecondes

        // Attendre que le token expire
        Thread.sleep(150); // Attendre un peu plus que la durée de vie du token

        // Act
        boolean isExpired = jwtTokenService.isTokenExpired(token);

        // Assert
        assertTrue("Le token devrait être considéré comme expiré après un court délai", isExpired);
    }
}