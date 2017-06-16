
package app.users;

import org.neogroup.sparks.model.EntityQuery;
import org.neogroup.sparks.processors.crud.CRUDProcessor;

import java.util.*;

public class UserCRUDProcessor extends CRUDProcessor<User> {

    private Map<Integer, User> users;
    private int nextId;

    public UserCRUDProcessor() {
        this.users = new HashMap<>();
        nextId = 1;
    }

    @Override
    protected User create(User entity, Map<String, Object> params) {
        entity.setId(nextId++);
        users.put(entity.getId(), entity);
        return entity;
    }

    @Override
    protected User update(User entity, Map<String, Object> params) {
        users.put(entity.getId(), entity);
        return entity;
    }

    @Override
    protected User delete(User entity, Map<String, Object> params) {
        users.remove(entity);
        return entity;
    }

    @Override
    protected Collection<User> retrieve(EntityQuery query, Map<String, Object> params) {
        List<User> usersList = new ArrayList<User>(users.values());
        return usersList;
    }
}
