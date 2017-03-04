package com.netcracker.repository.data.interfaces;


import com.netcracker.model.entity.Person;
import com.netcracker.repository.common.JdbcRepository;

import java.util.Optional;

public interface PersonRepository extends JdbcRepository<Person, Long> {
    Optional<Person> findPersonByEmail(String email);
    Optional<Person> updatePersonPassword(Person person);
    int updatePersonPassword(String newPassword, String email);
}