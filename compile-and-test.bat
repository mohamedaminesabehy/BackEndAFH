@echo off
echo ========================================
echo Compilation et Test - Support Arabe PDF
echo ========================================
echo.

echo 1. Nettoyage des anciens builds...
call mvn clean
echo.

echo 2. Compilation avec Maven...
call mvn compile
if %ERRORLEVEL% neq 0 (
    echo ERREUR: Compilation échouée
    pause
    exit /b 1
)
echo.

echo 3. Test des dépendances...
call mvn dependency:tree | findstr itext
echo.

echo 4. Compilation complète...
call mvn package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERREUR: Build échoué
    pause
    exit /b 1
)
echo.

echo 5. Vérification des polices arabes...
if exist "src\main\resources\fonts\arabic\arial-unicode-ms.ttf" (
    echo ✓ Police Arial Unicode MS trouvée
) else (
    echo ⚠ Police Arial Unicode MS manquante
)

if exist "src\main\resources\fonts\arabic\DejaVuSans.ttf" (
    echo ✓ Police DejaVu Sans trouvée
) else (
    echo ⚠ Police DejaVu Sans manquante
)
echo.

echo 6. Démarrage de l'application...
echo L'application va démarrer sur http://localhost:8080
echo Appuyez sur Ctrl+C pour arrêter
echo.
call mvn spring-boot:run

pause 