CREATE TABLE IF NOT EXISTS `web_mib_browser`.`mib_file`
(
    `id`           varchar(32)  NOT NULL,
    `type`         varchar(50)  NOT NULL COMMENT 'Mib类型',
    `url`          varchar(255) NOT NULL COMMENT '文件路径',
    `created_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
    `updated_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP (0),
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_type`(`type`) USING HASH
);

CREATE TABLE IF NOT EXISTS `web_mib_browser`.`connection`  (
    `id` varchar(32) NOT NULL,
    `ip` varchar(255) NOT NULL,
    `port` varchar(5) NOT NULL,
    `community` varchar(100) NULL,
    `version` varchar(20) NULL,
    `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_connection`(`ip`) USING HASH
);