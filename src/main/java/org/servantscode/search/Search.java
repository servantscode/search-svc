package org.servantscode.search;

import java.time.ZonedDateTime;

public class Search {
    public enum SearchType {PERSON, FAMILY, MINISTRY, DONATION, MASS_INTENTION, EVENT, ROOM, EQUIPMENT, SEARCH, PLEDGE };

    private int id;
    private String name;
    private String search;
    private SearchType searchType;
    private int searcherId;
    private ZonedDateTime saved;
    private ZonedDateTime lastUsed;

    public Search() { }

    // ------ Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public SearchType getSearchType() { return searchType; }
    public void setSearchType(SearchType searchType) { this.searchType = searchType; }

    public int getSearcherId() { return searcherId; }
    public void setSearcherId(int searcherId) { this.searcherId = searcherId; }

    public ZonedDateTime getSaved() { return saved; }
    public void setSaved(ZonedDateTime saved) { this.saved = saved; }

    public ZonedDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(ZonedDateTime lastUsed) { this.lastUsed = lastUsed; }
}
