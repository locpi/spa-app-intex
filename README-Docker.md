# Configuration Docker pour l'application Spa

## Architecture

L'application est déployée avec Docker Compose et utilise Traefik comme reverse proxy avec SSL automatique via Let's Encrypt.

### Services déployés

- **spa.loicpincon.fr** : Frontend Angular
- **spa-api.loicpincon.fr** : API Spring Boot
- **mqtt.loicpincon.fr** : WebSocket MQTT (port 9001)
- **traefik.loicpincon.fr** : Dashboard Traefik (optionnel)

## Configuration initiale

### 1. Prérequis sur le Raspberry Pi

```bash
# Installer Docker et Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Installer Docker Compose
sudo apt-get update
sudo apt-get install docker-compose-plugin
```

### 2. Configuration DNS

Configurer les enregistrements DNS pour pointer vers votre IP publique :

```
spa.loicpincon.fr       A    VOTRE_IP_PUBLIQUE
spa-api.loicpincon.fr   A    VOTRE_IP_PUBLIQUE
mqtt.loicpincon.fr      A    VOTRE_IP_PUBLIQUE
traefik.loicpincon.fr   A    VOTRE_IP_PUBLIQUE
```

### 3. Configuration des ports sur votre box

Rediriger les ports suivants vers votre Raspberry Pi :
- Port 80 → 80 (HTTP)
- Port 443 → 443 (HTTPS)

### 4. Setup initial

```bash
# Cloner/copier le projet
cd /path/to/spa-project

# Configuration MQTT
./setup-mosquitto.sh

# Modifier les variables d'environnement si nécessaire
nano docker-compose.yml
```

## Démarrage

```bash
# Démarrer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Voir le statut
docker-compose ps
```

## Configuration MQTT

### Utilisateurs configurés

- **spa_client** : Pour l'application backend
- **spa_device** : Pour le hardware du spa (ESP32/Arduino)

### Topics MQTT

- `spa/command/+` : Commandes vers le spa
- `spa/status/+` : Statut du spa
- `spa/temperature/+` : Données de température

## SSL/HTTPS

Les certificats SSL sont automatiquement générés et renouvelés par Let's Encrypt via Traefik.

## Monitoring

### Logs

```bash
# Logs de tous les services
docker-compose logs -f

# Logs d'un service spécifique
docker-compose logs -f spa-api
docker-compose logs -f traefik
```

### Health checks

```bash
# Vérifier l'état des services
curl https://spa-api.loicpincon.fr/actuator/health
curl https://spa.loicpincon.fr
```

## Maintenance

### Mise à jour

```bash
# Reconstruire et redémarrer
docker-compose build --no-cache
docker-compose up -d

# Ou avec Watchtower (automatique)
# Les mises à jour se font automatiquement toutes les heures
```

### Sauvegarde

```bash
# Sauvegarder la base de données
docker-compose exec postgres pg_dump -U spa_user spa_db > backup_$(date +%Y%m%d).sql

# Sauvegarder les volumes
docker run --rm -v spa_postgres_data:/data -v $(pwd):/backup ubuntu tar czf /backup/postgres_backup.tar.gz /data
```

### Restauration

```bash
# Restaurer la base de données
cat backup_20241201.sql | docker-compose exec -T postgres psql -U spa_user -d spa_db
```

## Sécurité

### Recommendations

1. Changer les mots de passe par défaut dans `docker-compose.yml`
2. Configurer un firewall sur le Raspberry Pi
3. Activer la surveillance des logs
4. Sauvegarder régulièrement les données

### Firewall basique

```bash
# Installer ufw
sudo apt install ufw

# Règles de base
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80
sudo ufw allow 443

# Activer
sudo ufw enable
```

## Troubleshooting

### Problèmes courants

1. **Certificats SSL non générés** : Vérifier que les DNS pointent bien vers votre IP
2. **Services inaccessibles** : Vérifier les redirections de ports sur votre box
3. **MQTT ne fonctionne pas** : Vérifier les mots de passe avec `./setup-mosquitto.sh`

### Debug

```bash
# Vérifier la configuration Traefik
docker-compose exec traefik traefik version

# Tester la connectivité MQTT
mosquitto_pub -h mqtt.loicpincon.fr -p 1883 -u spa_client -P spa_mqtt_2024! -t spa/test -m "hello"
```