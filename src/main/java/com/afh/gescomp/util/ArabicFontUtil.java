package com.afh.gescomp.util;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.FontFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.annotation.PostConstruct; // Compatible Java 1.7
import java.io.ByteArrayOutputStream;

/**
 * Utilitaire pour gérer les polices arabes dans les PDFs
 * Amélioré pour utiliser les polices TTF arabes disponibles
 */
@Component
public class ArabicFontUtil {

    private static final Logger LOGGER = Logger.getLogger(ArabicFontUtil.class.getName());
    
    // Cache des polices pour éviter de les recharger
    private static final Map<String, Font> fontCache = new HashMap<>();
    
    // Polices TTF arabes disponibles
    private static final String[] ARABIC_TTF_FONTS = {
        "Amiri-Regular.ttf",
        "NotoNaskhArabic-Regular.ttf"
    };
    
    // Polices système par défaut pour l'arabe (fallback)
    private static final String[] ARABIC_SYSTEM_FONTS = {
        "Arial Unicode MS",
        "Tahoma",
        "Microsoft Uighur",
        "Segoe UI",
        "Arial",
        "Times New Roman"
    };
    
    // Police par défaut pour le texte non-arabe
    private static final String DEFAULT_FONT = "Arial";
    
    private BaseFont arabicBaseFont;
    private BaseFont latinBaseFont;
    private BaseFont notoArabicBaseFont;
    
    @PostConstruct
    public void initializeFonts() {
        try {
            LOGGER.info("Initialisation des polices arabes TTF...");
            
            // Essayer d'abord Amiri (police principale)
            InputStream amiriStream = getClass().getResourceAsStream("/fonts/arabic/Amiri-Regular.ttf");
            if (amiriStream != null) {
                byte[] amiriFontBytes = toByteArray(amiriStream);
                arabicBaseFont = BaseFont.createFont("Amiri-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, amiriFontBytes, null);
                amiriStream.close();
                LOGGER.info("✓ Police Amiri chargée avec succès");
            } else {
                LOGGER.warning("⚠ Police Amiri non trouvée, utilisation du fallback");
            }
            
            // Essayer Noto Naskh Arabic (police secondaire)
            InputStream notoStream = getClass().getResourceAsStream("/fonts/arabic/NotoNaskhArabic-Regular.ttf");
            if (notoStream != null) {
                byte[] notoFontBytes = toByteArray(notoStream);
                notoArabicBaseFont = BaseFont.createFont("NotoNaskhArabic-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, notoFontBytes, null);
                notoStream.close();
                LOGGER.info("✓ Police Noto Naskh Arabic chargée avec succès");
            } else {
                LOGGER.warning("⚠ Police Noto Naskh Arabic non trouvée");
            }
            
            // Police latine standard (Helvetica)
            latinBaseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
            LOGGER.info("✓ Police latine chargée avec succès");
            
            // Si aucune police arabe TTF n'est trouvée, essayer les polices système
            if (arabicBaseFont == null && notoArabicBaseFont == null) {
                LOGGER.info("Tentative de chargement des polices système arabes...");
                for (String fontName : ARABIC_SYSTEM_FONTS) {
                    try {
                        if (isSystemFontAvailable(fontName)) {
                            arabicBaseFont = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                            LOGGER.info("✓ Police système arabe chargée: " + fontName);
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.FINE, "Police système non disponible: " + fontName, e);
                    }
                }
            }
            
            // Fallback final : utiliser BaseFont.IDENTITY_H
            if (arabicBaseFont == null && notoArabicBaseFont == null) {
                LOGGER.warning("⚠ Aucune police arabe trouvée, utilisation du fallback BaseFont.IDENTITY_H");
                arabicBaseFont = BaseFont.createFont(BaseFont.IDENTITY_H, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur lors de l'initialisation des polices: " + e.getMessage(), e);
        }
    }
    
    /**
     * Méthode publique pour réinitialiser les polices si nécessaire
     */
    public void reinitializeFonts() {
        initializeFonts();
    }
    
    /**
     * Obtient la police appropriée selon le contenu du texte
     * @param text Le texte à analyser
     * @param size La taille de la police
     * @param style Le style de la police
     * @return La police appropriée
     */
    public Font getAppropriateFont(String text, float size, int style) {
        if (containsArabicText(text)) {
            return getArabicFont(size, style);
        } else {
            return getLatinFont(size, style);
        }
    }
    
    /**
     * Obtient une police appropriée pour le texte donné (taille et style par défaut)
     * @param text Le texte à analyser
     * @return La police appropriée
     */
    public Font getAppropriateFont(String text) {
        return getAppropriateFont(text, 12, Font.NORMAL);
    }
    
    /**
     * Vérifie si le texte contient des caractères arabes
     * @param text Le texte à vérifier
     * @return true si le texte contient des caractères arabes
     */
    public boolean containsArabicText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Plages de caractères arabes étendues
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC_SUPPLEMENT ||
                Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC_EXTENDED_A) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtient une police arabe optimisée
     * @param size La taille de la police
     * @param style Le style de la police
     * @return La police arabe
     */
    private Font getArabicFont(float size, int style) {
        String cacheKey = "arabic_" + size + "_" + style;
        
        if (fontCache.containsKey(cacheKey)) {
            return fontCache.get(cacheKey);
        }
        
        try {
            // Essayer d'abord Amiri (police principale)
            if (arabicBaseFont != null) {
                Font arabicFont = new Font(arabicBaseFont, size, style);
                fontCache.put(cacheKey, arabicFont);
                return arabicFont;
            }
            
            // Fallback vers Noto Naskh Arabic
            if (notoArabicBaseFont != null) {
                Font notoFont = new Font(notoArabicBaseFont, size, style);
                fontCache.put(cacheKey, notoFont);
                return notoFont;
            }
            
            // Fallback vers une police système arabe
            Font systemArabicFont = getSystemArabicFont(size, style);
            if (systemArabicFont != null) {
                fontCache.put(cacheKey, systemArabicFont);
                return systemArabicFont;
            }
            
            // Fallback final : utiliser BaseFont.IDENTITY_H
            Font fallbackFont = FontFactory.getFont(BaseFont.IDENTITY_H, size, style);
            fontCache.put(cacheKey, fallbackFont);
            return fallbackFont;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du chargement de la police arabe: " + e.getMessage(), e);
            // Fallback final : police par défaut
            Font defaultFont = getDefaultFont(size, style);
            fontCache.put(cacheKey, defaultFont);
            return defaultFont;
        }
    }
    
    /**
     * Obtient une police latine
     * @param size La taille de la police
     * @param style Le style de la police
     * @return La police latine
     */
    private Font getLatinFont(float size, int style) {
        String cacheKey = "latin_" + size + "_" + style;
        
        if (fontCache.containsKey(cacheKey)) {
            return fontCache.get(cacheKey);
        }
        
        try {
            Font latinFont = new Font(latinBaseFont, size, style);
            fontCache.put(cacheKey, latinFont);
            return latinFont;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du chargement de la police latine: " + e.getMessage(), e);
            Font defaultFont = getDefaultFont(size, style);
            fontCache.put(cacheKey, defaultFont);
            return defaultFont;
        }
    }
    
    /**
     * Obtient une police arabe système (fallback)
     * @param size La taille de la police
     * @param style Le style de la police
     * @return La police arabe système ou null si aucune n'est trouvée
     */
    private Font getSystemArabicFont(float size, int style) {
        for (String fontName : ARABIC_SYSTEM_FONTS) {
            try {
                // Vérifier si la police système existe
                if (isSystemFontAvailable(fontName)) {
                    Font font = FontFactory.getFont(fontName, size, style);
                    if (font != null) {
                        return font;
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Police système non disponible: " + fontName, e);
            }
        }
        return null;
    }
    
    /**
     * Vérifie si une police système est disponible
     * @param fontName Le nom de la police
     * @return true si la police est disponible
     */
    private boolean isSystemFontAvailable(String fontName) {
        try {
            // Utiliser le nom complet de la classe pour éviter le conflit
            java.awt.Font font = new java.awt.Font(fontName, java.awt.Font.PLAIN, 12);
            return font.getFamily().equals(fontName);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtient la police par défaut
     * @param size La taille de la police
     * @param style Le style de la police
     * @return La police par défaut
     */
    private Font getDefaultFont(float size, int style) {
        String cacheKey = "default_" + size + "_" + style;
        
        if (fontCache.containsKey(cacheKey)) {
            return fontCache.get(cacheKey);
        }
        
        try {
            Font font = FontFactory.getFont(DEFAULT_FONT, size, style);
            fontCache.put(cacheKey, font);
            return font;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du chargement de la police par défaut: " + e.getMessage(), e);
            // Fallback : police système
            Font systemFont = FontFactory.getFont(BaseFont.HELVETICA, size, style);
            fontCache.put(cacheKey, systemFont);
            return systemFont;
        }
    }
    
    /**
     * Nettoie le cache des polices
     */
    public void clearFontCache() {
        fontCache.clear();
    }
    
    /**
     * Obtient la taille du cache des polices
     * @return La taille du cache
     */
    public int getFontCacheSize() {
        return fontCache.size();
    }
    
    /**
     * Nettoie le texte arabe en supprimant les caractères spéciaux
     * @param text Le texte à nettoyer
     * @return Le texte nettoyé
     */
    public static String cleanArabicText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // Supprimer les caractères spéciaux et normaliser
        return text.replaceAll("[^\\p{L}\\p{N}\\s\\-_\\.]", "").trim();
    }
    
    /**
     * Formate le texte arabe pour un meilleur affichage
     * @param text Le texte à formater
     * @return Le texte formaté
     */
    public static String formatArabicText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // Nettoyer le texte
        String cleanedText = cleanArabicText(text);
        
        // Normaliser les espaces
        cleanedText = cleanedText.replaceAll("\\s+", " ").trim();
        
        return cleanedText;
    }
    
    /**
     * Inverse l'ordre des mots pour l'affichage arabe
     * @param text Le texte à inverser
     * @return Le texte avec l'ordre des mots inversé
     */
    public static String reverseArabicWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        String[] words = text.split("\\s+");
        StringBuilder reversed = new StringBuilder();
        
        for (int i = words.length - 1; i >= 0; i--) {
            reversed.append(words[i]);
            if (i > 0) {
                reversed.append(" ");
            }
        }
        
        return reversed.toString();
    }

    // Méthode helper compatible Java 7 pour convertir InputStream en byte[]
    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        
        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } finally {
            buffer.close();
        }
        
        return buffer.toByteArray();
    }
} 