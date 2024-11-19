SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `turkcell`
--

CREATE TABLE `player` (
    `id` varchar(255) NOT NULL,
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `age` int(11) NOT NULL,
    `country` varchar(255) NOT NULL,
    `first_name` varchar(255) NOT NULL,
    `last_name` varchar(255) NOT NULL,
    `market_value` int(11) NOT NULL,
    `position` varchar(255) NOT NULL,
    `team_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `roles` (`id` bigint(20) NOT NULL, `name` varchar(60) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `team` (
    `id` varchar(255) NOT NULL,
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `available_cash` int(11) NOT NULL,
    `country` varchar(255) NOT NULL,
    `name` varchar(255) NOT NULL,
    `user_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `transfer` (
    `id` varchar(255) NOT NULL,
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `asked_price` int(11) NOT NULL,
    `market_value` int(11) DEFAULT NULL,
    `transferred` bit(1) NOT NULL,
    `player_id` varchar(255) DEFAULT NULL,
    `transferred_from_id` varchar(255) DEFAULT NULL,
    `transferred_to_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `users` (
    `id` varchar(255) NOT NULL,
    `created_at` datetime DEFAULT NULL,
    `updated_at` datetime DEFAULT NULL,
    `email` varchar(40) DEFAULT NULL,
    `name` varchar(40) DEFAULT NULL,
    `password` varchar(100) DEFAULT NULL,
    `username` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_roles` (`user_id` varchar(255) NOT NULL, `role_id` bigint(20) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `player` ADD PRIMARY KEY (`id`), ADD KEY `FKdvd6ljes11r44igawmpm1mc5s` (`team_id`);
ALTER TABLE `roles` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `UK_nb4h0p6txrmfc0xbrd1kglp9t` (`name`);
ALTER TABLE `team` ADD PRIMARY KEY (`id`), ADD KEY `FK16cnc4ugq97f0y71k3oplbvay` (`user_id`);
ALTER TABLE `transfer`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FKe98lusfudhauj501o005b2nma` (`player_id`),
    ADD KEY `FKpkvl95k0b13cs51784u94am6e` (`transferred_from_id`),
    ADD KEY `FK7i9btdwomnrbmrf182fjpma7p` (`transferred_to_id`);
ALTER TABLE `users`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
    ADD UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`);
ALTER TABLE `user_roles` ADD PRIMARY KEY (`user_id`,`role_id`), ADD KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`);

ALTER TABLE `roles`MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
ALTER TABLE `player` ADD CONSTRAINT `FKdvd6ljes11r44igawmpm1mc5s` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`);
ALTER TABLE `team` ADD CONSTRAINT `FK16cnc4ugq97f0y71k3oplbvay` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `transfer`
    ADD CONSTRAINT `FK7i9btdwomnrbmrf182fjpma7p` FOREIGN KEY (`transferred_to_id`) REFERENCES `team` (`id`),
    ADD CONSTRAINT `FKe98lusfudhauj501o005b2nma` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`),
    ADD CONSTRAINT `FKpkvl95k0b13cs51784u94am6e` FOREIGN KEY (`transferred_from_id`) REFERENCES `team` (`id`);
ALTER TABLE `user_roles`
    ADD CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
    ADD CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;