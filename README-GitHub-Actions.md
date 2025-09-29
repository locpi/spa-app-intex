# GitHub Actions - CI/CD Pipeline

## 🚀 Pipeline de déploiement automatique

Ce projet utilise GitHub Actions pour automatiser le build, la publication et le déploiement des images Docker.

### 📋 Workflows configurés

#### 1. `docker-build.yml` - Build et Publication
- **Déclenchement** : Push sur `main` ou `develop`, tags `v*`, et pull requests
- **Actions** :
  - Build des images Docker frontend et backend
  - Publication sur `registry.loicpincon.fr`
  - Support multi-architecture (AMD64 + ARM64)
  - Scan de sécurité avec Trivy
  - Mise à jour automatique du docker-compose

#### 2. `deploy.yml` - Déploiement Production
- **Déclenchement** : Après succès du workflow `docker-build` ou manuellement
- **Actions** :
  - Déploiement automatique sur le serveur de production
  - Health checks des services
  - Rollback automatique en cas d'échec

### 🐳 Images Docker

Les images sont publiées sur votre registry privé :
- **Frontend** : `registry.loicpincon.fr/spa-frontend:latest`
- **Backend** : `registry.loicpincon.fr/spa-api:latest`

### 🔧 Configuration requise

#### Secrets GitHub à configurer

Dans votre repository GitHub, ajouter les secrets suivants dans `Settings > Secrets and variables > Actions` :

```bash
# Registry Docker
REGISTRY_USERNAME=your_registry_username
REGISTRY_PASSWORD=your_registry_password

# Déploiement
DEPLOY_HOST=your_server_ip_or_domain
DEPLOY_USER=your_ssh_username
DEPLOY_PATH=/path/to/deployment/directory
SSH_PRIVATE_KEY=your_ssh_private_key
```

#### Configuration SSH pour le déploiement

1. **Générer une clé SSH** (si pas déjà fait) :
   ```bash
   ssh-keygen -t ed25519 -C "github-actions"
   ```

2. **Ajouter la clé publique au serveur** :
   ```bash
   # Sur le serveur de déploiement
   echo "your_public_key" >> ~/.ssh/authorized_keys
   ```

3. **Ajouter la clé privée aux secrets GitHub** :
   - Copier le contenu de la clé privée dans le secret `SSH_PRIVATE_KEY`

### 🏗️ Structure du déploiement

#### Fichiers de configuration
- `docker-compose.yml` : Développement local avec build
- `docker-compose.prod.yml` : Production avec images du registry
- `.dockerignore` : Exclusions pour les builds Docker

#### Domaines configurés
- `spa.loicpincon.fr` : Application frontend
- `spa-api.loicpincon.fr` : API backend
- `mqtt.loicpincon.fr` : WebSocket MQTT
- `traefik.loicpincon.fr` : Dashboard Traefik
- `portainer.loicpincon.fr` : Monitoring Docker

### 📈 Fonctionnalités avancées

#### Multi-architecture
- Support AMD64 et ARM64 (compatible Raspberry Pi)
- Cache des layers Docker pour builds rapides

#### Sécurité
- Scan automatique des vulnérabilités avec Trivy
- Rapports de sécurité dans GitHub Security tab
- Images optimisées avec utilisateurs non-root

#### Monitoring
- Health checks automatiques après déploiement
- Logs détaillés des services
- Rollback automatique en cas d'échec

### 🚀 Utilisation

#### Déploiement automatique
1. Push du code sur la branche `main`
2. GitHub Actions build et publie les images
3. Déploiement automatique sur le serveur
4. Vérification des services

#### Déploiement manuel
```bash
# Via GitHub Actions (onglet Actions du repo)
# Ou via API GitHub
curl -X POST \
  -H "Accept: application/vnd.github.v3+json" \
  -H "Authorization: token YOUR_TOKEN" \
  https://api.github.com/repos/USER/REPO/actions/workflows/deploy.yml/dispatches \
  -d '{"ref":"main"}'
```

#### Version taggée
```bash
git tag v1.0.0
git push origin v1.0.0
```

### 🔍 Monitoring du déploiement

#### Logs en temps réel
```bash
# Sur le serveur de production
cd /path/to/deployment
docker-compose logs -f spa-api spa-frontend
```

#### Vérifications manuelles
```bash
# Status des services
docker-compose ps

# Health check de l'API
curl https://spa-api.loicpincon.fr/actuator/health

# Test du frontend
curl https://spa.loicpincon.fr
```

### 🛠️ Développement local

#### Build local des images
```bash
# Frontend
docker build -t spa-frontend ./app-spa-frontend

# Backend
docker build -t spa-api ./spa-app-back
```

#### Test avec le registry
```bash
# Login au registry
docker login registry.loicpincon.fr

# Pull et test des images
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

### 📊 Métriques et optimisations

- **Build time** : ~3-5 minutes par image
- **Registry size** : Frontend ~100MB, Backend ~300MB
- **Déploiement** : ~2-3 minutes avec health checks
- **Rollback** : ~1 minute

### 🔒 Sécurité et bonnes pratiques

✅ Images basées sur Alpine Linux (légères et sécurisées)
✅ Utilisateurs non-root dans les containers
✅ Scan de vulnérabilités automatique
✅ Secrets chiffrés dans GitHub
✅ HTTPS obligatoire avec certificats auto-renouvelés
✅ Isolation réseau avec Docker networks