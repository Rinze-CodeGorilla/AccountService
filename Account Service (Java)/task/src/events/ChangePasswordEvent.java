package events;

import account.User;

public record ChangePasswordEvent(User user) {}
