@echo off
echo ========================================
echo Test Simple - Generation PDF Arabe
echo ========================================
echo.

echo 1. Compilation du projet...
call compile.bat
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)
echo ✅ Compilation reussie
echo.

echo 2. Demarrage du serveur...
echo Le serveur va demarrer sur http://localhost:8080
echo Appuyez sur Ctrl+C pour arreter le serveur
echo.
echo Pour tester l'export PDF:
echo 1. Ouvrez votre navigateur
echo 2. Allez sur: http://localhost:8080
echo 3. Naviguez vers "Période d'analyse de l'évaluation du marché"
echo 4. Cliquez sur "Export PDF"
echo 5. Verifiez que le PDF se telecharge et contient du texte arabe
echo.

java -jar target/gescomp-1.0.0.jar

pause 