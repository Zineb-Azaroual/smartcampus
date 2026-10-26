package com.smartcampus.auth;

public class LoginService {
	public boolean login(String username, String password) {
		if (username.isEmpty() || password.isEmpty())
			return false;

		return true;
	}
}
