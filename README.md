
Messagerie (Backend)
--------------------

Backend Spring Boot pour une application de messagerie (conversations, messages, fichiers, demandes d'amis, notifications).

Prérequis
---------
- Java 25
- Maven 3.9+

Démarrer l'application
----------------------
1. Build: mvn clean package
2. Lancer: mvn spring-boot:run
3. L'API est exposée sur http://localhost:8080

Authentification (mot de passe)
-------------------------------
Le backend utilise Spring Security avec HTTP Basic et une base d’utilisateurs stockée en base (H2).

- Inscription (publique):
  - POST /api/auth/register
  - Corps JSON: { "name": "Affichage", "username": "login", "password": "secret" }
  - Réponse: { "id": <userId> }

- Obtenir l’utilisateur courant (auth requis):
  - GET /api/auth/me
  - Ajoutez un header Authorization: Basic <base64(username:password)>

- Accès:
  - /api/auth/register et /h2-console/** sont publics (dev).
  - Tous les autres endpoints /api/** nécessitent une authentification HTTP Basic.

Notes
-----
- Les mots de passe sont stockés hashés (BCrypt).
- Swagger et Spring REST Docs ne sont pas inclus dans ce projet.
