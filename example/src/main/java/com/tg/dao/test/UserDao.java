package com.tg.dao.test;

import com.tg.annotation.Condition;
import com.tg.annotation.DaoGen;
import com.tg.annotation.OrderBy;
import com.tg.annotation.Select;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;

import java.util.Collection;
import java.util.List;

/**
 * Created by twogoods on 2017/7/28.
 */
@DaoGen(model = User.class)
public interface UserDao {
    @Select
    @OrderBy(condition = "id desc")
    List<User> queryUser(@Condition(value = Criterions.EQUAL, column = "name") String name,
                         @Condition(value = Criterions.GREATER, attach = Attach.OR) int age,
                         int pageSize, int pageNo);

//    @Select
//    List<User> queryUser2(@Condition(value = Criterions.GREATER, column = "score") int score,
//                          @Condition(value = Criterions.LESS, column = "score") int max);
//
//    @Select
//    List<User> queryUser3(@Condition(column = "id", value = Criterions.IN) String[] ids);
//
//    @Select
//    List<User> queryUser4(@Condition(value = Criterions.IN) Collection id);
}
