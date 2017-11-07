import entities.Student;
import entities.User;
import orm.EntityManager;
import orm.EntityManagerBuilder;
import orm.strategies.DropCreateStrategy;
import orm.strategies.UpdateStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.time.LocalDate;


public class Main {
    public static void main(String[] args) throws IOException, SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String username = reader.readLine().trim();
        String password = reader.readLine().trim();
        String db = reader.readLine().trim();


        EntityManagerBuilder emBuilder = new EntityManagerBuilder();
        EntityManager em = emBuilder.configureConnectionString()
                .setUser(username)
                .setPass(password)
                .setAdapter("mysql")
                .setDriver("jdbc")
                .setHost("localhost")
                .setPort("3306")
                .createConnection()
                .setDataSource(db)
                .configureCreationType().set(UpdateStrategy.class)
                .build();

        User user = new User("test", 20, LocalDate.now());
        em.persist(user);
    }
}
