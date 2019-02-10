package org.spincast.realworld.repositories;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;

public interface UserRepository {

    public User saveUser(User user);

    public User getUserById(long userId);

    public User getUserByEmail(String email);

    public User getUserByUsername(String username);

    public List<User> getUsers(Set<Long> userIds);

    public void updateUser(User user);

    public boolean isFollowingById(long sourceUserId, long targetUserId);

    public boolean isFollowingByUsername(long sourceUserId, String username);

    public Map<Long, Boolean> getFollowingsByUsers(long sourceUserId, Set<Long> targetUserids);

    public void follow(long sourceUserId, long targetUserId);

    public void unfollow(long sourceUserId, long targetUserId);

    public Map<Long, Profile> getProfilesByUserIds(Set<Long> userIds, User currentUser);

    public boolean isEmailTaken(String email);

    public boolean isUsernameTaken(String username);

}
