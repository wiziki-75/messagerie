
Documentation OpenAPI / Swagger
--------------------------------

L'application expose automatiquement la documentation OpenAPI et l'UI Swagger.

Après démarrage (par défaut sur http://localhost:8080), vous pouvez accéder à :

- UI Swagger: http://localhost:8080/swagger-ui.html
- Spécification OpenAPI (JSON): http://localhost:8080/v3/api-docs

Aucune authentification n'est requise pour ces endpoints dans cette version.

Notes de build
--------------

- Le projet utilise springdoc-openapi pour générer la doc.
- Si la résolution Maven échoue pour la dépendance springdoc, vérifiez votre connexion aux dépôts Maven Central et relancez le build.
