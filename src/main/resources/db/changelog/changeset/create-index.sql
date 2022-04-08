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
)