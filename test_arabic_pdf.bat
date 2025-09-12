@echo off
echo ========================================
echo Test de la Solution Arabe - Export PDF
echo ========================================

echo.
echo 1. Verification des fichiers de polices...
if exist "src\main\resources\fonts\arabic\Amiri-Regular.ttf" (
    echo [OK] Police Amiri trouvee
) else (
    echo [ERREUR] Police Amiri manquante
)

if exist "src\main\resources\fonts\arabic\NotoNaskhArabic-Regular.ttf" (
    echo [OK] Police Noto Naskh Arabic trouvee
) else (
    echo [ERREUR] Police Noto Naskh Arabic manquante
)

echo.
echo 2. Compilation du projet...
call compile.bat

echo.
echo 3. Demarrage du serveur...
echo Le serveur va demarrer sur http://localhost:8080
echo Pour tester l'export PDF:
echo 1. Ouvrir http://localhost:8080
echo 2. Aller vers "Période d'analyse de l'évaluation du marché"
echo 3. Sélectionner une période
echo 4. Cliquer sur "Export PDF"
echo 5. Vérifier que le texte arabe s'affiche correctement
echo.
echo Appuyez sur une touche pour continuer...
pause

echo.
echo Test termine !
echo Vérifiez les logs du serveur pour les messages d'initialisation des polices. 