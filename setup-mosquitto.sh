#!/bin/bash

# Script pour configurer les utilisateurs MQTT

echo "Configuration des utilisateurs MQTT..."

# Créer le répertoire de configuration s'il n'existe pas
mkdir -p ./mosquitto/config
mkdir -p ./mosquitto/data
mkdir -p ./mosquitto/log

# Créer le fichier de mot de passe
# Utilisateur pour l'application backend
mosquitto_passwd -c ./mosquitto/config/passwd spa_client
echo "Entrez le mot de passe pour spa_client: spa_mqtt_2024!"

# Utilisateur pour le spa hardware
mosquitto_passwd ./mosquitto/config/passwd spa_device
echo "Entrez le mot de passe pour spa_device: device_secret_2024!"

# Permissions sur les fichiers
chmod 600 ./mosquitto/config/passwd
chmod 644 ./mosquitto/config/mosquitto.conf
chmod 644 ./mosquitto/config/acl

# Créer le fichier acme.json pour Traefik
mkdir -p ./traefik
touch ./traefik/acme.json
chmod 600 ./traefik/acme.json

echo "Configuration terminée!"
echo "Vous pouvez maintenant lancer: docker-compose up -d"