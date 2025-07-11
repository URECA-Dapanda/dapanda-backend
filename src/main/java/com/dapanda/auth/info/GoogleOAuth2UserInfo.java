package com.dapanda.auth.info;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	@Override
	public String getEmail() {

		return (String) attributes.get("email");
	}

	@Override
	public String getName() {

		return (String) attributes.get("name");
	}

}
