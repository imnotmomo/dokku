package dev.coms4156.project.backend.model;

/**
 * User DTO for mock auth.
 */
@SuppressWarnings("PMD.DataClass")
public class User {
    private String username;
    private String password;     // mock only
    private String role;         // USER or ADMIN
    private String token;        // access token
    private String refreshToken; // refresh token

    public String getUsername() { return username; }
    public void setUsername(final String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(final String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(final String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(final String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(final String refreshToken) { this.refreshToken = refreshToken; }
}