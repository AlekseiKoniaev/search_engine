DROP TABLE IF EXISTS _field;

DROP TABLE IF EXISTS _page;

DROP TABLE IF EXISTS _lemma;

DROP TABLE IF EXISTS _index;

DROP TABLE IF EXISTS hibernate_sequence;

CREATE TABLE _field (_id int NOT NULL AUTO_INCREMENT, _name varchar(255) NOT NULL, _selector varchar(255) NOT NULL, _weight float NOT NULL, PRIMARY KEY (_id));

CREATE TABLE _page (_id int NOT NULL AUTO_INCREMENT, _path text NOT NULL, _code int NOT NULL, _content mediumtext NOT NULL, PRIMARY KEY (_id), UNIQUE KEY (_path(254)));

CREATE TABLE _lemma (_id int NOT NULL AUTO_INCREMENT,  _lemma varchar(255) NOT NULL, _frequency int NOT NULL, PRIMARY KEY (_id), UNIQUE KEY (_lemma(254)));

CREATE TABLE _index (_id int NOT NULL AUTO_INCREMENT, _page_id int NOT NULL, _lemma_id int NOT NULL, _rank float NOT NULL, PRIMARY KEY (_id));

CREATE TABLE hibernate_sequence (next_val bigint DEFAULT NULL);

INSERT INTO hibernate_sequence(next_val) VALUE (0);