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
)