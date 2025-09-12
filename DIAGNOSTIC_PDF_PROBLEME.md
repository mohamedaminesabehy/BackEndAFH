# Guide de Diagnostic - Problème PDF

## Problème Identifié

Le bouton "Télécharger PDF" ne fonctionne pas - rien ne se passe quand on clique dessus.

## Étapes de Diagnostic

### 1. **Vérifier la Console du Navigateur**

1. Ouvrez les **Outils de développement** (F12)
2. Allez dans l'onglet **Console**
3. Cliquez sur le bouton "Télécharger PDF"
4. Regardez les messages de débogage qui commencent par `🔍 DEBUG:`

### 2. **Tester le Bouton de Test Simple**

1. Cliquez sur le bouton **"Test PDF Simple"** (rouge)
2. Vérifiez si un PDF simple s'ouvre
3. Regardez les messages dans la console

### 3. **Vérifier les Données**

Dans la console, vérifiez :
- `🔍 DEBUG: Méthode downloadFournisseurPDF appelée`
- `🔍 DEBUG: Fournisseur reçu: [objet]`
- `🔍 DEBUG: Marchés récupérés: [tableau]`

### 4. **Vérifier pdfMake**

Dans la console, vérifiez :
- `✅ DEBUG: pdfMake est disponible`
- Ou `❌ DEBUG: pdfMake n'est pas défini`

## Solutions Possibles

### **Problème 1: pdfMake non disponible**
```
❌ DEBUG: pdfMake n'est pas défini
```

**Solution:**
- Vérifiez que le fichier `vfs_fonts.js` existe dans `src/assets/fonts/`
- Vérifiez que les imports sont corrects

### **Problème 2: Données manquantes**
```
❌ DEBUG: Numéro de fournisseur manquant
```

**Solution:**
- Vérifiez que `selectedFournisseur` contient bien les données
- Vérifiez que `selectedFournisseurMarches` est défini

### **Problème 3: Erreur dans la définition du PDF**
```
❌ DEBUG: Erreur lors de la génération PDF
```

**Solution:**
- Regardez la stack trace pour identifier l'erreur
- Testez avec le bouton "Test PDF Simple"

### **Problème 4: Bloqueur de popup**
```
Aucun message d'erreur, mais pas de PDF
```

**Solution:**
- Autorisez les popups pour votre site
- Vérifiez les paramètres du navigateur

## Test Manuel depuis la Console

### **Test 1: Vérifier pdfMake**
```javascript
// Dans la console du navigateur
console.log('pdfMake disponible:', typeof pdfMake !== 'undefined');
```

### **Test 2: Tester pdfMake directement**
```javascript
// Dans la console du navigateur
pdfMake.createPdf({content: ['Test']}).open();
```

### **Test 3: Vérifier les données**
```javascript
// Dans la console du navigateur
const component = document.querySelector('app-statistiques-periodes')?.__ngContext__?.instance;
if (component) {
  console.log('selectedFournisseur:', component.selectedFournisseur);
  console.log('selectedFournisseurMarches:', component.selectedFournisseurMarches);
}
```

### **Test 4: Appeler la méthode de test**
```javascript
// Dans la console du navigateur
const component = document.querySelector('app-statistiques-periodes')?.__ngContext__?.instance;
if (component) {
  component.testSimplePDF();
}
```

## Messages de Débogage Attendus

### **Succès:**
```
🔍 DEBUG: Méthode downloadFournisseurPDF appelée
🔍 DEBUG: Fournisseur reçu: {numFourn: "123", designation: "..."}
✅ DEBUG: pdfMake est disponible
🔍 DEBUG: Marchés récupérés: [...]
🔍 DEBUG: Données fournisseur préparées: {...}
🔄 DEBUG: Création de la définition du PDF...
✅ DEBUG: Définition du PDF créée: {...}
🔄 DEBUG: Génération du PDF avec pdfMake...
✅ DEBUG: PDF généré côté client avec succès
```

### **Échec:**
```
🔍 DEBUG: Méthode downloadFournisseurPDF appelée
❌ DEBUG: [message d'erreur spécifique]
```

## Actions à Effectuer

1. **Ouvrez la console** du navigateur (F12)
2. **Cliquez sur "Télécharger PDF"**
3. **Notez tous les messages** qui apparaissent
4. **Testez le bouton "Test PDF Simple"**
5. **Partagez les messages** de la console pour diagnostic

## Contact

Si le problème persiste après ces tests, partagez :
- Les messages de la console
- Le résultat du test PDF simple
- Les erreurs éventuelles 