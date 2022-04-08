CREATE TABLE `field` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `selector` varchar(255) NOT NULL,
    `weight` float NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `site` (
    `id` int NOT NULL AUTO_INCREMENT,
    `status` enum('INDEXING','INDEXED','FAILED') NOT NULL,
    `status_time` datetime NOT NULL,
    `last_error` text,
    `url` varchar(255) NOT NULL,
    `name` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_URL` (`url`)
);
  
CREATE TABLE `page` (
    `id` int NOT NULL AUTO_INCREMENT,
    `path` text NOT NULL,
    `code` int NOT NULL,
    `content` mediumtext NOT NULL,
    `site_id` int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_PATH_SITE` (`path`(255),`site_id`),
    KEY `FK_SITE_PAGE` (`site_id`),
    CONSTRAINT `FK_SITE_PAGE` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE
);

CREATE TABLE `lemma` (
    `id` int NOT NULL AUTO_INCREMENT,
    `lemma` varchar(255) NOT NULL,
    `frequency` int NOT NULL,
    `site_id` int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_LEMMA_SITE` (`lemma`,`site_id`),
    KEY `FK_SITE_LEMMA` (`site_id`),
    CONSTRAINT `FK_SITE_LEMMA` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE
);
  
CREATE TABLE `_index` (
    `id` int NOT NULL AUTO_INCREMENT,
    `page_id` int NOT NULL,
    `lemma_id` int NOT NULL,
    `_rank` float NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_PAGE_ID` (`page_id`),
    KEY `FK_LEMMA_ID` (`lemma_id`),
    CONSTRAINT `FK_PAGE_ID` FOREIGN KEY (`page_id`) REFERENCES `page` (`id`) ON DELETE CASCADE,
    CONSTRAINT `FK_LEMMA_ID` FOREIGN KEY (`lemma_id`) REFERENCES `lemma` (`id`) ON DELETE CASCADE
);