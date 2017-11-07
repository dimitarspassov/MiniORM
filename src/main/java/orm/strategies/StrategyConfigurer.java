package orm.strategies;


import orm.utils.DatabaseTableCreator;
import orm.EntityManagerBuilder;
import orm.utils.TableCreator;
import orm.scanner.ClassEntityScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;


public class StrategyConfigurer {
    private EntityManagerBuilder builder;

    public StrategyConfigurer(EntityManagerBuilder builder) {
        this.builder = builder;
    }

    public <T extends SchemaInitializationStrategy> EntityManagerBuilder
    set(Class<T> strategyClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Constructor<T> strategyConstructor = strategyClass.getConstructor(Connection.class, TableCreator.class, String.class, ClassEntityScanner.class);

        T strategy = strategyConstructor.newInstance(
                this.builder.getConnection(),
                new DatabaseTableCreator(this.builder.getConnection(), this.builder.getDataSource()),
                this.builder.getDataSource(),
                new ClassEntityScanner()
        );

        this.builder.setStrategy(strategy);
        return this.builder;
    }
}
