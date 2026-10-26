# SmartCampus

Plateforme de services du campus. Premier module : l'authentification (Jira SC-11).

## Construire et tester

```bash
mvn clean package
```

Le JAR est produit dans `target/smartcampus-1.0.jar`.

## Lancer l'application

```bash
java -jar target/smartcampus-1.0.jar
```

Variables d'environnement :

| Variable           | Défaut                    |
|--------------------|---------------------------|
| `PORT`             | `8080`                    |
| `SC_DEMO_EMAIL`    | `etudiant@smartcampus.ma` |
| `SC_DEMO_PASSWORD` | `Campus2026!`             |

## API

| Méthode | Route         | Description                                       |
|---------|---------------|---------------------------------------------------|
| GET     | `/api/health` | État de l'application                             |
| POST    | `/api/login`  | `email=...&password=...`, renvoie un jeton        |
| GET     | `/api/me`     | Utilisateur connecté (`Authorization: Bearer ...`) |
| POST    | `/api/logout` | Déconnexion (`Authorization: Bearer ...`)          |

Exemple :

```bash
curl -d "email=etudiant@smartcampus.ma&password=Campus2026!" http://localhost:8080/api/login
```

Après trois échecs consécutifs, le compte est verrouillé 15 minutes (SC-13).
