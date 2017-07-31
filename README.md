### 介绍
根据用户定义的方法，编译期自动生成mybatis的sql文件，生成的文件在dao接口所在的包下。

```
@Table(name = "user")
public class User {
    @Id("id")
    private int id;

    private String name;

    private int age;

    @Column("now_address")
    private String address;

}

```
上面的model定义了模型和数据库表的关系，看到下面这些方法的签名，聪明的你肯定能猜出每个方法的sql吧，这就是这个库打算做的工作。
```
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

    @Insert
    int insert(User user);
}
```

### 资料
https://mapperhelper.github.io/docs/2.use/

java8 移除APT   引入 Pluggable Annotation Processing API

debug http://blog.jensdriller.com/how-to-debug-a-java-annotation-processor-using-intellij/

http://blog.csdn.net/u011315960/article/details/64907139