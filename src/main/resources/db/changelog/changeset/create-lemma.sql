CREATE TABLE `lemma` (
    `id` int NOT NULL AUTO_INCREMENT,
    `lemma` varchar(255) NOT NULL,
    `frequency` int NOT NULL,
    `site_id` int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_LEMMA_SITE` (`lemma`,`site_id`),
    KEY `FK_SITE_LEMMA` (`site_id`),
    CONSTRAINT `FK_SITE_LEMMA` FOREIGN KEY (`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE
)