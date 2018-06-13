package HouseIt.dal.impl;

import HouseIt.dal.IUserDao;
import HouseIt.model.User;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements IUserDao {

    public User findByUsername(String username) {
        return (User) getCurrentSession().createCriteria(User.class)
                .add(Restrictions.eq("user.username", username));
    }

    public void deleteUser(User user) {
        getCurrentSession().delete(user);
    }

}