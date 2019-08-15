package org.servantscode.search.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.search.Search;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.servantscode.search.Search.SearchType.valueOf;

public class SearchDB extends DBAccess {

    private final SearchParser<Search> searchParser;

    private static final HashMap<String, String> FIELD_MAP;

    static {
        FIELD_MAP = new HashMap<>(8);
        FIELD_MAP.put("searchType", "search_type");
        FIELD_MAP.put("searcherId", "searcher_id");
        FIELD_MAP.put("lastUsed", "last_used");
    }

    public SearchDB() {
        this.searchParser = new SearchParser<>(Search.class, "name", FIELD_MAP);
    }

    public int getCount(int userId, Search.SearchType type, String search) {
        QueryBuilder query = count().from("searches")
                .where("searcher_id=?", userId)
                .search(searchParser.parse(search)).inOrg();
        if(type != null)
            query.where("search_type=?", type);
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve search count '" + search + "'", e);
        }
        return 0;
    }

    public Search getSearch(int id) {
        QueryBuilder query = selectAll().from("searches").withId(id).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
        ) {
            List<Search> searches = processResults(stmt);
            return firstOrNull(searches);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve search: " + id, e);
        }
    }

    public List<Search> getSearches(int userId, Search.SearchType type, String search, String sortField, int start, int count) {
        QueryBuilder query = selectAll().from("searches")
                .where("searcher_id=?", userId)
                .search(searchParser.parse(search)).inOrg();

        if(type != null)
            query.where("search_type=?", type);

        query.sort(sortField).limit(count).offset(start);

        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {
            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve searches.", e);
        }
    }

    public Search create(Search search) {
        String sql = "INSERT INTO searches(name, searcher_id, search, search_type, saved, last_used, org_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            stmt.setString(1, search.getName());
            stmt.setInt(2, search.getSearcherId());
            stmt.setString(3, search.getSearch());
            stmt.setString(4, search.getSearchType().toString());
            stmt.setTimestamp(5, convert(ZonedDateTime.now()));
            stmt.setTimestamp(6, convert(ZonedDateTime.now()));
            stmt.setInt(7, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create search: " + search.getName());

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    search.setId(rs.getInt(1));
            }
            return search;
        } catch (SQLException e) {
            throw new RuntimeException("Could not add search: " + search.getName(), e);
        }
    }

    public Search updateSearch(Search search) {
        String sql = "UPDATE searches SET name=?, searcher_id=?, search=?, search_type=?, last_used=? WHERE id=? AND org_id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, search.getName());
            stmt.setInt(2, search.getSearcherId());
            stmt.setString(3, search.getSearch());
            stmt.setString(4, search.getSearchType().toString());
            stmt.setTimestamp(5, convert(ZonedDateTime.now()));
            stmt.setInt(6, search.getId());
            stmt.setInt(7, OrganizationContext.orgId());

            if (stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update search: " + search.getName());

            return search;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update search: " + search.getName(), e);
        }
    }

    public void updateSearchUsed(int searchId) {
        String sql = "UPDATE searches SET last_used=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, convert(ZonedDateTime.now()));
            stmt.setInt(2, searchId);
            stmt.setInt(3, OrganizationContext.orgId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update search usage.", e);
        }
    }

    public boolean deleteSearch(int id) {
        String sql = "DELETE FROM searches WHERE id=? AND org_id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, OrganizationContext.orgId());
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete search: " + id, e);
        }
    }

    // ----- Private -----
    private List<Search> processResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<Search> searches = new ArrayList<>();
            while (rs.next()) {
                Search search = new Search();
                search.setId(rs.getInt("id"));
                search.setName(rs.getString("name"));
                search.setSearcherId(rs.getInt("searcher_id"));
                search.setSearch(rs.getString("search"));
                search.setSearchType(valueOf(rs.getString("search_type")));
                search.setSaved(convert(rs.getTimestamp("saved")));
                search.setLastUsed(convert(rs.getTimestamp("last_used")));
                searches.add(search);
            }
            return searches;
        }
    }
}
