package com.afh.gescomp.implementation;

import com.afh.gescomp.model.primary.Secteur;
import com.afh.gescomp.repository.primary.SecteurRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour SecteurServiceImpl
 * Compatible Java 7 et JUnit 4
 */
public class SecteurServiceImplTest {

    @Mock
    private SecteurRepository secteurRepository;

    @InjectMocks
    private SecteurServiceImpl secteurService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllSecteur() {
        // Arrange
        Secteur secteur1 = new Secteur();
        secteur1.setNumSectEco((short) 1);
        secteur1.setDesignation("Secteur A");

        Secteur secteur2 = new Secteur();
        secteur2.setNumSectEco((short) 2);
        secteur2.setDesignation("Secteur B");

        List<Secteur> secteurs = Arrays.asList(secteur1, secteur2);
        when(secteurRepository.findAll()).thenReturn(secteurs);

        // Act
        List<Secteur> result = secteurService.getAllSecteur();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Secteur A", result.get(0).getDesignation());
        assertEquals("Secteur B", result.get(1).getDesignation());
    }

    @Test
    public void testGetNumSecteurByDesignation() {
        // Arrange
        String designation = "Secteur A";
        Short expectedNumSecteur = 1;

        when(secteurRepository.findNumSectEcoByDesignation(designation)).thenReturn(expectedNumSecteur);

        // Act
        Short result = secteurService.getNumSecteurByDesignation(designation);

        // Assert
        assertEquals(expectedNumSecteur, result);
    }
}