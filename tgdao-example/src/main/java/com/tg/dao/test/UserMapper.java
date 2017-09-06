package com.tg.dao.test;


import com.tg.dao.annotation.*;
import com.tg.dao.constant.Attach;
import com.tg.dao.constant.Criterions;
import com.tg.dao.constant.InType;
import com.tg.dao.test.model.User;
import com.tg.dao.test.model.UserSearch;

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
@DaoGen(model = User.class, fileName = "UserTestMapper.xml")
public interface UserMapper {
    @Select(columns = "username,age")
    @OrderBy("id desc")
    List<User> queryUser(@Condition(criterion = Criterions.EQUAL, column = "username", test = "name!=null and name!=''") String name,
                         @Condition(criterion = Criterions.GREATER, attach = Attach.OR) int age,
                         @Limit int limit, @OffSet int offset);

    @Select(columns = "username,age")
    List<User> queryUser1(Integer age);

    @Select
    List<User> queryUser2(@Condition(criterion = Criterions.GREATER, column = "age") int min,
                          @Condition(criterion = Criterions.LESS, column = "age") int max);

    @Select
    List<User> queryUser3(@Condition(criterion = Criterions.EQUAL, column = "username") String name,
                          @Condition(column = "id", criterion = Criterions.IN) String[] ids);

    @Select
    List<User> queryUser4(@Condition(criterion = Criterions.IN) Collection id);

    @Select
    @Page
    @ModelConditions({
            @ModelCondition(field = "username", criterion = Criterions.EQUAL),
            @ModelCondition(field = "minAge", column = "age", criterion = Criterions.GREATER),
            @ModelCondition(field = "maxAge", column = "age", criterion = Criterions.LESS),
            @ModelCondition(field = "ids", column = "id", criterion = Criterions.IN),
            @ModelCondition(field = "idArr", column = "id", criterion = Criterions.IN, paramType = InType.ARRAY)
    })
    List<User> queryUser5(UserSearch userSearch);

    @Count
    int count(@Condition(criterion = Criterions.EQUAL, column = "username") String name,
              @Condition(criterion = Criterions.GREATER, attach = Attach.OR) int age);

    @Count
    @ModelConditions({
            @ModelCondition(attach = Attach.AND, field = "minAge", column = "age", criterion = Criterions.GREATER)
    })
    Integer count2(UserSearch search);

    //insert 一定是selective的
    @Insert(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @BatchInsert(columns = "username,age")
    int batchInsert(List<User> users);

    //update 的where部分不是selective的
    @Update(columns = "username,age")
    @ModelConditions({
            @ModelCondition(field = "id")
    })
    int update(User user);


    //delete 的where部分不是selective 的
    @Delete
    int delete(@Condition(criterion = Criterions.GREATER, column = "age") int min,
               @Condition(criterion = Criterions.LESS, column = "age") int max);

    @Delete
    @ModelConditions({
            @ModelCondition(attach = Attach.AND, field = "minAge", column = "age", criterion = Criterions.GREATER),
            @ModelCondition(attach = Attach.AND, field = "maxAge", column = "age", criterion = Criterions.LESS)
    })
    int delete2(UserSearch userSearch);


    //------------以下不支持------------------
    int update2(User user, @Condition(column = "id", criterion = Criterions.IN) int[] ids);

    int update3(int state, @Condition(column = "id", criterion = Criterions.IN) int[] ids);

}
