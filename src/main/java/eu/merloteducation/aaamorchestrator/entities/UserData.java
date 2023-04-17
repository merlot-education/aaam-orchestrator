package eu.merloteducation.aaamorchestrator.entities;

import java.util.Arrays;

public class UserData {
    private String id;
    private String createdTimestamp;
    private String username;
    private String enabled;
    private String totp;
    private String emailVerified;
    private String firstName;
    private String lastName;
    private String email;
    private String disableableCredentialTypes;
    private String[] requiredActions;
    private String notBefore;

    @Override
    public String toString() {
        return "UserData{" +
                "id='" + id + '\'' +
                ", createdTimestamp='" + createdTimestamp + '\'' +
                ", username='" + username + '\'' +
                ", enabled='" + enabled + '\'' +
                ", totp='" + totp + '\'' +
                ", emailVerified='" + emailVerified + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", disableableCredentialTypes='" + disableableCredentialTypes + '\'' +
                ", requiredActions=" + Arrays.toString(requiredActions) +
                ", notBefore='" + notBefore + '\'' +
                '}';
    }
}
