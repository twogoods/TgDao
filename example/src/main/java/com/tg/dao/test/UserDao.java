package com.tg.dao.test;

import com.tg.annotation.Condition;
import com.tg.annotation.DaoGen;
import com.tg.annotation.OrderBy;
import com.tg.annotation.Select;
import com.tg.constant.Attach;

import java.util.Collection;
import java.util.List;

/**
 * Created by twogoods on 2017/7/28.
 */
@DaoGen(model = User.class)
public interface UserDao {
    @Select
    @OrderBy(condition = "id desc")
    List<User> queryUser(@Condition(value = "=", column = "name") String name,
                         @Condition(value = ">", attach = Attach.OR) int age,
                         int pageSize, int pageNo);

    @Select
    List<User> queryUser2(@Condition(value = ">", column = "score") int score,
                          @Condition(value = "<", column = "score") int max);

    @Select
    List<User> queryUser3(@Condition(column = "id", value = "in") String[] ids);

    @Select
    List<User> queryUser4(@Condition(value = "in") Collection id);
}
