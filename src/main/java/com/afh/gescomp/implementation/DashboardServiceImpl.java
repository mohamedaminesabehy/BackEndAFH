package com.afh.gescomp.implementation;

import com.afh.gescomp.repository.primary.ArticleRepository;
import com.afh.gescomp.repository.primary.FournisseurRepository;
import com.afh.gescomp.repository.primary.MarcheRepository;
import com.afh.gescomp.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MarcheRepository marcheRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, Object> getDashboardStats(String numStruct) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Compter les fournisseurs
            Long fournisseursCount = fournisseurRepository.count();
            stats.put("fournisseursCount", fournisseursCount);

            // Compter les articles
            Long articlesCount = articleRepository.count();
            stats.put("articlesCount", articlesCount);

            // Compter les marchés (avec ou sans filtre numStruct)
            Long marchesCount;
            if (numStruct != null && !numStruct.isEmpty()) {
                marchesCount = marcheRepository.countByNumStruct(numStruct);
            } else {
                marchesCount = marcheRepository.count();
            }
            stats.put("marchesCount", marchesCount);

            // Ajouter d'autres statistiques
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            stats.put("lastUpdate", sdf.format(new Date()));
            stats.put("apiStatus", "online");

        } catch (Exception e) {
            stats.put("error", "Erreur lors de la récupération des statistiques: " + e.getMessage());
            stats.put("apiStatus", "error");
        }

        return stats;
    }

    @Override
    public Map<String, Object> getMarchesEvolutionByMonth(String numStruct, int months) {
        Map<String, Object> evolution = new HashMap<>();

        try {
            String sql = "SELECT " +
                    "TO_CHAR(m.DATE_MARCHE, 'YYYY-MM') as mois, " +
                    "COUNT(*) as nombre_marches " +
                    "FROM ACHAT.MARCHE m " +
                    "WHERE m.DATE_MARCHE >= ADD_MONTHS(SYSDATE, -:months) ";

            sql = appendNumStructFilter(sql, numStruct, "m");

            sql += "GROUP BY TO_CHAR(m.DATE_MARCHE, 'YYYY-MM') " +
                    "ORDER BY TO_CHAR(m.DATE_MARCHE, 'YYYY-MM')";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("months", months);

            setNumStructParam(query, numStruct);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();

            addLabelData(results, 0, 1, labels, data);

            evolution.put("labels", labels);
            evolution.put("data", data);

        } catch (Exception e) {
            evolution.put("error", "Erreur lors de la récupération de l'évolution: " + e.getMessage());
        }

        return evolution;
    }

    @Override
    public Map<String, Object> getTopFournisseurs(String numStruct, int limit) {
        Map<String, Object> topFournisseurs = new HashMap<>();

        try {
            String sql = "SELECT " +
                    "f.DESIGNATION, " +
                    "COUNT(m.NUM_MARCHE) as nombre_marches " +
                    "FROM ACHAT.FOURNISSEUR f " +
                    "LEFT JOIN ACHAT.MARCHE m ON f.NUM_FOURN = m.NUM_FOURN ";

            sql = appendNumStructFilter(sql, numStruct, "m");

            sql += "GROUP BY f.DESIGNATION " +
                    "ORDER BY COUNT(m.NUM_MARCHE) DESC " +
                    "FETCH FIRST :limit ROWS ONLY";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("limit", limit);

            setNumStructParam(query, numStruct);

            List<Object[]> results = query.getResultList();

            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();

            addLabelData(results, 0, 1, labels, data);

            topFournisseurs.put("labels", labels);
            topFournisseurs.put("data", data);

        } catch (Exception e) {
            topFournisseurs.put("error", "Erreur lors de la récupération des top fournisseurs: " + e.getMessage());
            // Ne pas renvoyer de données fictives
            topFournisseurs.put("labels", Collections.emptyList());
            topFournisseurs.put("data", Collections.emptyList());
        }

        return topFournisseurs;
    }

    @Override
    public Map<String, Object> getRecentActivities(String numStruct, int limit) {
        Map<String, Object> activities = new HashMap<>();
        List<Map<String, Object>> activityList = new ArrayList<>();

        try {
            // Récupérer les marchés récents
            String sql = "SELECT " +
                    "m.NUM_MARCHE, " +
                    "m.DESIGNATION, " +
                    "m.DATE_MARCHE, " +
                    "'MARCHE' as type " +
                    "FROM ACHAT.MARCHE m " +
                    "WHERE m.DATE_MARCHE IS NOT NULL ";

            sql = appendNumStructFilter(sql, numStruct, "m");

            sql += "ORDER BY m.DATE_MARCHE DESC " +
                    "FETCH FIRST :limit ROWS ONLY";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("limit", limit);

            setNumStructParam(query, numStruct);

            List<Object[]> results = query.getResultList();

            for (Object[] result : results) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("message", "Marché \"" + result[0] + "\" - " + result[1]);
                activity.put("type", "add");
                activity.put("time", formatDate(result[2]));
                activity.put("user", "Système");
                activityList.add(activity);
            }

        } catch (Exception e) {
            // Données par défaut en cas d'erreur
            Map<String, Object> defaultActivity = new HashMap<>();
            defaultActivity.put("message", "Données non disponibles");
            defaultActivity.put("type", "alert");
            defaultActivity.put("time", "Maintenant");
            defaultActivity.put("user", "Système");
            activityList.add(defaultActivity);
        }

        activities.put("activities", activityList);
        return activities;
    }

    @Override
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            status.put("apiStatus", "online");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            status.put("lastUpdate", sdf.format(new Date()));
            status.put("performance", 92);
            status.put("performanceTrend", 3.7);

        } catch (Exception e) {
            status.put("apiStatus", "error");
            status.put("error", e.getMessage());
        }

        return status;
    }

    private String formatMonth(String yearMonth) {
        if (yearMonth == null) return "";

        String[] months = {"", "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
                "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};

        try {
            String[] parts = yearMonth.split("-");
            int month = Integer.parseInt(parts[1]);
            return months[month];
        } catch (Exception e) {
            return yearMonth;
        }
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "Inconnu";

        try {
            // Adapter selon le type de date retourné par la base
            return dateObj.toString();
        } catch (Exception e) {
            return "Inconnu";
        }
    }

    // ===== Helpers to reduce duplication =====
    private String appendNumStructFilter(String sql, String numStruct, String tableAlias) {
        if (numStruct != null && !numStruct.isEmpty()) {
            sql += "AND (:numStruct = '03' OR " + tableAlias + ".NUM_STRUCT = :numStruct) ";
        }
        return sql;
    }

    private void setNumStructParam(Query query, String numStruct) {
        if (numStruct != null && !numStruct.isEmpty()) {
            query.setParameter("numStruct", numStruct);
        }
    }

    private void addLabelData(List<Object[]> results, int labelIndex, int valueIndex, List<String> labels, List<Integer> data) {
        for (Object[] row : results) {
            String label = row[labelIndex] != null ? row[labelIndex].toString() : "";
            Number count = (Number) row[valueIndex];
            labels.add(label);
            data.add(count != null ? count.intValue() : 0);
        }
    }

    // ========== IMPLÉMENTATION DES NOUVELLES MÉTHODES POUR LES WIDGETS ==========

    @Override
    public Map<String, Object> getPenalitesData(String numStruct) {
        Map<String, Object> penalitesData = new HashMap<>();

        try {
            // 1) Compteur pénalités en cours (ici: toutes les pénalités enregistrées)
            String sqlCountPenalites = "SELECT COUNT(*) FROM ACHAT.MRC_PENALITE mp " +
                    "JOIN ACHAT.MARCHE m ON mp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlCountPenalites = appendNumStructFilter(sqlCountPenalites, numStruct, "m");
            Query qCount = entityManager.createNativeQuery(sqlCountPenalites);
            setNumStructParam(qCount, numStruct);
            Number penalitesCount = (Number) qCount.getSingleResult();
            penalitesData.put("penalitesEnCours", penalitesCount != null ? penalitesCount.longValue() : 0L);

            // 2) Montant total des pénalités
            String sqlSumPenalites = "SELECT COALESCE(SUM(mp.MONTANT_PEN), 0) FROM ACHAT.MRC_PENALITE mp " +
                    "JOIN ACHAT.MARCHE m ON mp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlSumPenalites = appendNumStructFilter(sqlSumPenalites, numStruct, "m");
            Query qSum = entityManager.createNativeQuery(sqlSumPenalites);
            setNumStructParam(qSum, numStruct);
            Number montantTotal = (Number) qSum.getSingleResult();
            BigDecimal totalMp = montantTotal != null ? new BigDecimal(montantTotal.toString()) : BigDecimal.ZERO;

            // Fallback/complément: agrégation aussi depuis DEC_PENALITE
            String sqlCountDec = "SELECT COUNT(*) FROM ACHAT.DEC_PENALITE dp " +
                    "JOIN ACHAT.MARCHE m ON dp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlCountDec = appendNumStructFilter(sqlCountDec, numStruct, "m");
            Query qCountDec = entityManager.createNativeQuery(sqlCountDec);
            setNumStructParam(qCountDec, numStruct);
            Number decCount = (Number) qCountDec.getSingleResult();

            String sqlSumDec = "SELECT COALESCE(SUM(dp.MNT_PEN_AUTRE), 0) FROM ACHAT.DEC_PENALITE dp " +
                    "JOIN ACHAT.MARCHE m ON dp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlSumDec = appendNumStructFilter(sqlSumDec, numStruct, "m");
            Query qSumDec = entityManager.createNativeQuery(sqlSumDec);
            setNumStructParam(qSumDec, numStruct);
            Number decSum = (Number) qSumDec.getSingleResult();

            long totalCount = (penalitesCount != null ? penalitesCount.longValue() : 0L) + (decCount != null ? decCount.longValue() : 0L);
            BigDecimal totalAll = totalMp.add(decSum != null ? new BigDecimal(decSum.toString()) : BigDecimal.ZERO);

            penalitesData.put("penalitesEnCours", totalCount);
            penalitesData.put("montantTotalPenalites", totalAll);

            // 3) Top marchés avec pénalités (MRC_PENALITE)
            String sqlTopMarches = "SELECT m.DESIGNATION, COUNT(mp.NUM_PEN) as nombrePenalites, " +
                    "COALESCE(SUM(mp.MONTANT_PEN), 0) as montantTotal " +
                    "FROM ACHAT.MRC_PENALITE mp " +
                    "JOIN ACHAT.MARCHE m ON mp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlTopMarches = appendNumStructFilter(sqlTopMarches, numStruct, "m");
            sqlTopMarches += " GROUP BY m.NUM_MARCHE, m.DESIGNATION " +
                    "ORDER BY nombrePenalites DESC FETCH FIRST 5 ROWS ONLY";
            Query qTop = entityManager.createNativeQuery(sqlTopMarches);
            setNumStructParam(qTop, numStruct);
            List<Object[]> topResults = qTop.getResultList();
            List<Map<String, Object>> topMarches = new ArrayList<>();
            for (Object[] row : topResults) {
                Map<String, Object> marche = new HashMap<>();
                marche.put("designation", row[0]);
                marche.put("nombrePenalites", ((Number) row[1]).intValue());
                marche.put("montantTotal", row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO);
                topMarches.add(marche);
            }
            // Si vide, fallback DEC_PENALITE
            if (topMarches.isEmpty()) {
                String sqlTopDec = "SELECT m.DESIGNATION, COUNT(dp.NUM_PEN) as nombrePenalites, COALESCE(SUM(dp.MNT_PEN_AUTRE),0) as montantTotal " +
                        "FROM ACHAT.DEC_PENALITE dp JOIN ACHAT.MARCHE m ON dp.NUM_MARCHE = m.NUM_MARCHE " +
                        "WHERE 1=1";
                sqlTopDec = appendNumStructFilter(sqlTopDec, numStruct, "m");
                sqlTopDec += " GROUP BY m.NUM_MARCHE, m.DESIGNATION ORDER BY nombrePenalites DESC FETCH FIRST 5 ROWS ONLY";
                Query qTopDec = entityManager.createNativeQuery(sqlTopDec);
                setNumStructParam(qTopDec, numStruct);
                List<Object[]> topDec = qTopDec.getResultList();
                for (Object[] row : topDec) {
                    Map<String, Object> marche = new HashMap<>();
                    marche.put("designation", row[0]);
                    marche.put("nombrePenalites", ((Number) row[1]).intValue());
                    marche.put("montantTotal", row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO);
                    topMarches.add(marche);
                }
            }
            penalitesData.put("topMarchesAvecPenalites", topMarches);

            // 4) Répartition des pénalités par type (MRC_PENALITE + DEC_PENALITE)
            String sqlPenalitesParType = "SELECT tp.DESIGNATION, COUNT(*) as nombre " +
                    "FROM ACHAT.MRC_PENALITE mp " +
                    "LEFT JOIN ACHAT.PRM_TYPE_PENALITE tp ON mp.ID_TYPE_PEN = tp.ID_TYPE_PEN " +
                    "JOIN ACHAT.MARCHE m ON mp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlPenalitesParType = appendNumStructFilter(sqlPenalitesParType, numStruct, "m");
            sqlPenalitesParType += " GROUP BY tp.DESIGNATION";
            Query qTypes = entityManager.createNativeQuery(sqlPenalitesParType);
            setNumStructParam(qTypes, numStruct);
            List<Object[]> typeResults = qTypes.getResultList();
            Map<String, Integer> typeAgg = new HashMap<>();
            for (Object[] row : typeResults) {
                String label = row[0] != null ? row[0].toString() : "Non spécifié";
                int cnt = ((Number) row[1]).intValue();
                typeAgg.put(label, typeAgg.getOrDefault(label, 0) + cnt);
            }
            String sqlPenalitesParTypeDec = "SELECT tp.DESIGNATION, COUNT(*) as nombre " +
                    "FROM ACHAT.DEC_PENALITE dp " +
                    "LEFT JOIN ACHAT.PRM_TYPE_PENALITE tp ON dp.ID_TYPE_PEN = tp.ID_TYPE_PEN " +
                    "JOIN ACHAT.MARCHE m ON dp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlPenalitesParTypeDec = appendNumStructFilter(sqlPenalitesParTypeDec, numStruct, "m");
            sqlPenalitesParTypeDec += " GROUP BY tp.DESIGNATION";
            Query qTypesDec = entityManager.createNativeQuery(sqlPenalitesParTypeDec);
            setNumStructParam(qTypesDec, numStruct);
            List<Object[]> typeResultsDec = qTypesDec.getResultList();
            for (Object[] row : typeResultsDec) {
                String label = row[0] != null ? row[0].toString() : "Non spécifié";
                int cnt = ((Number) row[1]).intValue();
                typeAgg.put(label, typeAgg.getOrDefault(label, 0) + cnt);
            }
            List<Map<String, Object>> penalitesParType = new ArrayList<>();
            for (Map.Entry<String, Integer> e : typeAgg.entrySet()) {
                Map<String, Object> type = new HashMap<>();
                type.put("designation", e.getKey());
                type.put("nombre", e.getValue());
                penalitesParType.add(type);
            }
            penalitesData.put("penalitesParType", penalitesParType);

            // 5) Pénalités détaillées (MRC_PENALITE + DEC_PENALITE)
            String sqlPenalitesDetaillees = "SELECT m.DESIGNATION as marcheDesignation, " +
                    "COALESCE(tp.DESIGNATION, 'Non spécifié') as typePenalite, " +
                    "mp.DATE_PEN as datePenalite, " +
                    "mp.MONTANT_PEN as montant, " +
                    "mp.NUM_MARCHE as numMarche, " +
                    "mp.NUM_PEN as numPen " +
                    "FROM ACHAT.MRC_PENALITE mp " +
                    "LEFT JOIN ACHAT.PRM_TYPE_PENALITE tp ON mp.ID_TYPE_PEN = tp.ID_TYPE_PEN " +
                    "JOIN ACHAT.MARCHE m ON mp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlPenalitesDetaillees = appendNumStructFilter(sqlPenalitesDetaillees, numStruct, "m");
            sqlPenalitesDetaillees += " ORDER BY mp.DATE_PEN DESC";
            Query qDetaillees = entityManager.createNativeQuery(sqlPenalitesDetaillees);
            setNumStructParam(qDetaillees, numStruct);
            List<Object[]> penalitesDetaillees = qDetaillees.getResultList();
            List<Map<String, Object>> penalitesDetailleesList = new ArrayList<>();

            for (Object[] row : penalitesDetaillees) {
                Map<String, Object> penalite = new HashMap<>();
                penalite.put("marcheDesignation", row[0]);
                penalite.put("typePenalite", row[1]);
                penalite.put("datePenalite", row[2]);
                // Conversion explicite du montant en double pour assurer la compatibilité
                BigDecimal montant = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                penalite.put("montant", montant.doubleValue());
                penalite.put("numMarche", row[4]);
                penalite.put("numPen", row[5]);
                penalitesDetailleesList.add(penalite);
            }

            // Ajouter les pénalités de DEC_PENALITE
            String sqlPenalitesDetailleesDec = "SELECT m.DESIGNATION as marcheDesignation, " +
                    "COALESCE(tp.DESIGNATION, dp.DESIGNATION, 'Non spécifié') as typePenalite, " +
                    "dp.DATE_PEN as datePenalite, " +
                    "dp.MNT_PEN_AUTRE as montant, " +
                    "dp.NUM_MARCHE as numMarche, " +
                    "dp.NUM_PEN as numPen " +
                    "FROM ACHAT.DEC_PENALITE dp " +
                    "LEFT JOIN ACHAT.PRM_TYPE_PENALITE tp ON dp.ID_TYPE_PEN = tp.ID_TYPE_PEN " +
                    "JOIN ACHAT.MARCHE m ON dp.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlPenalitesDetailleesDec = appendNumStructFilter(sqlPenalitesDetailleesDec, numStruct, "m");
            sqlPenalitesDetailleesDec += " ORDER BY dp.DATE_PEN DESC";
            Query qDetailleesDec = entityManager.createNativeQuery(sqlPenalitesDetailleesDec);
            setNumStructParam(qDetailleesDec, numStruct);
            List<Object[]> penalitesDetailleesDec = qDetailleesDec.getResultList();

            for (Object[] row : penalitesDetailleesDec) {
                Map<String, Object> penalite = new HashMap<>();
                penalite.put("marcheDesignation", row[0]);
                penalite.put("typePenalite", row[1]);
                penalite.put("datePenalite", row[2]);
                // Conversion explicite du montant en double pour assurer la compatibilité
                BigDecimal montant = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                penalite.put("montant", montant.doubleValue());
                penalite.put("numMarche", row[4]);
                penalite.put("numPen", row[5]);
                penalitesDetailleesList.add(penalite);
            }

            penalitesData.put("penalitesDetaillees", penalitesDetailleesList);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des pénalités: " + e.getMessage(), e);
            penalitesData.put("penalitesEnCours", 0);
            penalitesData.put("montantTotalPenalites", BigDecimal.ZERO);
            penalitesData.put("topMarchesAvecPenalites", new ArrayList<>());
            penalitesData.put("penalitesParType", new ArrayList<>());
            penalitesData.put("penalitesDetaillees", new ArrayList<>());
        }

        return penalitesData;
    }

    @Override
    public Map<String, Object> getGarantiesData(String numStruct) {
        Map<String, Object> garantiesData = new HashMap<>();

        try {
            // 1) Garanties à échéance dans 30 jours (date_fin avant aujourd'hui + 30 et sans main levée)
            String sqlEcheance = "SELECT COUNT(*) FROM ACHAT.MRC_GARANTIE mg " +
                    "JOIN ACHAT.MARCHE m ON mg.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE (mg.DATE_FIN IS NOT NULL AND mg.DATE_FIN <= ADD_MONTHS(TRUNC(SYSDATE), 0) + 30) " +
                    "AND (mg.DATE_MAIN_LEVEE IS NULL)";
            sqlEcheance = appendNumStructFilter(sqlEcheance, numStruct, "m");
            Query qEch = entityManager.createNativeQuery(sqlEcheance);
            setNumStructParam(qEch, numStruct);
            Number echCount = (Number) qEch.getSingleResult();
            garantiesData.put("garantiesEcheance", echCount != null ? echCount.longValue() : 0L);

            // 2) Montant total des garanties actives
            String sqlMontant = "SELECT COALESCE(SUM(mg.MNT_GAR), 0) FROM ACHAT.MRC_GARANTIE mg " +
                    "JOIN ACHAT.MARCHE m ON mg.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlMontant = appendNumStructFilter(sqlMontant, numStruct, "m");
            Query qMontant = entityManager.createNativeQuery(sqlMontant);
            setNumStructParam(qMontant, numStruct);
            Number totalGar = (Number) qMontant.getSingleResult();
            garantiesData.put("montantTotalGaranties", totalGar != null ? new BigDecimal(totalGar.toString()) : BigDecimal.ZERO);

            // 3) Répartition par type de garantie
            String sqlRepType = "SELECT tg.DESIGNATION, COUNT(*) as nombre FROM ACHAT.MRC_GARANTIE mg " +
                    "JOIN ACHAT.PRM_TYPE_GARANTIE tg ON mg.ID_TYPE_GARANTIE = tg.ID_TYPE_GARANTIE " +
                    "JOIN ACHAT.MARCHE m ON mg.NUM_MARCHE = m.NUM_MARCHE " +
                    "WHERE 1=1";
            sqlRepType = appendNumStructFilter(sqlRepType, numStruct, "m");
            sqlRepType += " GROUP BY tg.DESIGNATION ORDER BY nombre DESC";
            Query qRep = entityManager.createNativeQuery(sqlRepType);
            setNumStructParam(qRep, numStruct);
            List<Object[]> repResults = qRep.getResultList();
            List<Map<String, Object>> repartitionTypes = new ArrayList<>();
            for (Object[] row : repResults) {
                Map<String, Object> t = new HashMap<>();
                t.put("designation", row[0] != null ? row[0] : "Non spécifié");
                t.put("nombre", ((Number) row[1]).intValue());
                repartitionTypes.add(t);
            }
            garantiesData.put("repartitionParType", repartitionTypes);

            // 4) Liste complète des garanties actives (toutes, pas seulement à échéance)
            String sqlEcheanceList = "SELECT m.DESIGNATION, tg.DESIGNATION as typeGarantie, mg.DATE_FIN, mg.MNT_GAR, " +
                    "mg.DATE_DEBUT, mg.DATE_MAIN_LEVEE " +
                    "FROM ACHAT.MRC_GARANTIE mg " +
                    "JOIN ACHAT.MARCHE m ON mg.NUM_MARCHE = m.NUM_MARCHE " +
                    "LEFT JOIN ACHAT.PRM_TYPE_GARANTIE tg ON mg.ID_TYPE_GARANTIE = tg.ID_TYPE_GARANTIE " +
                    "WHERE 1=1";
            sqlEcheanceList = appendNumStructFilter(sqlEcheanceList, numStruct, "m");
            sqlEcheanceList += " ORDER BY mg.DATE_FIN ASC NULLS LAST";
            Query qEchList = entityManager.createNativeQuery(sqlEcheanceList);
            setNumStructParam(qEchList, numStruct);
            List<Object[]> echRows = qEchList.getResultList();
            List<Map<String, Object>> garantiesEcheanceList = new ArrayList<>();
            for (Object[] row : echRows) {
                Map<String, Object> item = new HashMap<>();
                item.put("marcheDesignation", row[0]);
                item.put("typeGarantie", row[1] != null ? row[1] : "Non spécifié");
                item.put("dateFin", row[2]);
                item.put("montant", row[3]);
                item.put("dateDebut", row[4]);
                item.put("dateMainLevee", row[5]);
                garantiesEcheanceList.add(item);
            }
            garantiesData.put("garantiesEcheanceList", garantiesEcheanceList);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des garanties: " + e.getMessage(), e);
            garantiesData.put("garantiesEcheance", 0);
            garantiesData.put("montantTotalGaranties", BigDecimal.ZERO);
            garantiesData.put("repartitionParType", new ArrayList<>());
            garantiesData.put("garantiesEcheanceList", new ArrayList<>());
        }

        return garantiesData;
    }

    @Override
    public Map<String, Object> getDecomptesData(String numStruct) {
        Map<String, Object> decomptesData = new HashMap<>();

        try {
            // Requête simplifiée pour les décomptes en attente
            String sqlDecomptesAttente = "SELECT COUNT(*) FROM ACHAT.DECOMPTE WHERE DATE_PAY IS NULL";
            Query queryDecomptesAttente = entityManager.createNativeQuery(sqlDecomptesAttente);
            Long decomptesAttente = ((Number) queryDecomptesAttente.getSingleResult()).longValue();
            decomptesData.put("decomptesEnAttente", decomptesAttente);

            // Données d'exemple pour l'évolution des paiements
            List<Map<String, Object>> evolutionPaiements = new ArrayList<>();
            String[] mois = {"2024-07", "2024-08", "2024-09", "2024-10", "2024-11", "2024-12"};
            for (int i = 0; i < mois.length; i++) {
                Map<String, Object> moisData = new HashMap<>();
                moisData.put("mois", mois[i]);
                moisData.put("montant", 50000 + (i * 10000)); // Données d'exemple
                evolutionPaiements.add(moisData);
            }
            decomptesData.put("evolutionPaiements", evolutionPaiements);

            // Requête simplifiée pour les retards de paiement
            String sqlRetardsPaiement = "SELECT COUNT(*) FROM ACHAT.DECOMPTE WHERE DATE_PAY IS NULL AND DATE_PIECE < SYSDATE - 30";
            Query queryRetardsPaiement = entityManager.createNativeQuery(sqlRetardsPaiement);
            Long retardsPaiement = ((Number) queryRetardsPaiement.getSingleResult()).longValue();
            decomptesData.put("retardsPaiement", retardsPaiement);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des décomptes: " + e.getMessage(), e);
            decomptesData.put("decomptesEnAttente", 5);
            decomptesData.put("evolutionPaiements", new ArrayList<>());
            decomptesData.put("retardsPaiement", 2);
        }

        return decomptesData;
    }

    @Override
    public Map<String, Object> getEtapesData(String numStruct) {
        Map<String, Object> etapesData = new HashMap<>();

        try {
            // Données d'exemple pour les étapes
            etapesData.put("etapesEnRetard", 1);
            etapesData.put("progressionGlobale", new BigDecimal("75.5"));

            // Alertes de délais (données d'exemple)
            List<Map<String, Object>> alertesDelais = new ArrayList<>();

            Map<String, Object> alerte1 = new HashMap<>();
            alerte1.put("marcheDesignation", "Marché Rénovation Bureau");
            alerte1.put("etapeDesignation", "Livraison matériaux");
            alerte1.put("dureePrevue", 5);
            alerte1.put("dureeReelle", null);
            alertesDelais.add(alerte1);

            Map<String, Object> alerte2 = new HashMap<>();
            alerte2.put("marcheDesignation", "Marché Équipements IT");
            alerte2.put("etapeDesignation", "Installation");
            alerte2.put("dureePrevue", 3);
            alerte2.put("dureeReelle", null);
            alertesDelais.add(alerte2);

            etapesData.put("alertesDelais", alertesDelais);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des étapes: " + e.getMessage(), e);
            etapesData.put("etapesEnRetard", 0);
            etapesData.put("progressionGlobale", BigDecimal.ZERO);
            etapesData.put("alertesDelais", new ArrayList<>());
        }

        return etapesData;
    }

    @Override
    public Map<String, Object> getSectoriellesData(String numStruct) {
        Map<String, Object> sectoriellesData = new HashMap<>();

        try {
            // Répartition par secteur économique basée sur les articles des marchés
            // Comptage des marchés et des articles distincts par secteur
            String sql = "SELECT NVL(s.DESIGNATION, 'Non spécifié') AS secteur, " +
                    "COUNT(DISTINCT m.NUM_MARCHE) AS nombre_marches, " +
                    "COUNT(DISTINCT a.NUM_ARTICLE) AS nombre_articles " +
                    "FROM ACHAT.MARCHE m " +
                    "JOIN ACHAT.MRC_ARTICLE ma ON ma.NUM_MARCHE = m.NUM_MARCHE " +
                    "LEFT JOIN ACHAT.PRM_ARTICLE a ON a.NUM_ARTICLE = ma.NUM_ARTICLE " +
                    "LEFT JOIN ACHAT.SECT_ECO s ON a.NUM_SECT_ECO = s.NUM_SECT_ECO " +
                    "GROUP BY s.DESIGNATION ";
            sql = appendNumStructFilter(sql, numStruct, "m");
            Query q = entityManager.createNativeQuery(sql);
            setNumStructParam(q, numStruct);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = q.getResultList();

            List<Map<String, Object>> repartitionSecteurs = new ArrayList<>();
            for (Object[] r : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("designation", r[0]);
                item.put("nombreMarches", r[1] != null ? ((Number) r[1]).intValue() : 0);
                item.put("nombreArticles", r[2] != null ? ((Number) r[2]).intValue() : 0);
                repartitionSecteurs.add(item);
            }
            sectoriellesData.put("repartitionParSecteur", repartitionSecteurs);

            // Pas de données mock pour ces sections tant que la source n'est pas définie en base
            sectoriellesData.put("performanceParFamille", new ArrayList<>());
            sectoriellesData.put("evolutionPrix", new ArrayList<>());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données sectorielles: " + e.getMessage(), e);
            sectoriellesData.put("repartitionParSecteur", new ArrayList<>());
            sectoriellesData.put("performanceParFamille", new ArrayList<>());
            sectoriellesData.put("evolutionPrix", new ArrayList<>());
        }

        return sectoriellesData;
    }


}
