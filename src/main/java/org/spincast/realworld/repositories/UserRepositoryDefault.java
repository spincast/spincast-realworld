package org.spincast.realworld.repositories;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.spincast.plugins.jdbc.JdbcQueries;
import org.spincast.plugins.jdbc.JdbcUtils;
import org.spincast.plugins.jdbc.SpincastResultSet;
import org.spincast.plugins.jdbc.statements.DeleteStatement;
import org.spincast.plugins.jdbc.statements.InsertStatement;
import org.spincast.plugins.jdbc.statements.ResultSetHandler;
import org.spincast.plugins.jdbc.statements.SelectStatement;
import org.spincast.plugins.jdbc.statements.UpdateStatement;
import org.spincast.realworld.models.EntityFactory;
import org.spincast.realworld.models.users.Profile;
import org.spincast.realworld.models.users.User;
import org.spincast.realworld.services.UserService;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserRepositoryDefault extends BaseRepository implements UserRepository {

    @Inject
    public UserRepositoryDefault(Provider<DataSource> dataSource,
                                 JdbcUtils jdbcUtils,
                                 EntityFactory entityFactory,
                                 UserService userService) {
        super(dataSource, jdbcUtils, entityFactory, userService);
    }

    @Override
    public User saveUser(User user) {

        return getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<User>() {

            @Override
            public User run(Connection connection) {

                InsertStatement stm = getJdbcUtils().statements().createInsertStatement(connection);

                stm.sql("INSERT INTO users( email,  username,  hashed_password,  password_salt,  bio,  image) " +
                        "VALUES           (:email, :username, :hashed_password, :password_salt, :bio, :image) ");

                stm.setString("email", user.getEmail());
                stm.setString("username", user.getUsername());
                stm.setString("hashed_password", user.getHashedPassword());
                stm.setString("password_salt", user.getPasswordSalt());
                stm.setString("bio", user.getBio());
                stm.setString("image", user.getImage());

                long generatedId = stm.insertGetGeneratedId("id");

                User savedUser = getEntityFactory().createUser(generatedId,
                                                               user.getEmail(),
                                                               user.getUsername(),
                                                               user.getHashedPassword(),
                                                               user.getPasswordSalt(),
                                                               user.getBio(),
                                                               user.getImage());
                return savedUser;
            }
        });
    }

    @Override
    public User getUserById(long userId) {
        return getFirstUserOrNull(getUser(Sets.newHashSet(userId), null, null));
    }

    @Override
    public User getUserByEmail(String email) {
        return getFirstUserOrNull(getUser(null, email, null));
    }

    @Override
    public User getUserByUsername(String username) {
        return getFirstUserOrNull(getUser(null, null, username));
    }

    @Override
    public List<User> getUsers(Set<Long> userIds) {
        return getUser(userIds, null, null);
    }

    protected User getFirstUserOrNull(List<User> users) {
        if (users == null || users.size() == 0) {
            return null;
        }
        return users.get(0);
    }

    protected List<User> getUser(Set<Long> ids, String email, String username) {

        if ((ids == null || ids.size() == 0) && email == null && username == null) {
            throw new RuntimeException("The ids, email or username must be specified");
        }

        List<User> users = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<List<User>>() {

            @Override
            public List<User> run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT id, email, username, hashed_password, password_salt, bio, image " +
                        "FROM users " +
                        "WHERE 1=1 ");

                if (ids != null && ids.size() > 0) {
                    stm.sql("AND id IN(:ids) ");
                    stm.setInLong("ids", ids);
                }

                if (email != null) {
                    stm.sql("AND email = :email ");
                    stm.setString("email", email);
                }

                if (username != null) {
                    stm.sql("AND username = :username ");
                    stm.setString("username", username);
                }

                return stm.selectList(new ResultSetHandler<User>() {

                    @Override
                    public User handle(SpincastResultSet rs) throws Exception {

                        User user = getEntityFactory().createUser(rs.getLongOrNull("id"),
                                                                  rs.getString("email"),
                                                                  rs.getString("username"),
                                                                  rs.getString("hashed_password"),
                                                                  rs.getString("password_salt"),
                                                                  rs.getString("bio"),
                                                                  rs.getString("image"));
                        return user;
                    }
                });
            }
        });

        if (users == null) {
            users = new ArrayList<>();
        }
        return users;
    }

    @Override
    public void updateUser(User user) {
        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                UpdateStatement stm = getJdbcUtils().statements().createUpdateStatement(connection);

                stm.sql("UPDATE users " +
                        "SET email = :email, " +
                        "   username = :username, " +
                        "   hashed_password = :hashed_password, " +
                        "   password_salt = :password_salt, " +
                        "   bio = :bio, " +
                        "   image = :image " +
                        "WHERE " +
                        "   id = :id");

                stm.setLong("id", user.getId());
                stm.setString("email", user.getEmail());
                stm.setString("username", user.getUsername());
                stm.setString("hashed_password", user.getHashedPassword());
                stm.setString("password_salt", user.getPasswordSalt());
                stm.setString("bio", user.getBio());
                stm.setString("image", user.getImage());

                stm.update();

                return null;
            }
        });
    }

    @Override
    public boolean isFollowingById(long sourceUserId, long targetUserId) {
        Map<Long, Boolean> followingsByUsers = getFollowingsByUsers(sourceUserId, Sets.newHashSet(targetUserId));
        return followingsByUsers.get(targetUserId);
    }

    @Override
    public Map<Long, Boolean> getFollowingsByUsers(long sourceUserId, Set<Long> targetUsersIds) {

        final Map<Long, Boolean> followings = new HashMap<>();

        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT target_user_id " +
                        "FROM followings " +
                        "WHERE source_user_id = :source_user_id " +
                        "AND target_user_id IN(:targetUsersIds) ");

                stm.setLong("source_user_id", sourceUserId);
                stm.setInLong("targetUsersIds", targetUsersIds);

                stm.selectList(new ResultSetHandler<Void>() {

                    @Override
                    public Void handle(SpincastResultSet rs) throws Exception {
                        followings.put(rs.getLongOrNull("target_user_id"), true);
                        return null;
                    }
                });
                return null;
            }
        });

        for (Long targetUserId : targetUsersIds) {
            if (!followings.containsKey(targetUserId)) {
                followings.put(targetUserId, false);
            }
        }

        return followings;
    }

    @Override
    public boolean isFollowingByUsername(long sourceUserId, String username) {

        Boolean following = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Boolean>() {

            @Override
            public Boolean run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT 1 " +
                        "FROM followings " +
                        "WHERE source_user_id = :source_user_id " +
                        "AND target_user_id = ( " +
                        "   SELECT id " +
                        "   FROM users " +
                        "   WHERE username = :username " +
                        ") ");
                stm.setLong("source_user_id", sourceUserId);
                stm.setString("username", username);

                return stm.selectOne(new ResultSetHandler<Boolean>() {

                    @Override
                    public Boolean handle(SpincastResultSet rs) throws Exception {
                        return true;
                    }
                });
            }
        });

        return following != null ? following : false;
    }

    @Override
    public void follow(long sourceUserId, long targetUserId) {
        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                InsertStatement stm = getJdbcUtils().statements().createInsertStatement(connection);

                stm.sql("INSERT INTO followings(source_user_id, target_user_id) " +
                        "VALUES (:source_user_id, :target_user_id) " +
                        "ON CONFLICT (source_user_id, target_user_id) DO NOTHING ");

                stm.setLong("source_user_id", sourceUserId);
                stm.setLong("target_user_id", targetUserId);

                stm.insert();

                return null;
            }
        });
    }

    @Override
    public void unfollow(long sourceUserId, long targetUserId) {
        getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Void>() {

            @Override
            public Void run(Connection connection) {

                DeleteStatement stm = getJdbcUtils().statements().createDeleteStatement(connection);

                stm.sql("DELETE FROM followings " +
                        "WHERE source_user_id = :source_user_id " +
                        "AND target_user_id = :target_user_id ");

                stm.setLong("source_user_id", sourceUserId);
                stm.setLong("target_user_id", targetUserId);

                stm.delete();

                return null;
            }
        });
    }

    @Override
    public Map<Long, Profile> getProfilesByUserIds(Set<Long> userIds, User currentUser) {

        Map<Long, Profile> profilesByUsersIds = new HashMap<Long, Profile>();
        if (userIds == null || userIds.size() == 0) {
            return profilesByUsersIds;
        }

        List<User> users = getUsers(userIds);
        Map<Long, Boolean> followings = currentUser != null ? getFollowingsByUsers(currentUser.getId(), userIds) : null;

        for (User user : users) {
            boolean following = currentUser != null ? followings.get(user.getId()) : false;
            Profile profile = getEntityFactory().createProfile(user, following);
            profilesByUsersIds.put(user.getId(), profile);
        }

        return profilesByUsersIds;
    }

    @Override
    public boolean isEmailTaken(String email) {

        Boolean taken = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Boolean>() {

            @Override
            public Boolean run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT 1 " +
                        "FROM users " +
                        "WHERE LOWER(email) = LOWER(:email) ");
                stm.setString("email", email);

                return stm.selectOne(new ResultSetHandler<Boolean>() {

                    @Override
                    public Boolean handle(SpincastResultSet rs) throws Exception {
                        return true;
                    }
                });
            }
        });

        return taken != null ? taken : false;
    }

    @Override
    public boolean isUsernameTaken(String username) {
        Boolean taken = getJdbcUtils().scopes().autoCommit(getDataSource(), new JdbcQueries<Boolean>() {

            @Override
            public Boolean run(Connection connection) {

                SelectStatement stm = getJdbcUtils().statements().createSelectStatement(connection);

                stm.sql("SELECT 1 " +
                        "FROM users " +
                        "WHERE LOWER(username) = LOWER(:username) ");
                stm.setString("username", username);

                return stm.selectOne(new ResultSetHandler<Boolean>() {

                    @Override
                    public Boolean handle(SpincastResultSet rs) throws Exception {
                        return true;
                    }
                });
            }
        });

        return taken != null ? taken : false;
    }
}
