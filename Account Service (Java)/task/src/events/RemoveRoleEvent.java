package events;

import account.Role;
import account.User;

public record RemoveRoleEvent(User user, Role role) {}
