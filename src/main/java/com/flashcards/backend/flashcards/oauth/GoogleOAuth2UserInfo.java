package com.flashcards.backend.flashcards.oauth;

import java.util.Map;

import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_EMAIL;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_FAMILY_NAME;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_GIVEN_NAME;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_NAME;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_PICTURE;
import static com.flashcards.backend.flashcards.constants.AuthConstants.GOOGLE_ATTR_SUB;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get(GOOGLE_ATTR_SUB);
    }

    @Override
    public String getName() {
        return (String) attributes.get(GOOGLE_ATTR_NAME);
    }

    @Override
    public String getEmail() {
        return (String) attributes.get(GOOGLE_ATTR_EMAIL);
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get(GOOGLE_ATTR_PICTURE);
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get(GOOGLE_ATTR_GIVEN_NAME);
    }

    @Override
    public String getLastName() {
        return (String) attributes.get(GOOGLE_ATTR_FAMILY_NAME);
    }
}