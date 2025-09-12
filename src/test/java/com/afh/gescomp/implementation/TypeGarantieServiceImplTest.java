package com.afh.gescomp.implementation;

import com.afh.gescomp.model.primary.TypeGarantie;
import com.afh.gescomp.repository.primary.TypeGarantieRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import org.mockito.Spy;

/**
 * Tests unitaires pour TypeGarantieServiceImpl
 * Compatible Java 7 et JUnit 4
 */
public class TypeGarantieServiceImplTest {

    @Mock
    private TypeGarantieRepository typeGarantieRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TypeGarantieServiceImpl typeGarantieService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        typeGarantieService = spy(typeGarantieService);
    }

    @Test
    public void testFindAllByOrderByIdAsc() {
        // Arrange
        TypeGarantie garantie1 = new TypeGarantie();
        garantie1.setId(1L);
        garantie1.setDesignation("Garantie A");

        TypeGarantie garantie2 = new TypeGarantie();
        garantie2.setId(2L);
        garantie2.setDesignation("Garantie B");

        List<TypeGarantie> garanties = Arrays.asList(garantie1, garantie2);
        when(typeGarantieRepository.findAllByOrderByIdAsc()).thenReturn(garanties);

        // Act
        List<TypeGarantie> result = typeGarantieService.findAllByOrderByIdAsc();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Garantie A", result.get(0).getDesignation());
        assertEquals("Garantie B", result.get(1).getDesignation());
    }

    @Test
    public void testSave_WithEmptyTable() {
        // Arrange
        TypeGarantie garantie = new TypeGarantie();
        garantie.setId(1L);
        garantie.setDesignation("Nouvelle Garantie");

        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRM_TYPE_GARANTIE", Long.class)).thenReturn(0L);
        when(typeGarantieRepository.save(garantie)).thenReturn(garantie);

        // Act
        typeGarantieService.save(garantie);

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM PRM_TYPE_GARANTIE", Long.class);
        verify(typeGarantieService, times(1)).resetSequence();
        verify(typeGarantieRepository).save(garantie);
    }

    @Test
    public void testSave_WithNonEmptyTable() {
        // Arrange
        TypeGarantie garantie = new TypeGarantie();
        garantie.setId(1L);
        garantie.setDesignation("Nouvelle Garantie");

        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRM_TYPE_GARANTIE", Long.class)).thenReturn(5L);
        when(typeGarantieRepository.save(garantie)).thenReturn(garantie);

        // Act
        typeGarantieService.save(garantie);

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM PRM_TYPE_GARANTIE", Long.class);
        verify(typeGarantieService, never()).resetSequence();
        verify(typeGarantieRepository).save(garantie);
    }

    @Test
    public void testFindById() {
        // Arrange
        Long id = 1L;
        TypeGarantie garantie = new TypeGarantie();
        garantie.setId(id);
        garantie.setDesignation("Garantie Test");

        when(typeGarantieRepository.findOne(id)).thenReturn(garantie);

        // Act
        TypeGarantie result = typeGarantieService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Garantie Test", result.getDesignation());
    }

    @Test
    public void testDeleteTypeGarantie() {
        // Arrange
        TypeGarantie garantie = new TypeGarantie();
        garantie.setId(1L);
        garantie.setDesignation("Garantie à supprimer");

        doNothing().when(typeGarantieRepository).delete(garantie);

        // Act
        typeGarantieService.deleteTypeGarantie(garantie);

        // Assert
        verify(typeGarantieRepository).delete(garantie);
    }

    @Test
    public void testResetSequence_WithExistingRecords() {
        // Arrange
        when(jdbcTemplate.queryForObject("SELECT COALESCE(MAX(ID_TYPE_GARANTIE), 0) FROM PRM_TYPE_GARANTIE", Long.class)).thenReturn(5L);
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        typeGarantieService.resetSequence();

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COALESCE(MAX(ID_TYPE_GARANTIE), 0) FROM PRM_TYPE_GARANTIE", Long.class);
        verify(jdbcTemplate).execute("ALTER SEQUENCE TYPE_GARANTIE_SEQ RESTART START WITH 6");
    }

    @Test
    public void testResetSequence_WithNoRecords() {
        // Arrange
        when(jdbcTemplate.queryForObject("SELECT COALESCE(MAX(ID_TYPE_GARANTIE), 0) FROM PRM_TYPE_GARANTIE", Long.class)).thenReturn(0L);
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        typeGarantieService.resetSequence();

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COALESCE(MAX(ID_TYPE_GARANTIE), 0) FROM PRM_TYPE_GARANTIE", Long.class);
        verify(jdbcTemplate).execute("ALTER SEQUENCE TYPE_GARANTIE_SEQ RESTART START WITH 1");
    }
}