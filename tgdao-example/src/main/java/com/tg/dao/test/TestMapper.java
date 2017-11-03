package com.tg.dao.test;

import com.tg.dao.annotation.Params;
import com.tg.dao.test.model.User;
import org.apache.ibatis.annotations.Param;


/**
 * Created by twogoods on 2017/11/3.
 */
@Params
public interface TestMapper {
    Object test(@Param("user") User user);

    int sdbu(User user);
}
