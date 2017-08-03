package com.tg.dao.test;

import com.tg.annotation.*;
import com.tg.constant.Attach;
import com.tg.constant.Criterions;

import java.util.Collection;
import java.util.List;

/**
 * Created by twogoods on 2017/7/28.
 * <p>
 * 关于selective,出于mysql字段不为null
 * insert 全是selective的
 * update set 是selective的,where里不是selective的
 * delete where里不是selective的
 * select where 里全是selective的
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

    //TODO 分页
    @Select
    @Page
    @ModelConditions({
            @ModelCondition(attach = Attach.AND, field = "name", criterion = Criterions.EQUAL),
            @ModelCondition(attach = Attach.AND, field = "minAge", column = "age", criterion = Criterions.GREATER),
            @ModelCondition(attach = Attach.AND, field = "maxAge", column = "age", criterion = Criterions.LESS)
    })
    List<User> queryUser5(UserSearch userSearch);

    @Count
    int count(@Condition(value = Criterions.EQUAL, column = "name") String name,
              @Condition(value = Criterions.GREATER, attach = Attach.OR) int age);

    @Count
    @ModelConditions({
            @ModelCondition(attach = Attach.AND, field = "age", criterion = Criterions.GREATER)
    })
    int count2(User user);

    //insert 一定是selective的
    @Insert(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @BatchInsert(columns = "name,age,now_address")
    int batchInsert(List<User> users);

    //update 的where部分不是selective的
    @Update
    @ModelConditions({
            @ModelCondition(field = "id", criterion = Criterions.EQUAL)
    })
    int update(User user);


    //delete 的where部分不是selective 的
    @Delete
    int delete(@Condition(value = Criterions.GREATER, column = "score") int score,
               @Condition(value = Criterions.LESS, column = "score") int max);


    //------------一下不支持
    int update2(User user, @Condition(column = "id", value = Criterions.IN) int[] ids);

    int update3(int state, @Condition(column = "id", value = Criterions.IN) int[] ids);

}
