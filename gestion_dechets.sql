-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: gestion_dechets
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `collecte`
--

DROP TABLE IF EXISTS `collecte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collecte` (
  `id_collecte` int NOT NULL AUTO_INCREMENT,
  `date_collecte` datetime DEFAULT CURRENT_TIMESTAMP,
  `quantite_collectee` decimal(10,2) DEFAULT NULL,
  `id_intervention` int NOT NULL,
  `id_agent_collecteur` int NOT NULL,
  PRIMARY KEY (`id_collecte`),
  KEY `id_intervention` (`id_intervention`),
  KEY `id_agent_collecteur` (`id_agent_collecteur`),
  CONSTRAINT `collecte_ibfk_1` FOREIGN KEY (`id_intervention`) REFERENCES `intervention` (`id_intervention`),
  CONSTRAINT `collecte_ibfk_2` FOREIGN KEY (`id_agent_collecteur`) REFERENCES `utilisateur` (`id_utilisateur`),
  CONSTRAINT `collecte_chk_1` CHECK ((`quantite_collectee` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collecte`
--

LOCK TABLES `collecte` WRITE;
/*!40000 ALTER TABLE `collecte` DISABLE KEYS */;
/*!40000 ALTER TABLE `collecte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dechet`
--

DROP TABLE IF EXISTS `dechet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dechet` (
  `id_dechet` int NOT NULL AUTO_INCREMENT,
  `type_dechet` varchar(50) NOT NULL,
  `categorie` enum('menager','industriel','medical','agricole') DEFAULT 'menager',
  `toxicite` enum('non','faible','moyenne','elevee') DEFAULT 'non',
  `statut_tri` enum('non_trie','en_cours','trie','recycle','elimine') DEFAULT 'non_trie',
  `date_tri` datetime DEFAULT NULL,
  `id_collecte` int NOT NULL,
  `id_agent_tri` int DEFAULT NULL,
  PRIMARY KEY (`id_dechet`),
  KEY `id_collecte` (`id_collecte`),
  KEY `id_agent_tri` (`id_agent_tri`),
  CONSTRAINT `dechet_ibfk_1` FOREIGN KEY (`id_collecte`) REFERENCES `collecte` (`id_collecte`),
  CONSTRAINT `dechet_ibfk_2` FOREIGN KEY (`id_agent_tri`) REFERENCES `utilisateur` (`id_utilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dechet`
--

LOCK TABLES `dechet` WRITE;
/*!40000 ALTER TABLE `dechet` DISABLE KEYS */;
/*!40000 ALTER TABLE `dechet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `intervention`
--

DROP TABLE IF EXISTS `intervention`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `intervention` (
  `id_intervention` int NOT NULL AUTO_INCREMENT,
  `date_planification` datetime NOT NULL,
  `date_realisation` datetime DEFAULT NULL,
  `statut` enum('planifiee','en_cours','terminee','annulee') DEFAULT 'planifiee',
  `notes` text,
  `id_conducteur` int NOT NULL,
  `id_signalement` int NOT NULL,
  PRIMARY KEY (`id_intervention`),
  UNIQUE KEY `unique_signalement_intervention` (`id_signalement`),
  KEY `id_conducteur` (`id_conducteur`),
  CONSTRAINT `intervention_ibfk_1` FOREIGN KEY (`id_conducteur`) REFERENCES `utilisateur` (`id_utilisateur`),
  CONSTRAINT `intervention_ibfk_2` FOREIGN KEY (`id_signalement`) REFERENCES `signalement` (`id_signalement`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `intervention`
--

LOCK TABLES `intervention` WRITE;
/*!40000 ALTER TABLE `intervention` DISABLE KEYS */;
/*!40000 ALTER TABLE `intervention` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rapport_tri`
--

DROP TABLE IF EXISTS `rapport_tri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rapport_tri` (
  `id_rapport_tri` int NOT NULL AUTO_INCREMENT,
  `date_generation` datetime DEFAULT CURRENT_TIMESTAMP,
  `periode_debut` date NOT NULL,
  `periode_fin` date NOT NULL,
  `quantite_triee` decimal(10,2) DEFAULT NULL,
  `taux_recyclage` decimal(5,2) DEFAULT NULL,
  `id_agent_tri` int NOT NULL,
  PRIMARY KEY (`id_rapport_tri`),
  KEY `id_agent_tri` (`id_agent_tri`),
  CONSTRAINT `rapport_tri_ibfk_1` FOREIGN KEY (`id_agent_tri`) REFERENCES `utilisateur` (`id_utilisateur`),
  CONSTRAINT `rapport_tri_chk_1` CHECK ((`taux_recyclage` between 0 and 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rapport_tri`
--

LOCK TABLES `rapport_tri` WRITE;
/*!40000 ALTER TABLE `rapport_tri` DISABLE KEYS */;
/*!40000 ALTER TABLE `rapport_tri` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `signalement`
--

DROP TABLE IF EXISTS `signalement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `signalement` (
  `id_signalement` int NOT NULL AUTO_INCREMENT,
  `date_signalement` datetime DEFAULT CURRENT_TIMESTAMP,
  `adresse` varchar(200) NOT NULL,
  `description` text,
  `etat` enum('nouveau','en_cours','termine','annule') DEFAULT 'nouveau',
  PRIMARY KEY (`id_signalement`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `signalement`
--

LOCK TABLES `signalement` WRITE;
/*!40000 ALTER TABLE `signalement` DISABLE KEYS */;
/*!40000 ALTER TABLE `signalement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateur` (
  `id_utilisateur` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `adresse` varchar(200) NOT NULL,
  `role` enum('citoyen','conducteur','agent_tri','administrateur') NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `date_creation` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_utilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateur`
--

LOCK TABLES `utilisateur` WRITE;
/*!40000 ALTER TABLE `utilisateur` DISABLE KEYS */;
/*!40000 ALTER TABLE `utilisateur` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-03 14:23:46
