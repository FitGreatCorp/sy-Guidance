package com.fitgreat.airfacerobot.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 数据库业务操作类<p>
 *
 * @author zixuefei
 * @since 2020/3/18 0018 14:56
 */

@Dao
public interface UserDao {

    //增
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    //删
    @Delete
    void delete(User... users);

    //改
    @Update
    void update(User... users);

    //根据id查询对象
    @Query("SELECT * FROM user where id = :id")
    User getUserById(int id);

    //查
    @Query("SELECT * FROM user")
    List<User> getAllUsers();

}
