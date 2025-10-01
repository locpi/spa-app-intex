package fr.loicpincon.spaapp.model;

/**
 * Statut d'une session de spa
 */
public enum SessionStatus {
    SCHEDULED("Programmée"),
    ACTIVE("En cours"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Vérifie si la session est en cours (active)
     */
    public boolean isRunning() {
        return this == ACTIVE;
    }

    /**
     * Vérifie si la session est terminée (complète ou annulée)
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * Vérifie si la session peut être démarrée
     */
    public boolean canStart() {
        return this == SCHEDULED;
    }

    /**
     * Vérifie si la session peut être annulée
     */
    public boolean canCancel() {
        return this == SCHEDULED || this == ACTIVE;
    }
}