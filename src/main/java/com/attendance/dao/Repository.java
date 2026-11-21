package com.attendance.dao;

import java.sql.SQLException;
import java.util.List;

public interface Repository<T> {
    void save(T item) throws SQLException;
    List<T> findAll() throws SQLException;
    void update(T item) throws SQLException;
    void deleteByKey(String key) throws SQLException;
}