package com.afh.gescomp.implementation;

import com.afh.gescomp.model.primary.Banque;
import com.afh.gescomp.repository.primary.BanqueRepository;
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
 * Tests unitaires pour BanqueServiceImpl
 * Compatible Java 7 et JUnit 4
 */
public class BanqueServiceImplTest {

    @Mock
    private BanqueRepository banqueRepository;

    @InjectMocks
    private BanqueServiceImpl banqueService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllBanque() {
        // Arrange
        Banque banque1 = new Banque();
        banque1.setId((short) 1);
        banque1.setDesignation("Banque A");

        Banque banque2 = new Banque();
        banque2.setId((short) 2);
        banque2.setDesignation("Banque B");

        List<Banque> banques = Arrays.asList(banque1, banque2);
        when(banqueRepository.findAll()).thenReturn(banques);

        // Act
        List<Banque> result = banqueService.getAllBanque();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Banque A", result.get(0).getDesignation());
        assertEquals("Banque B", result.get(1).getDesignation());
    }

    @Test
    public void testGetBanqueByNumBanque() {
        // Arrange
        Short numBanque = 1;
        Banque banque = new Banque();
        banque.setId(numBanque);
        banque.setDesignation("Banque A");

        when(banqueRepository.findById(numBanque)).thenReturn(banque);

        // Act
        Banque result = banqueService.getBanqueByNumBanque(numBanque);

        // Assert
        assertNotNull(result);
        assertEquals(numBanque, result.getId());
        assertEquals("Banque A", result.getDesignation());
    }
}