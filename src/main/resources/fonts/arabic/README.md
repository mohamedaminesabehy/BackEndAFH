# Configuration des Polices Arabes pour iText5

## Polices Requises

Pour un support arabe complet dans les PDF, placez les polices suivantes dans ce dossier :

### 1. Police Principale (Recommandée)
- **Nom**: `arial-unicode-ms.ttf`
- **Source**: Microsoft Office ou Windows
- **Support**: Arabe complet + Latin

### 2. Police Alternative (Fallback)
- **Nom**: `DejaVuSans.ttf`
- **Source**: https://dejavu-fonts.github.io/
- **Support**: Arabe + Latin (Open Source)

### 3. Police Système (Dernier recours)
- **Nom**: `helvetica.ttf`
- **Source**: Système d'exploitation
- **Support**: Latin uniquement

## Installation

1. Téléchargez les polices TTF
2. Placez-les dans ce dossier
3. Redémarrez l'application

## Vérification

Les polices sont automatiquement détectées par le service `FournisseurPDFService`.

## Support des Caractères

- **Arabe**: ا ب ت ث ج ح خ د ذ ر ز س ش ص ض ط ظ ع غ ف ق ك ل م ن ه و ي
- **Latin**: A-Z, a-z, 0-9
- **Symboles**: @ # $ % & * ( ) - _ + = [ ] { } | \ : ; " ' < > , . ? /

## Compatibilité

- **Java**: 1.7+
- **iText**: 5.5.13.3+
- **Maven**: 3.6.3+
- **Spring Boot**: 2.x+ 