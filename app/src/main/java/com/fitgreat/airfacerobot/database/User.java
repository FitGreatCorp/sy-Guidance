package com.fitgreat.airfacerobot.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 数据库类实体<p>
 *
 * @author zixuefei
 * @since 2020/3/18 0018 14:57
 */

@Entity
public class User {

    @PrimaryKey(autoGenerate = true)//主键是否自动增长，默认为false
    private int id;
    private String name;
    private int age;

    //这里的getter/setter方法是必须的
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
