package com.example.dog.service;

import com.example.dog.dao.JdbcConnectionHolder;
import com.example.dog.model.Dog;

/*
Is not used now, was replaced with TransactionalProxy
 */
public class TransactionalDogService implements DogService {

    private final DogService dogService;
    private final JdbcConnectionHolder jdbcConnectionHolder;

    public TransactionalDogService(DogService dogService, JdbcConnectionHolder jdbcConnectionHolder) {
        this.dogService = dogService;
        this.jdbcConnectionHolder = jdbcConnectionHolder;
    }

    @Override
    public Dog get(int id) {
        jdbcConnectionHolder.getConnection();
        jdbcConnectionHolder.setReadOnly(true);
        return dogService.get(id);
    }

    @Override
    public Dog add(Dog dog) {
        jdbcConnectionHolder.startTransaction();
        Dog addedDog = dogService.add(dog);
        jdbcConnectionHolder.commit();
        jdbcConnectionHolder.close();
        return addedDog;
    }

    @Override
    public Dog update(int id, Dog dog) {
        jdbcConnectionHolder.startTransaction();
        Dog updatedDog;
        try {
            updatedDog = dogService.update(id, dog);
            jdbcConnectionHolder.commit();
        } catch (Throwable e) {
            jdbcConnectionHolder.rollback();
            throw e;
        } finally {
            jdbcConnectionHolder.close();
        }
        return updatedDog;
    }

    @Override
    public Dog remove(int id) {
        jdbcConnectionHolder.startTransaction();
        Dog deletedDog = dogService.remove(id);
        jdbcConnectionHolder.commit();
        jdbcConnectionHolder.close();
        return deletedDog;
    }
}