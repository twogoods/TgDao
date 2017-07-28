package com.tg.dao.test;

import com.tg.annotation.Column;
import com.tg.annotation.Table;

/**
 * Created by twogoods on 2017/7/28.
 */

@Table(name = "user")
public class User {
    private int id;

    private String name;

    private int age;
    
    @Column("now_address")
    private String address;

}
