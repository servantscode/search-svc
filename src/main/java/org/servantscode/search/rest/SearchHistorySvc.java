package org.servantscode.search.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.EnumUtils;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.search.Search;
import org.servantscode.search.db.SearchDB;
import org.servantscode.search.db.SearchHistoryDB;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.servantscode.commons.StringUtils.isEmpty;

@Path("/search/history")
public class SearchHistorySvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(SearchHistorySvc.class);

    private final SearchHistoryDB db;

    @Context SecurityContext securityContext;

    public SearchHistorySvc() { db = new SearchHistoryDB(); }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Search> getSearchHistory(@QueryParam("start") @DefaultValue("0") int start,
                                                      @QueryParam("count") @DefaultValue("10") int count,
                                                      @QueryParam("sort_field") @DefaultValue("saved DESC") String sortField,
                                                      @QueryParam("search") @DefaultValue("") String search) {

        try {
            int totalPeople = db.getCount(search);
            List<Search> results = db.getSearches(search, sortField, start, count);
            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving search history failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteSearch(@PathParam("search") String search,
                             @PathParam("searchType") Search.SearchType searchType) {

        if(isEmpty(search) || searchType == null)
            throw new NotFoundException();

        try {
            Search dbSearch = db.getSearch(search, searchType, getUserId(securityContext));
            if(dbSearch == null || !db.deleteSearch(dbSearch))
                throw new NotFoundException();
            LOG.info("Deleted search: " + dbSearch.getSearch());
        } catch (Throwable t) {
            LOG.error("Deleting search failed:", t);
            throw t;
        }
    }
}
