package org.servantscode.search.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.EnumUtils;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.search.Search;
import org.servantscode.search.db.SearchDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/search")
public class SearchSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(SearchSvc.class);

    private final SearchDB db;

    public SearchSvc() {
        db = new SearchDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Search> getSearches(@QueryParam("start") @DefaultValue("0") int start,
                                                 @QueryParam("count") @DefaultValue("10") int count,
                                                 @QueryParam("sort_field") @DefaultValue("last_used DESC") String sortField,
                                                 @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("search.list");
        try {
            int userId = getUserId();
            int totalSearches = db.getCount(userId, null, search);
            List<Search> results = db.getSearches(userId, null, search, sortField, start, count);
            return new PaginatedResponse<>(start, results.size(), totalSearches, results);
        } catch (Throwable t) {
            LOG.error("Retrieving searches failed:", t);
            throw t;
        }
    }

    @GET @Path("/type/{type}") @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Search> getSearches(@PathParam("type") String typeString,
                                                 @QueryParam("start") @DefaultValue("0") int start,
                                                 @QueryParam("count") @DefaultValue("10") int count,
                                                 @QueryParam("sort_field") @DefaultValue("last_used DESC") String sortField,
                                                 @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("search.list");
        try {
            Search.SearchType type = Search.SearchType.valueOf(typeString.toUpperCase());
            int userId = getUserId();
            int totalSearches = db.getCount(userId, type, search);
            List<Search> results = db.getSearches(userId, type, search, sortField, start, count);
            return new PaginatedResponse<>(start, results.size(), totalSearches, results);
        } catch (Throwable t) {
            LOG.error("Retrieving searches failed:", t);
            throw t;
        }
    }


    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Search getSearch(@PathParam("id") int id) {
        verifyUserAccess("search.read");
        try {
            return db.getSearch(id);
        } catch (Throwable t) {
            LOG.error("Retrieving search failed:", t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Search createSearch(Search search) {
        verifyUserAccess("search.create");
        try {
            db.create(search);
            LOG.info("Saved search: " + search.getName());
            return search;
        } catch (Throwable t) {
            LOG.error("Creating search failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Search updateSearch(Search search) {
        verifyUserAccess("search.update");
        try {
            db.updateSearch(search);
            LOG.info("Updated search: " + search.getName());
            return search;
        } catch (Throwable t) {
            LOG.error("Updating search failed:", t);
            throw t;
        }
    }

    @PUT @Path("/{id}/touched") @Produces(MediaType.APPLICATION_JSON)
    public void markUsed(@PathParam("id") int searchId) {
        verifyUserAccess("search.update");
        try {
            db.updateSearchUsed(searchId);
        } catch (Throwable t) {
            LOG.error("Marking search usage failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteSearch(@PathParam("id") int id) {
        verifyUserAccess("search.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Search search = db.getSearch(id);
            if(search == null || !db.deleteSearch(id))
                throw new NotFoundException();
            LOG.info("Deleted search: " + search.getName());
        } catch (Throwable t) {
            LOG.error("Deleting search failed:", t);
            throw t;
        }
    }

    @GET @Path("/types") @Produces(APPLICATION_JSON)
    public List<String> getSearchTypes() { return EnumUtils.listValues(Search.SearchType.class); }
}
