/**
 * Cerberus  Copyright (C) 2013 - 2016  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.crud.service.impl;

import java.util.List;
import org.cerberus.crud.dao.IUserSystemDAO;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IUserSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class UserSystemService implements IUserSystemService {

    @Autowired
    private IUserSystemDAO userSystemDAO;
    
    @Override
    public UserSystem findUserSystemByKey(String login, String system) throws CerberusException {
        return userSystemDAO.findUserSystemByKey(login, system);
    }

    @Override
    public List<UserSystem> findallUser() throws CerberusException {
        return userSystemDAO.findallUser();
    }

    @Override
    public List<UserSystem> findUserSystemByUser(String login) throws CerberusException {
        return userSystemDAO.findUserSystemByUser(login);
    }

    @Override
    public List<UserSystem> findUserSystemBySystem(String system) throws CerberusException {
        return userSystemDAO.findUserSystemBySystem(system);
    }

    @Override
    public void insertUserSystem(UserSystem useSystem) throws CerberusException {
        userSystemDAO.insertUserSystem(useSystem);
    }

    @Override
    public void deleteUserSystem(UserSystem userSystem) throws CerberusException {
        userSystemDAO.deleteUserSystem(userSystem);
    }

    @Override
    public void updateUserSystems(User user, List<UserSystem> newSystems) throws CerberusException {
        List<UserSystem> oldSystems = this.findUserSystemByUser(user.getLogin());

        //delete if don't exist in new
        for (UserSystem old : oldSystems) {
            if (!newSystems.contains(old)) {
                this.deleteUserSystem(old);
            }
        }
        //insert if don't exist in old
        for (UserSystem newS : newSystems) {
            if (!oldSystems.contains(newS)) {
                this.insertUserSystem(newS);
            }
        }
    }
    
}
