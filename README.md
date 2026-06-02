# 🕵️‍♂️ Jeu Cluedo - Projet de Synthèse (Architecture Client-Serveur)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)

Ce projet est une adaptation multijoueur du célèbre jeu de société **Cluedo**. Il a été développé dans le cadre du projet de synthèse de L2 Informatique à l'Université de Lorraine.

Il repose sur une **architecture Client-Serveur robuste**, gérant la logique métier du jeu en réseau, couplée à une interface graphique interactive et une persistance des données en base de données.

---

## 🏗️ Architecture du Projet

Le dépôt est divisé en deux parties principales :

* 📁 **`psd4s` (Serveur & Logique Métier)** : Contient le cœur du jeu, la gestion des threads pour accepter les connexions (`ThreadAcceptConnexion`, `ThreadConnexion`), la logique de déplacement sur le plateau (`Superviseur`, `PlateauCluedo`), et la validation des règles strictes (soupçons, réfutations, accusations).
* 📁 **`pds_partie_client` (Client & Interface Graphique)** : Gère l'affichage pour chaque joueur en utilisant **JavaFX** (avec des vues définies en `.fxml` et stylisées en `.css`). Il gère également le design sonore et la persistance du bloc-notes du joueur via JDBC.

### 🧠 Le Design Pattern "Expert"
La communication réseau repose sur un **Design Pattern Expert** (Chain of Responsibility). Lorsqu'un message est reçu via les sockets, il parcourt une chaîne d'experts (`ExpertDeplacer`, `ExpertSoupconner`, `ExpertMessagePublic`, etc.) jusqu'à ce que l'expert compétent le traite. Cela garantit un code modulaire et facile à maintenir.

---

## ✨ Fonctionnalités Principales

* 🌐 **Multijoueur en ligne :** Système de gestion des tours via des Sockets TCP.
* 🎲 **Mécaniques complètes du Cluedo :** Lancer de dés, déplacement case par case en fonction des résultats, et interaction avec les salles.
* 💬 **Système de Chat intégré :** Possibilité d'envoyer des messages publics à tous les joueurs ou des messages privés.
* 📝 **Carnet de notes persistant :** Sauvegarde automatique des indices du joueur grâce à une base de données **SQL** via JDBC (avec prévention des injections SQL).
* 🔊 **Design Sonore :** Intégration de bruitages d'ambiance pour une meilleure immersion.

---

## 🛠️ Technologies Utilisées

* **Langage :** Java (POO avancée, Multithreading, Sockets)
* **Interface Graphique :** JavaFX (FXML, CSS)
* **Base de données :** SQL (via le pilote MySQL JDBC)
* **Outils & Versioning :** Git, Maven (`pom.xml`)
* **Tests :** Implémentation de tests d'intégration réseau et de logique métier avec JUnit (`TestAccuse`, `TestDeplacements`, `TestRefutation`, etc.).

---

## 🚀 Installation & Exécution

### 1. Prérequis
* Avoir **Java 17** (ou supérieur) installé.
* Avoir **Maven** installé.
* Un serveur **MySQL** (ex: XAMPP, WAMP, ou serveur local) pour la base de données.

### 2. Configuration de la Base de Données
1. Lancez votre serveur SQL.
2. Exécutez le script SQL fourni dans les ressources pour créer les tables nécessaires.
3. Modifiez les identifiants de connexion dans le fichier manager du client si nécessaire.

### 3. Lancer le Serveur
Placez-vous dans le répertoire du serveur et compilez le projet :
```bash
cd psd4s
mvn clean compile
