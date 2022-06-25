package com.mcmiddleearth.mcmescripts;

public enum ConfigKeys {

    DRIVE_ACCESS_TOKEN ("drive.accessToken"),
    DRIVE_REFRESH_TOKEN ("drive.refreshToken"),
    DRIVE_FOLDER_ANIMATIONS ("drive.animationsFolder"),
    DRIVE_FOLDER_ENTITIES ("drive.entitiesFolder"),
    DRIVE_FOLDER_SCRIPTS ("drive.scriptsFolder"),
    DRIVE_CLIENT_ID ("drive.clientId"),
    DRIVE_CLIENT_SECRET("drive.clientSecret"),
    SCRIPT_CHECKER_PERIOD ("scriptCheckerPeriod"),
    TRIGGER_CHECKER_PERIOD ("triggerCheckerPeriod"),
    START_UP_DELAY ("startUpDelay"),
    QUEST_LIFETIME ("quest.lifetime");

    private final String key;

    ConfigKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
