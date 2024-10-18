package events;

import account.Role;
import account.User;

public record GrantRoleEvent(User user, Role role) {}
