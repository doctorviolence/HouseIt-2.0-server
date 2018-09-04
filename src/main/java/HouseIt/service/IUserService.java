package HouseIt.service;

import HouseIt.entities.User;
import HouseIt.exception.MyEntityNotFoundException;

public interface IUserService {

    User createUser(User user);

    void updateUser(User user) throws MyEntityNotFoundException;

    void deleteUser(long userId) throws MyEntityNotFoundException;

}
