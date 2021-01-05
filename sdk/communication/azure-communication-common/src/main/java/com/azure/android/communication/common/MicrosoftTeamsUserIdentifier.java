package com.azure.android.communication.common;

/**
 * Represents a Microsoft Teams user.
 */
public class MicrosoftTeamsUserIdentifier extends CommunicationIdentifier {
    private final String id;
    private final boolean isAnonymous;

    /**
     * Creates a {@link MicrosoftTeamsUserIdentifier} from the provided identifier string.
     * <p>
     * The same identifier string will be returned whenever {@link #getId()} is called.
     * @param teamsUserId Teams user identifier string
     */
    public MicrosoftTeamsUserIdentifier(String teamsUserId) {
        id = teamsUserId;
        isAnonymous = false;
    }

    /**
     * Creates a {@link MicrosoftTeamsUserIdentifier} from the provided identifier string.
     * Optionally specify whether this is an anonymous user.
     * <p>
     * The same identifier string will be returned whenever {@link #getId()} is called.
     *
     * @param teamsUserId Teams user identifier string
     * @param isAnonymous Set this to true if the user is anonymous, for example when joining a meeting with a share link.
     */
    public MicrosoftTeamsUserIdentifier(String teamsUserId, boolean isAnonymous) {
        id = teamsUserId;
        this.isAnonymous = isAnonymous;
    }

    /**
     * The identifier of the Microsoft Teams user. If the user isn't anonymous, the id is the AAD object id of the user.
     */
    public String getId() {
        return id;
    }

    /**
     * True if the user is anonymous, for example when joining a meeting with a share link.
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }
}
