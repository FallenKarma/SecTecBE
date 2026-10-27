package it.sectec.platform.identity.service;

public record AccountMail(String email, String purpose, String token) {
    @Override public String toString() { return "AccountMail[redacted]"; }
}

