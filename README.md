
# Search engine

Designed for embedding in the site and performing a search on it.

#### Features:

1. **Site indexing.** Walk all internal pages of the site and indexes all text information from these pages.
2. **Single page indexing.** Adding or update one specific page.
3. **Search system.** Performs a search on pre-indexed pages and return the result as list of pages based on relevance.
4. **Multisite mode.** Allows you to index several sites independently, as well as search for one or all sites.

### Technology

Maven, Spring Boot, MySQL, jdbcTemplate, flyway

### Run

- Build project, by running `mvn clean package` and put the `application.yaml` config file alongside.
- Config must contain:
    1. Access data to database MySQL: host, login, password;
    2. List of sites to be indexed. This should be an array of objects containing the site's address and name. All website URLs must be complete and must not contain a slash at the end;
    3. User-Agent;
    4. Hosting address;
    5. Web-interface path (default `/admin`).

### API

- GET `/startIndexing` - start full indexing;
- GET `/stopIndexing` - stop current indexing;
- POST `/indexPage` - Adding or update single page,
    - parameters:
        - *url* - page address;
- GET `/statistics` - statistics;
- GET `/search` - getting data by search query,
    - parameters:
        - *query* — search query;
        - *site* — site to search (if not set, the search should occur on all indexed sites);
        - *offset* — offset 0 for paging (default 0);
        - *limit* — number of results to display (default 20).

### Demo 
Demo version of the project is available [here](http://koniaev-search-engine.herokuapp.com/admin/)
