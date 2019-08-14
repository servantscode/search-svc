package org.servantscode.search.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.AbstractDBUpgrade;

import java.sql.SQLException;

public class DBUpgrade extends AbstractDBUpgrade {
    private static final Logger LOG = LogManager.getLogger(DBUpgrade.class);

    @Override
    public void doUpgrade() throws SQLException {
        LOG.info("Verifying database structures.");

        if(!tableExists("search_history")) {
            LOG.info("-- Creating search_history table");
            runSql("CREATE TABLE search_history(searcher_id INTEGER REFERENCES people(id) ON DELETE CASCADE, " +
                                               "search TEXT, " +
                                               "search_type TEXT, " +
                                               "saved TIMESTAMP WITH TIME ZONE, " +
                                               "last_used TIMESTAMP WITH TIME ZONE, " +
                                               "org_id INTEGER REFERENCES organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("searches")) {
            LOG.info("-- Creating searches table");
            runSql("CREATE TABLE searches(id SERIAL PRIMARY KEY," +
                                         "name TEXT, " +
                                         "searcher_id INTEGER REFERENCES people(id) ON DELETE CASCADE, " +
                                         "search TEXT, " +
                                         "search_type TEXT, " +
                                         "saved TIMESTAMP WITH TIME ZONE, " +
                                         "last_used TIMESTAMP WITH TIME ZONE, " +
                                         "org_id INTEGER REFERENCES organizations(id) ON DELETE CASCADE)");
        }
    }
}
