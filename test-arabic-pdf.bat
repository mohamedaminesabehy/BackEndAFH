@echo off
echo ========================================
echo Test de Generation PDF avec Support Arabe
echo ========================================
echo.

echo 1. Compilation du projet...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)
echo ✅ Compilation reussie
echo.

echo 2. Demarrage de l'application...
start "Test PDF Arabe" mvn spring-boot:run
echo ✅ Application demarree
echo.

echo 3. Attente du demarrage (30 secondes)...
timeout /t 30 /nobreak > nul
echo.

echo 4. Test de l'export PDF...
echo Ouvrez votre navigateur et allez sur: http://localhost:8080
echo Puis testez l'export PDF dans la section "Période d'analyse"
echo.

echo 5. Vérification des logs...
echo Regardez les logs de l'application pour voir:
echo - "✓ Police Amiri chargée avec succès"
echo - "✓ Police Noto Naskh Arabic chargée avec succès"
echo - "✓ Police latine chargée avec succès"
echo.

echo ========================================
echo Test termine
echo ========================================
pause 