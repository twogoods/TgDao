package com.tg.dao.test;

import com.tg.annotation.*;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

/**
 * Created by twogoods on 2017/7/28.
 */
@DaoGen(model = User.class)
public interface UserDao {
    @Select
    @OrderBy("id desc")
    List<User> queryUser(@Condition(value = Criterions.EQUAL, column = "name") String name,
                         @Condition(value = Criterions.GREATER, attach = Attach.OR) int age,
                         @Limit int limit, @OffSet int offset);

    @Select
    List<User> queryUser2(@Condition(value = Criterions.GREATER, column = "score") int score,
                          @Condition(value = Criterions.LESS, column = "score") int max);

    @Select
    List<User> queryUser3(@Condition(column = "id", value = Criterions.IN) String[] ids);

    @Select
    List<User> queryUser4(@Condition(value = Criterions.IN) Collection id);

    @Select
    @ModelConditions({
            @ModelCondition(attach = Attach.AND, field = "name", criterion = Criterions.EQUAL),
            @ModelCondition(attach = Attach.AND, field = "age", criterion = Criterions.EQUAL)
    })
    List<User> queryUser5(User user);


    @Count
    int count(@Condition(value = Criterions.EQUAL, column = "name") String name,
              @Condition(value = Criterions.GREATER, attach = Attach.OR) int age);

    @Insert(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @BatchInsert(columns = "name,age,now_address")
    int batchInsert(List<User> users);

    @Update
    @ModelConditions({
            @ModelCondition(field = "id", criterion = Criterions.EQUAL)
    })
    int update(User user);

    @Update
    int update2(User user,
                @Condition(column = "id", value = Criterions.IN) int[] ids);

    @Delete
    int delete(@Condition(value = Criterions.GREATER, column = "score") int score,
               @Condition(value = Criterions.LESS, column = "score") int max);

}
