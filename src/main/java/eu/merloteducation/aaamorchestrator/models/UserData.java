/*
 *  Copyright 2023-2024 Dataport AöR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
