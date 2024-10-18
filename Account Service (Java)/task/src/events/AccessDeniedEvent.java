package events;

import account.User;

public record AccessDeniedEvent(User user) {}
