# Task Management System - Backend

## 📋 Description

Backend REST API pour un système de gestion de tâches développé avec Spring Boot. Ce système permet de gérer des tâches, des projets, des utilisateurs avec des fonctionnalités avancées comme les notifications en temps réel via WebSocket.

## 🚀 Fonctionnalités

- **Authentification JWT** - Sécurité robuste avec tokens JWT
- **Gestion des Tâches** - CRUD complet avec filtres et recherche
- **Gestion des Projets** - Organisation des tâches par projets
- **Système de Commentaires** - Collaboration sur les tâches
- **Notifications en Temps Réel** - WebSocket pour les notifications
- **Gestion des Utilisateurs** - Administration des comptes
- **API RESTful** - Interface standardisée
- **Documentation Swagger** - Documentation interactive de l'API

## 🛠️ Technologies Utilisées

- **Spring Boot 3.1.0**
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - Persistance des données
- **JWT** - Gestion des tokens
- **WebSocket** - Notifications temps réel
- **MySQL/H2** - Base de données
- **Maven** - Gestion des dépendances
- **Lombok** - Réduction du code boilerplate

## 📦 Installation et Configuration

### Prérequis
- Java 17+
- Maven 3.6+
- MySQL 8.0+ (ou H2 pour les tests)

### Étapes d'installation

1. **Cloner le projet**
```bash
git clone <repository-url>
cd task-management-backend
```

2. **Configurer la base de données**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/task_management
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Installer les dépendances**
```bash
mvn clean install
```

4. **Lancer l'application**
```bash
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## 🔑 Utilisateurs par Défaut

- **Admin**: `admin` / `admin123`
- **User**: `user` / `user123`

## 📚 API Endpoints

### Authentification
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion
- `POST /api/auth/logout` - Déconnexion

### Tâches
- `GET /api/tasks` - Liste des tâches (avec filtres)
- `POST /api/tasks` - Créer une tâche
- `GET /api/tasks/{id}` - Détails d'une tâche
- `PUT /api/tasks/{id}` - Modifier une tâche
- `DELETE /api/tasks/{id}` - Supprimer une tâche
- `GET /api/tasks/my-tasks` - Mes tâches
- `GET /api/tasks/overdue` - Tâches en retard
- `GET /api/tasks/statistics` - Statistiques

### Projets
- `GET /api/projects` - Liste des projets
- `POST /api/projects` - Créer un projet
- `GET /api/projects/{id}` - Détails d'un projet
- `PUT /api/projects/{id}` - Modifier un projet
- `DELETE /api/projects/{id}` - Supprimer un projet

### Utilisateurs
- `GET /api/users` - Liste des utilisateurs (admin)
- `GET /api/users/active` - Utilisateurs actifs
- `GET /api/users/profile` - Profil utilisateur
- `PUT /api/users/{id}` - Modifier un utilisateur
- `POST /api/users/change-password` - Changer le mot de passe

### Commentaires
- `POST /api/comments` - Ajouter un commentaire
- `GET /api/comments/task/{taskId}` - Commentaires d'une tâche
- `PUT /api/comments/{id}` - Modifier un commentaire
- `DELETE /api/comments/{id}` - Supprimer un commentaire

## 🔒 Sécurité

- **JWT Authentication** - Tokens sécurisés avec expiration
- **Role-based Access Control** - Permissions par rôles (USER, ADMIN)
- **Password Encryption** - BCrypt pour les mots de passe
- **CORS Configuration** - Sécurité cross-origin

## 📡 WebSocket

Endpoint WebSocket: `/ws`

### Notifications
- Assignation de tâches
- Changement de statut
- Nouveaux commentaires
- Rappels d'échéances

## 🏗️ Architecture

```
src/main/java/com/taskmanagement/
├── config/          # Configurations Spring
├── controller/      # Contrôleurs REST
├── dto/            # Objects de transfert de données
├── entity/         # Entités JPA
├── exception/      # Gestion des exceptions
├── repository/     # Repositories JPA
├── security/       # Configuration sécurité
├── service/        # Logique métier
└── util/           # Utilitaires
```

## 🧪 Tests

```bash
# Lancer tous les tests
mvn test

# Tests avec profil de développement
mvn test -Dspring.profiles.active=dev
```

## 📊 Monitoring et Logs

Les logs sont configurés avec différents niveaux :
- `DEBUG` pour le développement
- `INFO` pour la production
- `ERROR` pour les erreurs critiques

## 🔄 Développement

### Profils Spring
- `dev` - Développement avec H2
- `prod` - Production avec MySQL

### Base de données H2 (développement)
Console H2 accessible sur : `http://localhost:8080/h2-console`

## 📝 Licence

Ce projet est sous licence MIT.
