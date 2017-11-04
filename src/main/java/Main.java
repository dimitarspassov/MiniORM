import entities.User;
import orm.Connector;
import orm.EntityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;


public class Main {
    public static void main(String[] args) throws IOException, SQLException, IllegalAccessException, InstantiationException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String username = reader.readLine().trim();
        String password = reader.readLine().trim();
        String db = reader.readLine().trim();

        Connector.createConnection(username, password, db);

        EntityManager<User> em = new EntityManager<>(Connector.getConnection());


        //Create a user
        User testUser = new User("test_user", 21, LocalDate.now());

        //Set id, if persist should result in UPDATE query
        //testUser.setId(10);

        em.persist(testUser);

        //Find first without where clause
        User user = em.findFirst(User.class);
        printUser(user);

        //Find first with where clause
        User specificUser = em.findFirst(User.class, "age > 20 AND SUBSTR(username,1,1) = 'c'");
        printUser(specificUser);

        //Find all users
        for (User u : em.find(User.class)) {
            printUser(u);
        }


        //Find all users with condition
        for (User u : em.find(User.class, "age>22")) {
            printUser(u);
        }
    }


    private static void printUser(User user) {
        System.out.println(user.getUsername());
        System.out.println(user.getAge());
        System.out.println(user.getId());
        System.out.println(user.getRegistrationDate());
        System.out.println();
    }
}
