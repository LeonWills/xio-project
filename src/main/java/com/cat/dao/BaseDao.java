package com.cat.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author CAT
 */
public abstract class BaseDao {
    @Autowired
    JdbcTemplate jdbcTemplate;
}
