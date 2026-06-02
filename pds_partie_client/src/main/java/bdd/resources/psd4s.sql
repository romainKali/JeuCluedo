-- phpMyAdmin SQL Dump
-- version 4.9.3
-- https://www.phpmyadmin.net/
-- Host: localhost:3306
-- Generation Time: Feb 28, 2025 at 10:31 AM
-- Server version: 5.7.26
-- PHP Version: 7.4.2

DROP DATABASE IF EXISTS `psd4s`;


CREATE DATABASE `psd4s`;
USE `psd4s`;


CREATE TABLE `joueur` (
                          `id` int(11) NOT NULL AUTO_INCREMENT,
                          `pseudo` varchar(256) NOT NULL UNIQUE,
                          `parties_jouees` int(11) DEFAULT 0,
                          PRIMARY KEY (`id`)
);


CREATE TABLE `carte` (
                         `id_carte` int(11) NOT NULL,
                         `nom_carte` varchar(50) NOT NULL,
                         `type_carte` varchar(20) NOT NULL,
                         PRIMARY KEY (`id_carte`)
);


CREATE TABLE `indice` (
                          `id_carte` int(11) NOT NULL,
                          `proprio` int(11) NOT NULL,
                          `soupconneur` int(11) NOT NULL,
                          `possede` int(11) NOT NULL DEFAULT 2,
                          PRIMARY KEY (`id_carte`, `proprio`, `soupconneur`),
                          FOREIGN KEY (`id_carte`) REFERENCES `carte`(`id_carte`) ON DELETE CASCADE,
                          FOREIGN KEY (`proprio`) REFERENCES `joueur`(`id`) ON DELETE CASCADE,
                          FOREIGN KEY (`soupconneur`) REFERENCES `joueur`(`id`) ON DELETE CASCADE
);


INSERT IGNORE INTO `carte` (`id_carte`, `nom_carte`, `type_carte`) VALUES
(1, 'Mademoiselle_Rose', 'Personnage'),
(2, 'Colonel_Moutarde', 'Personnage'),
(3, 'Madame_Leblanc', 'Personnage'),
(4, 'Reverend_Olive', 'Personnage'),
(5, 'Madame_Pervenche', 'Personnage'),
(6, 'Professeur_Violet', 'Personnage'),
(7, 'Poignard', 'Arme'),
(8, 'Revolver', 'Arme'),
(9, 'Chandelier', 'Arme'),
(10, 'Corde', 'Arme'),
(11, 'Cle_anglaise', 'Arme'),
(12, 'Matraque', 'Arme'),
(13, 'Bureau', 'Lieu'),
(14, 'Bibliotheque', 'Lieu'),
(15, 'Salle_de_billard', 'Lieu'),
(16, 'Veranda', 'Lieu'),
(17, 'Salle_de_bal', 'Lieu'),
(18, 'Hall', 'Lieu'),
(19, 'Salon', 'Lieu'),
(20, 'Salle_a_manger', 'Lieu'),
(21, 'Cuisine', 'Lieu');