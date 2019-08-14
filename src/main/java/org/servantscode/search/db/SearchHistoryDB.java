package org.servantscode.search.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.search.Search;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.servantscode.search.Search.SearchType.valueOf;

public class SearchHistoryDB extends DBAccess {

    private final SearchParser<Search> searchParser;

    public SearchHistoryDB() {
        this.searchParser = new SearchParser<>(Search.class, "search");
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("search_history").search(searchParser.parse(search)).inOrg();
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

    public Search getSearch(String search, Search.SearchType searchType, int searcherId) {
        QueryBuilder query = selectAll().from("search_history")
                .where("search=?", search)
                .where("search_type=?", searchType.toString())
                .where("searcher_id=?", searcherId).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn)) {

            List<Search> searches = processResults(stmt);
            return firstOrNull(searches);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve search: " + search, e);
        }
    }

    public List<Search> getSearches(String search, String sortField, int start, int count) {
        QueryBuilder query = selectAll().from("search_history").search(searchParser.parse(search)).inOrg()
                .sort(sortField).limit(count).offset(start);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {
            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve searches.", e);
        }
    }

    public Search create(Search search) {
        String sql = "INSERT INTO search_history(searcher_id, search, search_type, saved, last_used, org_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, search.getSearcherId());
            stmt.setString(2, search.getSearch());
            stmt.setString(3, search.getSearchType().toString());
            stmt.setTimestamp(4, convert(ZonedDateTime.now()));
            stmt.setTimestamp(5, convert(ZonedDateTime.now()));
            stmt.setInt(6, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not create search: " + search.getSearch());

            return search;
        } catch (SQLException e) {
            throw new RuntimeException("Could not add search: " + search.getSearch(), e);
        }
    }

    public void useSearch(Search search) {
        String sql = "UPDATE search_history SET last_used=? " +
                     "WHERE searcher_id=? AND search=? AND searcher_type=? AND org_id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, convert(ZonedDateTime.now()));
            stmt.setInt(2, search.getSearcherId());
            stmt.setString(3, search.getSearch());
            stmt.setString(4, search.getSearchType().toString());
            stmt.setInt(5, OrganizationContext.orgId());

            if (stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not mark search used: " + search.getSearch());

        } catch (SQLException e) {
            throw new RuntimeException("Could not update search: " + search.getSearch(), e);
        }
    }

    public boolean deleteSearch(Search search) {
        String sql = "DELETE FROM search_history " +
                     "WHERE searcher_id=? AND search=? AND searcher_type=? AND org_id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, search.getSearcherId());
            stmt.setString(2, search.getSearch());
            stmt.setString(3, search.getSearchType().toString());
            stmt.setInt(4, OrganizationContext.orgId());
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete search: " + search.getName(), e);
        }
    }

    // ----- Private -----
    private List<Search> processResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<Search> searches = new ArrayList<>();
            while (rs.next()) {
                Search search = new Search();
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
