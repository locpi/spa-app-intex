#!/bin/bash

# Script pour générer le fichier de mots de passe MQTT
# Usage: ./generate-mqtt-password.sh

echo "Génération du fichier de mots de passe MQTT..."

# Créer le fichier passwd avec l'utilisateur spa_client
# Mot de passe: SpaM0tt0_2024!Complex#
docker run --rm -it eclipse-mosquitto:2.0 mosquitto_passwd -c -b /tmp/passwd spa_client "SpaM0tt0_2024!Complex#" > mosquitto-passwd

echo "Fichier mosquitto-passwd généré avec succès!"
echo "Utilisateur: spa_client"
echo "Mot de passe: SpaM0tt0_2024!Complex#"