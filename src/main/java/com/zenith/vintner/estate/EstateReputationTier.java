package com.zenith.vintner.estate;

public enum EstateReputationTier {
    NEW_ESTATE(0),
    ESTABLISHED(25),
    RESPECTED(60),
    RENOWNED(110),
    CELEBRATED(170);

    private final int minimumScore;

    EstateReputationTier(int minimumScore) {
        this.minimumScore = minimumScore;
    }

    public int minimumScore() {
        return minimumScore;
    }

    public String translationKey() {
        return "estate_reputation.vintner."
                + name().toLowerCase(java.util.Locale.ROOT);
    }

    public EstateReputationTier next() {
        EstateReputationTier[] tiers = values();
        int next = ordinal() + 1;
        return next < tiers.length ? tiers[next] : null;
    }

    public static EstateReputationTier forScore(int score) {
        EstateReputationTier result = NEW_ESTATE;
        for (EstateReputationTier tier : values()) {
            if (score >= tier.minimumScore) {
                result = tier;
            }
        }
        return result;
    }
}
