package com.flashcards.backend.flashcards.oauth;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_AVATAR_URL;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_EMAIL;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_ID;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GITHUB_ATTR_NAME;

public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get(GITHUB_ATTR_ID));
    }

    @Override
    public String getName() {
        return (String) attributes.get(GITHUB_ATTR_NAME);
    }

    @Override
    public String getEmail() {
        return (String) attributes.get(GITHUB_ATTR_EMAIL);
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get(GITHUB_ATTR_AVATAR_URL);
    }

    @Override
    public String getFirstName() {
        String name = getName();
        if (StringUtils.isNotBlank(name)) {
            String[] nameParts = name.split("\\s+");
            return nameParts.length > 0 ? nameParts[0] : null;
        }
        return null;
    }

    @Override
    public String getLastName() {
        String name = getName();
        if (StringUtils.isNotBlank(name)) {
            String[] nameParts = name.split("\\s+");
            return nameParts.length > 1 ? nameParts[nameParts.length - 1] : null;
        }
        return null;
    }
}