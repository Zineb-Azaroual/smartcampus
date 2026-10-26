# Intégration continue SmartCampus

## Le pipeline

Le pipeline est décrit dans le `Jenkinsfile` à la racine du dépôt :

1. **Checkout** : Jenkins récupère le commit à construire.
2. **Build** : `mvn -B clean package -DskipTests` compile et produit le JAR.
3. **Test** : `mvn -B test` lance les tests JUnit, les résultats sont publiés dans Jenkins.
4. **Archivage** : si tout est vert, `target/*.jar` est conservé avec une empreinte.

## Les jobs Jenkins

| Job                    | Branche                       | Rôle                                      |
|------------------------|-------------------------------|-------------------------------------------|
| `smartcampus-pipeline` | `main`                        | Version de référence, produit l'artefact  |
| `smartcampus-branches` | toutes (`**`)                 | Vérifie chaque branche poussée            |
| `smartcampus-echec`    | `test/SC-14-echec-volontaire` | Démonstration d'un échec puis correction  |

## Déclenchement automatique

Jenkins tourne en local, donc GitHub ne peut pas l'appeler directement. On passe par un relais smee.io :

```
push sur GitHub -> webhook -> smee.io -> relais smee sur le PC -> Jenkins /github-webhook/
```

À chaque push, Jenkins compare les branches avec son dernier build et lance le pipeline s'il trouve un nouveau commit.
