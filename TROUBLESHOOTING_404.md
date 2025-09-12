# Guide de Dépannage - Erreur 404 lors du Téléchargement PDF

## Problème Identifié

```
Failed to load resource: the server responded with a status of 404 (Not Found)
❌ Erreur lors du téléchargement du PDF: HttpErrorResponse
```

## Causes Possibles

### 1. **Serveur Backend Non Démarré**
- Le serveur Spring Boot n'est pas en cours d'exécution
- L'application n'est pas déployée sur le port 8080

### 2. **URL Incorrecte**
- L'endpoint n'existe pas dans le contrôleur
- Le mapping de l'URL est incorrect
- Le contexte de l'application est différent

### 3. **Problème de Configuration**
- Les annotations Spring ne sont pas reconnues
- Le contrôleur n'est pas scanné par Spring
- Problème de compilation Java

## Solutions

### 1. **Vérifier que le Serveur Fonctionne**

#### Test de Connectivité
```bash
# Test simple avec curl
curl -v http://localhost:8080/api/fournisseur

# Test avec wget
wget -qO- http://localhost:8080/api/fournisseur

# Test dans le navigateur
http://localhost:8080/api/fournisseur
```

#### Vérifier les Logs du Serveur
```bash
# Regarder les logs de démarrage Spring Boot
# Chercher des erreurs de compilation ou de déploiement
```

### 2. **Vérifier la Configuration du Contrôleur**

#### Structure du Contrôleur
```java
@RestController
@RequestMapping("/api/fournisseur")
public class FournisseurController {
    
    @RequestMapping(value = "/export/pdf/{numFourn}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportFournisseurDetailsToPDF(
            @PathVariable String numFourn,
            @RequestParam(value = "supportArabic", defaultValue = "true") boolean supportArabic,
            @RequestParam(value = "fontFamily", defaultValue = "arabic") String fontFamily,
            @RequestParam(value = "encoding", defaultValue = "UTF-8") String encoding) {
        // ... implémentation
    }
}
```

#### URL Attendue
```
GET http://localhost:8080/api/fournisseur/export/pdf/{numFourn}?supportArabic=true&fontFamily=arabic&encoding=UTF-8
```

### 3. **Vérifier la Compilation**

#### Compiler le Projet
```bash
# Utiliser le script de compilation existant
compile.bat

# Ou compiler manuellement
javac -cp "lib/*" src/main/java/com/afh/gescomp/controller/FournisseurController.java
```

#### Vérifier les Erreurs de Compilation
- Annotations Spring non reconnues
- Imports manquants
- Problèmes de syntaxe Java

### 4. **Vérifier le Déploiement**

#### Structure des Fichiers
```
GESCOMP/
├── src/main/java/com/afh/gescomp/
│   ├── controller/
│   │   └── FournisseurController.java  ✅ Doit exister
│   └── implementation/
│       └── FournisseurServiceImpl.java  ✅ Doit exister
├── target/                              ✅ Doit être généré
└── pom.xml                              ✅ Doit être valide
```

#### Vérifier que l'Application est Déployée
```bash
# Regarder les logs de démarrage
# Vérifier que Spring Boot a démarré
# Vérifier que le contrôleur est scanné
```

## Tests de Diagnostic

### 1. **Test de l'Endpoint de Base**
```bash
curl http://localhost:8080/api/fournisseur
```
**Résultat attendu**: Liste des fournisseurs ou page paginée

### 2. **Test de l'Endpoint d'Export**
```bash
curl -v "http://localhost:8080/api/fournisseur/export/pdf/TEST123?supportArabic=true"
```
**Résultat attendu**: Fichier PDF ou erreur 500 (pas 404)

### 3. **Test de Connectivité Angular**
```typescript
// Dans la console du navigateur
fetch('http://localhost:8080/api/fournisseur')
  .then(response => console.log('Status:', response.status))
  .catch(error => console.error('Erreur:', error));
```

## Solutions Spécifiques

### 1. **Si le Serveur ne Démarre Pas**
```bash
# Vérifier Java 1.7
java -version

# Vérifier Maven 3.6.3
mvn -version

# Nettoyer et recompiler
mvn clean compile
```

### 2. **Si les Annotations Spring ne Sont Pas Reconnues**
```java
// Vérifier les imports
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

// Vérifier que la classe est dans le bon package
package com.afh.gescomp.controller;
```

### 3. **Si l'URL n'est Pas Mappée**
```java
// Vérifier le mapping
@RequestMapping("/api/fournisseur")  // Doit correspondre à l'URL appelée
@RequestMapping(value = "/export/pdf/{numFourn}", method = RequestMethod.GET)
```

## Logs de Débogage

### 1. **Côté Frontend (Angular)**
```typescript
console.log('🔍 Test de connectivité avec:', testUrl);
console.log('📋 URL:', apiUrl);
console.log('📋 Paramètres:', params.toString());
```

### 2. **Côté Backend (Java)**
```java
System.out.println("🔄 Génération PDF pour fournisseur: " + numFourn);
System.out.println("📋 Support arabe: " + supportArabic);
System.out.println("🔤 Famille de police: " + fontFamily);
System.out.println("🔤 Encodage: " + encoding);
```

## Vérifications Finales

### 1. **Serveur Accessible**
- ✅ Port 8080 ouvert
- ✅ Application Spring Boot démarrée
- ✅ Logs de démarrage sans erreur

### 2. **Contrôleur Fonctionnel**
- ✅ Classe compilée sans erreur
- ✅ Annotations Spring reconnues
- ✅ Endpoint mappé correctement

### 3. **Service Implémenté**
- ✅ Interface définie
- ✅ Implémentation complète
- ✅ Méthode PDF générée

### 4. **Frontend Configuré**
- ✅ URL correcte dans environment.ts
- ✅ Paramètres envoyés correctement
- ✅ Gestion d'erreur appropriée

## Contact et Support

Si le problème persiste après avoir suivi ce guide :

1. **Vérifiez les logs** du serveur backend
2. **Testez les endpoints** avec curl ou Postman
3. **Vérifiez la compilation** Java
4. **Consultez la documentation** Spring Boot 1.3.8

## Conclusion

L'erreur 404 indique généralement un problème de configuration ou de déploiement. En suivant ce guide étape par étape, vous devriez identifier et résoudre le problème pour que le téléchargement PDF fonctionne correctement avec le support arabe. 