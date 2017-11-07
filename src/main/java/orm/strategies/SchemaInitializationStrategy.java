package orm.strategies;

import java.io.IOException;
import java.sql.SQLException;

public interface SchemaInitializationStrategy {

    void execute() throws SQLException, IOException, ClassNotFoundException;
}
