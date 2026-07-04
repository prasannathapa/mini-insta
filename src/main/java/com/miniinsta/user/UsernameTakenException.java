package com.miniinsta.user;

/** Thrown when registration is attempted with a username that already exists. */
public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String username) {
        super("username already taken: @" + username);
    }
}
