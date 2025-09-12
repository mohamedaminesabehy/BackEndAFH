package com.afh.gescomp.implementation;

import com.afh.gescomp.model.primary.TypePenalite;
import com.afh.gescomp.repository.primary.TypePenaliteRepository;
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
 * Tests unitaires pour TypePenaliteServiceImpl
 * Compatible Java 7 et JUnit 4
 */
public class TypePenaliteServiceImplTest {

    @Mock
    private TypePenaliteRepository typePenaliteRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TypePenaliteServiceImpl typePenaliteService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        typePenaliteService = spy(typePenaliteService);
    }

    @Test
    public void testFindAllByOrderByIdAsc() {
        // Arrange
        TypePenalite penalite1 = new TypePenalite();
        penalite1.setId(1L);
        penalite1.setDesignation("Pénalité A");

        TypePenalite penalite2 = new TypePenalite();
        penalite2.setId(2L);
        penalite2.setDesignation("Pénalité B");

        List<TypePenalite> penalites = Arrays.asList(penalite1, penalite2);
        when(typePenaliteRepository.findAllByOrderByIdAsc()).thenReturn(penalites);

        // Act
        List<TypePenalite> result = typePenaliteService.findAllByOrderByIdAsc();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Pénalité A", result.get(0).getDesignation());
        assertEquals("Pénalité B", result.get(1).getDesignation());
    }

    @Test
    public void testSave_WithEmptyTable() {
        // Arrange
        TypePenalite penalite = new TypePenalite();
        penalite.setId(1L);
        penalite.setDesignation("Nouvelle Pénalité");

        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRM_TYPE_PENALITE", Long.class)).thenReturn(0L);
        when(typePenaliteRepository.save(penalite)).thenReturn(penalite);

        // Act
        typePenaliteService.save(penalite);

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM PRM_TYPE_PENALITE", Long.class);
        verify(typePenaliteService, times(1)).resetSequence();
        verify(typePenaliteRepository).save(penalite);
    }

    @Test
    public void testSave_WithNonEmptyTable() {
        // Arrange
        TypePenalite penalite = new TypePenalite();
        penalite.setId(1L);
        penalite.setDesignation("Nouvelle Pénalité");

        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PRM_TYPE_PENALITE", Long.class)).thenReturn(5L);
        when(typePenaliteRepository.save(penalite)).thenReturn(penalite);

        // Act
        typePenaliteService.save(penalite);

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM PRM_TYPE_PENALITE", Long.class);
        verify(typePenaliteService, never()).resetSequence();
        verify(typePenaliteRepository).save(penalite);
    }

    @Test
    public void testFindById() {
        // Arrange
        Long id = 1L;
        TypePenalite penalite = new TypePenalite();
        penalite.setId(id);
        penalite.setDesignation("Pénalité Test");

        when(typePenaliteRepository.findOne(id)).thenReturn(penalite);

        // Act
        TypePenalite result = typePenaliteService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Pénalité Test", result.getDesignation());
    }

    @Test
    public void testDeleteTypePenalite() {
        // Arrange
        TypePenalite penalite = new TypePenalite();
        penalite.setId(1L);
        penalite.setDesignation("Pénalité à supprimer");

        doNothing().when(typePenaliteRepository).delete(penalite);

        // Act
        typePenaliteService.deleteTypePenalite(penalite);

        // Assert
        verify(typePenaliteRepository).delete(penalite);
    }

    @Test
    public void testResetSequence_WithExistingRecords() {
        // Arrange
        when(jdbcTemplate.queryForObject("SELECT COALESCE(MAX(ID_TYPE_PEN), 0) FROM PRM_TYPE_PENALITE", Long.class)).thenReturn(5L);
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        typePenaliteService.resetSequence();

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COALESCE(MAX(ID_TYPE_PEN), 0) FROM PRM_TYPE_PENALITE", Long.class);
        verify(jdbcTemplate).execute("ALTER SEQUENCE ACHAT.TYPE_PENALITE_SEQ RESTART START WITH 6");
    }

    @Test
    public void testResetSequence_WithNoRecords() {
        // Arrange
        when(jdbcTemplate.queryForObject("SELECT COALESCE(MAX(ID_TYPE_PEN), 0) FROM PRM_TYPE_PENALITE", Long.class)).thenReturn(0L);
        doNothing().when(jdbcTemplate).execute(anyString());

        // Act
        typePenaliteService.resetSequence();

        // Assert
        verify(jdbcTemplate).queryForObject("SELECT COALESCE(MAX(ID_TYPE_PEN), 0) FROM PRM_TYPE_PENALITE", Long.class);
        verify(jdbcTemplate).execute("ALTER SEQUENCE ACHAT.TYPE_PENALITE_SEQ RESTART START WITH 1");
    }
}