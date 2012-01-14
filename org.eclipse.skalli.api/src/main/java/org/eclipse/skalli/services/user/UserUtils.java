package org.eclipse.skalli.services.user;

import org.eclipse.skalli.model.User;

public class UserUtils {

    // no instances, please!
    private UserUtils() {
    }

    /**
     * Returns the {@link User} matching the given <code>userId<code>-
     *
     * @param userId  the unique identifier of the user.
     * @return a user, or <code>null</code> if no known user matched
     * the given unique identifier.
     *
     * @throws IllegalStateException if no user service is available.
     */
    public static User getUser(String userId) {
        User user = null;
        if (userId != null) {
            UserService userService = UserServices.getUserService();
            user = userService.getUserById(userId.toString());
        }
        return user;
    }
}
