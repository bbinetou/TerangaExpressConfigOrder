-- Script d'initialisation de la base de données PostgreSQL
-- Ce script est exécuté automatiquement au démarrage du conteneur PostgreSQL

-- Créer l'utilisateur pour order-service
CREATE USER orderuser WITH PASSWORD 'orderpass';

-- Créer la base de données order_db
CREATE DATABASE order_db;

-- Donner tous les privilèges à orderuser sur order_db
GRANT ALL PRIVILEGES ON DATABASE order_db TO orderuser;

-- Se connecter à order_db pour configurer les permissions
\c order_db

-- Donner les privilèges sur le schéma public
GRANT ALL ON SCHEMA public TO orderuser;

-- Donner les privilèges sur toutes les tables (présentes et futures)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO orderuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO orderuser;

-- Message de confirmation
SELECT 'Database order_db created and configured successfully!' AS status;
