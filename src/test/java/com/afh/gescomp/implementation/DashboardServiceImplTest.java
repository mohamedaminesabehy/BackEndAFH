package com.afh.gescomp.implementation;

import com.afh.gescomp.repository.primary.ArticleRepository;
import com.afh.gescomp.repository.primary.FournisseurRepository;
import com.afh.gescomp.repository.primary.MarcheRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DashboardServiceImpl
 * Un seul test par méthode publique
 * Compatible Java 7 et JUnit 4
 */
public class DashboardServiceImplTest {

    @Mock
    private FournisseurRepository fournisseurRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private MarcheRepository marcheRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // ========== TEST 1: getDashboardStats ==========

    @Test
    public void testGetDashboardStats() {
        // Arrange
        String numStruct = "STRUCT001";
        when(fournisseurRepository.count()).thenReturn(100L);
        when(articleRepository.count()).thenReturn(500L);
        when(marcheRepository.countByNumStruct(numStruct)).thenReturn(50L);

        // Act
        Map<String, Object> result = dashboardService.getDashboardStats(numStruct);

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertEquals("Le nombre de fournisseurs doit être 100", Long.valueOf(100L), result.get("fournisseursCount"));
        assertEquals("Le nombre d'articles doit être 500", Long.valueOf(500L), result.get("articlesCount"));
        assertEquals("Le nombre de marchés doit être 50", Long.valueOf(50L), result.get("marchesCount"));
        assertNotNull("La date de mise à jour doit être présente", result.get("lastUpdate"));
        assertEquals("Le statut API doit être 'online'", "online", result.get("apiStatus"));

        // Verify
        verify(fournisseurRepository).count();
        verify(articleRepository).count();
        verify(marcheRepository).countByNumStruct(numStruct);
    }

    // ========== TEST 2: getMarchesEvolutionByMonth ==========



    // ========== TEST 3: getTopFournisseurs ==========

    @Test
    public void testGetTopFournisseurs() {
        // Arrange
        String numStruct = "STRUCT001";
        int limit = 5;

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> mockResults = new ArrayList<Object[]>();
        mockResults.add(new Object[]{"Fournisseur A", 10});
        mockResults.add(new Object[]{"Fournisseur B", 8});
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        Map<String, Object> result = dashboardService.getTopFournisseurs(numStruct, limit);

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertTrue("Le résultat doit contenir les labels", result.containsKey("labels"));
        assertTrue("Le résultat doit contenir les données", result.containsKey("data"));

        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");

        assertEquals("Il doit y avoir 2 fournisseurs", 2, labels.size());
        assertEquals("Il doit y avoir 2 données", 2, data.size());
        assertEquals("Le premier fournisseur doit être 'Fournisseur A'", "Fournisseur A", labels.get(0));
        assertEquals("La première donnée doit être 10", Integer.valueOf(10), data.get(0));

        // Verify
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("limit", limit);
        verify(query).setParameter("numStruct", numStruct);
    }

    // ========== TEST 4: getRecentActivities ==========

    @Test
    public void testGetRecentActivities() {
        // Arrange
        String numStruct = "STRUCT001";
        int limit = 10;

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> mockResults = new ArrayList<Object[]>();
        mockResults.add(new Object[]{"MARCHE001", "Marché Test", "2024-01-15", "MARCHE"});
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        Map<String, Object> result = dashboardService.getRecentActivities(numStruct, limit);

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertTrue("Le résultat doit contenir les activités", result.containsKey("activities"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> activities = (List<Map<String, Object>>) result.get("activities");

        assertEquals("Il doit y avoir 1 activité", 1, activities.size());
        Map<String, Object> firstActivity = activities.get(0);
        assertTrue("L'activité doit contenir un message", firstActivity.containsKey("message"));
        assertTrue("L'activité doit contenir un type", firstActivity.containsKey("type"));
        assertTrue("L'activité doit contenir un temps", firstActivity.containsKey("time"));
        assertTrue("L'activité doit contenir un utilisateur", firstActivity.containsKey("user"));

        // Verify
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("limit", limit);
        verify(query).setParameter("numStruct", numStruct);
    }

    // ========== TEST 5: getSystemStatus ==========

    @Test
    public void testGetSystemStatus() {
        // Act
        Map<String, Object> result = dashboardService.getSystemStatus();

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertEquals("Le statut API doit être 'online'", "online", result.get("apiStatus"));
        assertNotNull("La date de mise à jour doit être présente", result.get("lastUpdate"));
        assertEquals("La performance doit être 92", Integer.valueOf(92), result.get("performance"));
        assertEquals("La tendance de performance doit être 3.7", Double.valueOf(3.7), result.get("performanceTrend"));
    }

    // ========== TEST 6: getPenalitesData ==========


    // ========== TEST 7: getGarantiesData ==========

    @Test
    public void testGetGarantiesData() {
        // Arrange
        String numStruct = "STRUCT001";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(8L, BigDecimal.valueOf(50000.0));
        when(query.getResultList()).thenReturn(new ArrayList<Object[]>());

        // Act
        Map<String, Object> result = dashboardService.getGarantiesData(numStruct);

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertTrue("Le résultat doit contenir les garanties à échéance", result.containsKey("garantiesEcheance"));
        assertTrue("Le résultat doit contenir le montant total", result.containsKey("montantTotalGaranties"));
        assertTrue("Le résultat doit contenir la répartition par type", result.containsKey("repartitionParType"));
        assertTrue("Le résultat doit contenir la liste des garanties", result.containsKey("garantiesEcheanceList"));

        // Verify
        verify(entityManager, atLeastOnce()).createNativeQuery(anyString());
        verify(query, atLeastOnce()).setParameter("numStruct", numStruct);
    }

    // ========== TEST 8: getDecomptesData ==========

    @Test
    public void testGetDecomptesData() {
        // Arrange
        String numStruct = "STRUCT001";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(15L, 8L);

        // Act
        Map<String, Object> result = dashboardService.getDecomptesData(numStruct);

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertTrue("Le résultat doit contenir les décomptes en attente", result.containsKey("decomptesEnAttente"));
        assertTrue("Le résultat doit contenir l'évolution des paiements", result.containsKey("evolutionPaiements"));
        assertTrue("Le résultat doit contenir les retards de paiement", result.containsKey("retardsPaiement"));

        // Verify
        verify(entityManager, atLeastOnce()).createNativeQuery(anyString());
    }

    // ========== TEST 9: getEtapesData ==========

    @Test
    public void testGetEtapesData() {
        // Arrange
        String numStruct = "STRUCT001";

        // Act
        Map<String, Object> result = dashboardService.getEtapesData(numStruct);

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertTrue("Le résultat doit contenir les étapes en retard", result.containsKey("etapesEnRetard"));
        assertTrue("Le résultat doit contenir la progression globale", result.containsKey("progressionGlobale"));
        assertTrue("Le résultat doit contenir les alertes de délais", result.containsKey("alertesDelais"));

        assertEquals("Les étapes en retard doivent être 1", Integer.valueOf(1), result.get("etapesEnRetard"));
        assertEquals("La progression globale doit être 75.5", new BigDecimal("75.5"), result.get("progressionGlobale"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alertes = (List<Map<String, Object>>) result.get("alertesDelais");
        assertEquals("Il doit y avoir 2 alertes", 2, alertes.size());
    }

    // ========== TEST 10: getSectoriellesData ==========

    @Test
    public void testGetSectoriellesData() {
        // Arrange
        String numStruct = "STRUCT001";

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        List<Object[]> mockResults = new ArrayList<Object[]>();
        mockResults.add(new Object[]{"Secteur A", 5, 20});
        mockResults.add(new Object[]{"Secteur B", 3, 15});
        when(query.getResultList()).thenReturn(mockResults);

        // Act
        Map<String, Object> result = dashboardService.getSectoriellesData(numStruct);

        // Assert
        assertNotNull("Le résultat ne doit pas être null", result);
        assertTrue("Le résultat doit contenir la répartition par secteur", result.containsKey("repartitionParSecteur"));
        assertTrue("Le résultat doit contenir la performance par famille", result.containsKey("performanceParFamille"));
        assertTrue("Le résultat doit contenir l'évolution des prix", result.containsKey("evolutionPrix"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> repartition = (List<Map<String, Object>>) result.get("repartitionParSecteur");
        assertEquals("Il doit y avoir 2 secteurs", 2, repartition.size());

        Map<String, Object> firstSecteur = repartition.get(0);
        assertEquals("Le premier secteur doit être 'Secteur A'", "Secteur A", firstSecteur.get("designation"));
        assertEquals("Le nombre de marchés doit être 5", Integer.valueOf(5), firstSecteur.get("nombreMarches"));

        // Verify
        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter("numStruct", numStruct);
    }
}