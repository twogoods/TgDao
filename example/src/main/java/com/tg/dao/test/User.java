package com.tg.dao.test;

import com.tg.annotation.Column;
import com.tg.annotation.Id;
import com.tg.annotation.Ignore;
import com.tg.annotation.Table;

/**
 * Created by twogoods on 2017/7/28.
 */

@Table(name = "T_User")
public class User {
    @Id("id")
    private int id;

    private String name;

    private int age;

    @Column("now_address")
    private String address;

    @Ignore
    private String test;

}
