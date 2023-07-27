package eu.merloteducation.aaamorchestrator.models;

import com.fasterxml.jackson.annotation.JsonView;
import eu.merloteducation.aaamorchestrator.views.Views;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserData {
    @JsonView(Views.UserDataView.class)
    private String id;

    @JsonView(Views.UserDataView.class)
    private long createdTimestamp;

    @JsonView(Views.UserDataView.class)
    private String username;

    @JsonView(Views.UserDataView.class)
    private boolean enabled;

    private boolean totp;

    private boolean emailVerified;

    @JsonView(Views.UserDataView.class)
    private String firstName;

    @JsonView(Views.UserDataView.class)
    private String lastName;

    @JsonView(Views.UserDataView.class)
    private String email;

    @JsonView(Views.UserDataView.class)
    private Map<String, List<String>> attributes;

    private List<String> disableableCredentialTypes;

    private List<String> requiredActions;

    private long notBefore;

    @JsonView(Views.UserDataView.class)
    private String orgaRole;

}
