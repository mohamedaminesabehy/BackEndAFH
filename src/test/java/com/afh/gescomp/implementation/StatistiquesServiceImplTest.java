package com.afh.gescomp.implementation;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour StatistiquesServiceImpl
 * Compatible Java 7 et JUnit 4
 */
public class StatistiquesServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private StatistiquesServiceImpl statistiquesService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    // ========== TEST 2: getRegionsRepartitionByDates ==========

    @Test
    public void testGetRegionsRepartitionByDates() {
        // Arrange
        String numStruct = "01";
        String dateDebut = "2024-01-01";
        String dateFin = "2024-12-31";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> rows = new ArrayList<Object[]>();
        rows.add(new Object[]{"Tunis", 5});
        rows.add(new Object[]{"Sfax", 3});
        when(query.getResultList()).thenReturn(rows);

        // Act
        Map<String, Object> result = statistiquesService.getRegionsRepartitionByDates(numStruct, dateDebut, dateFin);

        // Assert
        assertNotNull(result);
        assertEquals(Arrays.asList("Tunis", "Sfax"), result.get("labels"));
        assertEquals(Arrays.asList(5, 3), result.get("data"));

        // Verify
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("dateDebut", dateDebut);
        verify(query).setParameter("dateFin", dateFin);
        verify(query).setParameter("numStruct", numStruct);
    }

    // ========== TEST 3: getFournisseursRepartitionByDates ==========

    @Test
    public void testGetFournisseursRepartitionByDates() {
        // Arrange
        String numStruct = "01";
        int limit = 5;
        String dateDebut = "2024-01-01";
        String dateFin = "2024-12-31";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> rows = new ArrayList<Object[]>();
        rows.add(new Object[]{"Fournisseur A", 7});
        rows.add(new Object[]{"Fournisseur B", 2});
        when(query.getResultList()).thenReturn(rows);

        // Act
        Map<String, Object> result = statistiquesService.getFournisseursRepartitionByDates(numStruct, limit, dateDebut, dateFin);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("labels"));
        assertTrue(result.containsKey("data"));
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(2, labels.size());
        assertEquals(2, data.size());
        assertEquals("Fournisseur A", labels.get(0));
        assertEquals(Integer.valueOf(7), data.get(0));

        // Verify
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("dateDebut", dateDebut);
        verify(query).setParameter("dateFin", dateFin);
        verify(query).setParameter("numStruct", numStruct);
    }

    // ========== TEST 4: getArticlesRepartitionByDates ==========

    @Test
    public void testGetArticlesRepartitionByDates() {
        // Arrange
        String numStruct = "01";
        String dateDebut = "2024-01-01";
        String dateFin = "2024-12-31";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> rows = new ArrayList<Object[]>();
        rows.add(new Object[]{"Article X", 9});
        rows.add(new Object[]{"Article Y", 4});
        when(query.getResultList()).thenReturn(rows);

        // Act
        Map<String, Object> result = statistiquesService.getArticlesRepartitionByDates(numStruct, dateDebut, dateFin);

        // Assert
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(2, labels.size());
        assertEquals(2, data.size());
        assertEquals("Article X", labels.get(0));
        assertEquals(Integer.valueOf(9), data.get(0));

        // Verify
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("dateDebut", dateDebut);
        verify(query).setParameter("dateFin", dateFin);
        verify(query).setParameter("numStruct", numStruct);
    }

    // ========== TEST 5: getMarchesDetailles ==========

    @Test
    public void testGetMarchesDetailles() {
        // Arrange
        String numStruct = "01";
        int page = 1;
        int size = 2;
        String filterName = "TEST";
        Double filterMinAmount = 100.0;

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);

        List<Object[]> rows = new ArrayList<Object[]>();
        rows.add(new Object[]{1L, "Marche A", 1000.0, "2024-05-01", "F001", "FOUR A", "BANQUE X"});
        when(query.getResultList()).thenReturn(rows);

        // Act
        Map<String, Object> result = statistiquesService.getMarchesDetailles(numStruct, page, size, filterName, filterMinAmount);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("marches"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> marches = (List<Map<String, Object>>) result.get("marches");
        assertEquals(1, marches.size());
        assertEquals("Marche A", marches.get(0).get("designation"));
        assertEquals("FOUR A", marches.get(0).get("fournisseur"));

        // Verify
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("numStruct", numStruct); // Named parameter
        verify(query).setParameter("filterName", "%" + filterName + "%"); // Named parameter
        verify(query).setParameter("filterMinAmount", filterMinAmount); // Named parameter

        // Calculate expected paramIndex for offset and size based on conditions in service method
        int expectedParamIndex = 1;
        if (numStruct != null && !numStruct.isEmpty()) {
            expectedParamIndex++;
        }
        if (filterName != null && !filterName.isEmpty()) {
            expectedParamIndex++;
        }
        if (filterMinAmount != null && filterMinAmount > 0) {
            expectedParamIndex++;
        }
        verify(query).setParameter("offset", page * size);
        verify(query).setParameter("size", size);
    }



    // ========== TEST 7: getFournisseurComplet ==========


    // ========== TEST 9: getMarchesEvolutionByDates ==========

    @Test
    public void testGetMarchesEvolutionByDates() {
        // Arrange
        String numStruct = "01";
        String dateDebut = "2024-07-01";
        String dateFin = "2024-08-31";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> rows = new ArrayList<Object[]>();
        rows.add(new Object[]{"2024-07", 5});
        rows.add(new Object[]{"2024-08", 8});
        when(query.getResultList()).thenReturn(rows);
        when(query.getSingleResult()).thenReturn(13L, 4.0); // activeCount, avgDelayDays

        // Act
        Map<String, Object> result = statistiquesService.getMarchesEvolutionByDates(numStruct, dateDebut, dateFin);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("labels"));
        assertTrue(result.containsKey("data"));
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(2, labels.size());
        assertEquals(2, data.size());
        assertEquals(Integer.valueOf(5), data.get(0));
        assertEquals(13L, result.get("activeCount"));
        assertEquals(4.0, (Double) result.get("avgDelayDays"), 0.001);

        // Verify
        verify(entityManager, atLeastOnce()).createNativeQuery(anyString());
        verify(query, atLeastOnce()).setParameter("dateDebut", dateDebut);
        verify(query, atLeastOnce()).setParameter("dateFin", dateFin);
        verify(query, atLeastOnce()).setParameter("numStruct", numStruct);
    }

    // ========== TEST 10: getArticlesPlusDemandes (non-paginated) ==========

    public Map<String, Object> getArticlesPlusDemandes(String numStruct, String filterSecteur, String filterFamille,
                                                       String filterStatut, Double filterTvaMin, Double filterTvaMax) {
        Map<String, Object> result = new HashMap<>();
        try {
            StringBuilder sql = new StringBuilder("SELECT a.DESIGNATION, a.SECTEUR, COUNT(*) as utilisations, " +
                    "COALESCE(SUM(lm.QUANTITE), 0) as quantite, a.UNITE_MESURE, a.TVA, a.FAMILLE, a.STATUT " +
                    "FROM ACHAT.ARTICLE a JOIN ACHAT.LIGNE_MARCHE lm ON a.NUM_ARTICLE = lm.NUM_ARTICLE " +
                    "JOIN ACHAT.MARCHE m ON lm.NUM_MARCHE = m.NUM_MARCHE WHERE 1=1");
            if (numStruct != null && !numStruct.trim().isEmpty()) {
                sql.append(" AND m.NUM_STRUCT = :numStruct");
            }
            if (filterSecteur != null && !filterSecteur.trim().isEmpty()) {
                sql.append(" AND UPPER(a.SECTEUR) LIKE UPPER(:filterSecteur)");
            }
            if (filterFamille != null && !filterFamille.trim().isEmpty()) {
                sql.append(" AND UPPER(a.FAMILLE) LIKE UPPER(:filterFamille)");
            }
            if (filterTvaMin != null && filterTvaMin >= 0) {
                sql.append(" AND a.TVA >= :filterTvaMin");
            }
            if (filterTvaMax != null && filterTvaMax >= 0) {
                sql.append(" AND a.TVA <= :filterTvaMax");
            }
            sql.append(" GROUP BY a.DESIGNATION, a.SECTEUR, a.UNITE_MESURE, a.TVA, a.FAMILLE, a.STATUT " +
                    "ORDER BY utilisations DESC");

            Query query = entityManager.createNativeQuery(sql.toString());
            if (numStruct != null && !numStruct.trim().isEmpty()) {
                query.setParameter("numStruct", numStruct);
            }
            if (filterSecteur != null && !filterSecteur.trim().isEmpty()) {
                query.setParameter("filterSecteur", "%" + filterSecteur + "%");
            }
            if (filterFamille != null && !filterFamille.trim().isEmpty()) {
                query.setParameter("filterFamille", "%" + filterFamille + "%");
            }
            if (filterTvaMin != null && filterTvaMin >= 0) {
                query.setParameter("filterTvaMin", filterTvaMin);
            }
            if (filterTvaMax != null && filterTvaMax >= 0) {
                query.setParameter("filterTvaMax", filterTvaMax);
            }

            List<Object[]> rows = query.getResultList();
            List<Map<String, Object>> articles = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> article = new HashMap<>();
                article.put("designation", row[0]);
                article.put("secteur", row[1]);
                article.put("utilisations", ((Number) row[2]).intValue());
                article.put("quantite", ((Number) row[3]).doubleValue());
                article.put("uniteMesure", row[4]);
                article.put("tva", ((Number) row[5]).doubleValue());
                article.put("famille", row[6]);
                article.put("statut", row[7]);
                articles.add(article);
            }

            result.put("status", "success");
            result.put("articles", articles);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Erreur lors de la récupération des articles: " + e.getMessage());
        }
        return result;
    }

    // ========== TEST 11: getFournisseursAvecMarches with filters ==========


    // ========== TEST 12: getArticlesBySecteur ==========

    @Test
    public void testGetArticlesBySecteur() {
        // Arrange
        String numStruct = "01";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> rows = new ArrayList<Object[]>();
        rows.add(new Object[]{"Energie", 25, 40.5, 120000.0});
        rows.add(new Object[]{"Eau", 10, 16.2, 45000.0});
        when(query.getResultList()).thenReturn(rows);

        // Act
        Map<String, Object> result = statistiquesService.getArticlesBySecteur(numStruct);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("articlesBySecteur"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("articlesBySecteur");
        assertEquals(2, list.size());
        Map<String, Object> first = list.get(0);
        assertEquals("Energie", first.get("secteur"));
        assertEquals(25, first.get("nombreArticles"));
        assertEquals(40.5, (Double) first.get("pourcentage"), 0.001);
        assertEquals(120000.0, (Double) first.get("montantTotal"), 0.001);

        // Verify
        verify(entityManager).createNativeQuery(anyString());
    }

    // ========== TEST 13: exportToExcel (by period) ==========

    @Test
    public void testExportToExcel_ByPeriod() throws Exception {
        // Arrange
        String numStruct = "01";
        String type = "marches";
        String dateDebut = "2024-01-01";
        String dateFin = "2024-12-31";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query); // Update to handle named parameters
        when(query.setParameter(anyInt(), any())).thenReturn(query); // Keep for offset/size if used

        List<Object[]> rows = new ArrayList<>();
        java.util.Date date = new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2024-05-10");
        rows.add(new Object[]{123L, "Marché Test", "Fourn A", 9999.99, new java.sql.Date(date.getTime()), "Banque X"});
        when(query.getResultList()).thenReturn(rows);

        // Act
        byte[] excelBytes = statistiquesService.exportToExcel(numStruct, type, dateDebut, dateFin);

        // Assert basic validity
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        // Parse the workbook to validate content
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(excelBytes);
        org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(bais);
        try {
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = wb.getSheet("Statistiques Marchés");
            assertNotNull(sheet);

            // Header row (index 4 per implementation)
            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(4);
            assertNotNull(headerRow);
            assertEquals("Numéro Marché", headerRow.getCell(0).getStringCellValue());
            assertEquals("Désignation", headerRow.getCell(1).getStringCellValue());
            assertEquals("Fournisseur", headerRow.getCell(2).getStringCellValue());
            assertEquals("Montant (TND)", headerRow.getCell(3).getStringCellValue());
            assertEquals("Date Marché", headerRow.getCell(4).getStringCellValue());
            assertEquals("Banque", headerRow.getCell(5).getStringCellValue());

            // First data row starts at index 5
            org.apache.poi.ss.usermodel.Row dataRow = sheet.getRow(5);
            assertNotNull(dataRow);
            assertEquals("123", dataRow.getCell(0).getStringCellValue());
            assertEquals("Marché Test", dataRow.getCell(1).getStringCellValue());
            assertEquals("Fourn A", dataRow.getCell(2).getStringCellValue());
            assertEquals(9999.99, dataRow.getCell(3).getNumericCellValue(), 0.001);
            assertTrue(dataRow.getCell(4).getStringCellValue().length() > 0);
            assertEquals("Banque X", dataRow.getCell(5).getStringCellValue());
        } finally {
            wb.close();
            bais.close();
        }

        // Verify
        verify(entityManager, atLeastOnce()).createNativeQuery(anyString());
        if (numStruct != null && !numStruct.trim().isEmpty()) {
            verify(query, atLeastOnce()).setParameter("numStruct", numStruct);
        }
        if (dateDebut != null && !dateDebut.trim().isEmpty()) {
            verify(query, atLeastOnce()).setParameter("dateDebut", dateDebut); // Verify named parameter
        }
        if (dateFin != null && !dateFin.trim().isEmpty()) {
            verify(query, atLeastOnce()).setParameter("dateFin", dateFin); // Verify named parameter
        }
    }
}
