DROP TABLE IF EXISTS _field;
DROP TABLE IF EXISTS _page;
DROP TABLE IF EXISTS _lemma;
DROP TABLE IF EXISTS _index;
CREATE TABLE _field (_id int NOT NULL AUTO_INCREMENT, _name varchar(255) NOT NULL, _selector varchar(255) NOT NULL, _weight float NOT NULL, PRIMARY KEY (_id));
CREATE TABLE _page (_id int NOT NULL AUTO_INCREMENT, _path text NOT NULL, _code int NOT NULL, _content mediumtext CHARACTER SET utf8mb4 NOT NULL, PRIMARY KEY (_id), UNIQUE KEY (_path(254)));
CREATE TABLE _lemma (_id int NOT NULL AUTO_INCREMENT, _lemma varchar(255) NOT NULL, _frequency int NOT NULL, PRIMARY KEY (_id), UNIQUE KEY (_lemma(254)));
CREATE TABLE _index (_id int NOT NULL AUTO_INCREMENT, _page_id int NOT NULL, _lemma_id int NOT NULL, _rank float NOT NULL, PRIMARY KEY (_id));
INSERT INTO _field(_name, _selector, _weight) VALUES ('title', 'title', 1.0), ('body', 'body', 0.8);