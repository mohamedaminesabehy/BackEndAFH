package com.afh.gescomp.config;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Initialiseur pour l'encodage UTF-8
 * Compatible avec Spring Boot 1.3.8
 * Résout les problèmes d'affichage des caractères arabes
 */
@Component
public class UTF8EncodingInitializer {

    private static final Logger logger = LoggerFactory.getLogger(UTF8EncodingInitializer.class);

    /**
     * Initialisation automatique au démarrage de l'application
     */
    @PostConstruct
    public void initializeUTF8Encoding() {
        try {
            // Forcer l'encodage par défaut du système
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            
            // Configuration des propriétés Java pour l'encodage
            System.setProperty("spring.http.encoding.charset", "UTF-8");
            System.setProperty("spring.http.encoding.enabled", "true");
            System.setProperty("spring.http.encoding.force", "true");
            
            // Configuration de l'encodage des logs
            System.setProperty("logging.charset.console", "UTF-8");
            System.setProperty("logging.charset.file", "UTF-8");
            
            System.out.println("✅ Configuration d'encodage UTF-8 appliquée avec succès");
            System.out.println("✅ Propriétés système configurées pour l'arabe");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la configuration de l'encodage UTF-8: {}", e.getMessage(), e);
            System.err.println("❌ Erreur lors de la configuration de l'encodage: " + e.getMessage());
        }
    }
} 