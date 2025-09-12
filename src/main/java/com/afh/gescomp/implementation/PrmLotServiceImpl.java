package com.afh.gescomp.implementation;

import com.afh.gescomp.dto.PrmLotDTO;
import com.afh.gescomp.dto.PrmTypeLotDTO;
import com.afh.gescomp.model.primary.PrmLot;
import com.afh.gescomp.model.primary.PrmTypeLot;
import com.afh.gescomp.repository.primary.PrmLotRepository;
import com.afh.gescomp.service.PrmLotService;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PrmLotServiceImpl implements PrmLotService {

    private static final Logger logger = LoggerFactory.getLogger(PrmLotServiceImpl.class);

    @Autowired
    private PrmLotRepository prmLotRepository;
    @Autowired
    private DataSource dataSource;

    @Override
    public List<PrmLotDTO> getPrmLots() {
        return prmLotRepository.findPrmLotsDTO();
    }

    @Override
    public PrmLot findPrmLotByIdLot(String idLot) {
        return prmLotRepository.findByIdLot(idLot);
    }

    @Override
    public List<PrmLotDTO> getPrmLotsByMatricule(Integer matricule) {
        List<PrmLotDTO> lots = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{ ? = call GET_PRM_LOTS_BY_MATRICULE(?) }")) {

            stmt.registerOutParameter(1, OracleTypes.CURSOR); // 👈 pour le curseur de retour
            stmt.setInt(2, matricule); // 👈 paramètre d'entrée

            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    PrmLotDTO lot = new PrmLotDTO();
                    lot.setIdLot(rs.getString("id_lot"));
                    lot.setDesignation(rs.getString("designation"));
                    lot.setIdTypeLot(new PrmTypeLot(rs.getLong("id_type_lot"), null)); // temporaire, on le complète plus tard
                    lots.add(lot);
                }
            }

        } catch (SQLException e) {
            logger.error("Erreur lors de l'exécution de la procédure stockée pour récupérer les lots: {}", e.getMessage(), e);
            // logger ou remonter une exception custom
        }

        return lots;
    }

}
