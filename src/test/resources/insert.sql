INSERT INTO `users` (`id`, `created_at`, `updated_at`, `email`, `name`, `password`, `username`) VALUES
    ('9a8bb192-ccd0-480e-808e-fae8ae47cf67', '2022-08-20 11:56:39', '2022-08-20 11:56:39', 'user1@senocak.com', 'Anil User1', '$2a$10$SJrGhsUSWkbL5dIaowTrnOknr/0XzRLs9WP8no.X4WJnEotLxPa/.', 'user1'),
    ('cc365ccb-43a4-411a-a399-f974e5d276d7', '2022-08-20 11:56:39', '2022-08-20 11:56:39', 'user2@senocak.com', 'Anil User2', '$2a$10$hDuLZ9E4A/cT.uYMduukk.PHyByZ/15FSqqa3Ty5a0wAKirnf.2QS', 'user2'),
('ec1a09c6-e834-4262-be22-93fbffb551ea', '2022-08-20 11:56:39', '2022-08-20 11:56:39', 'admin@senocak.com', 'Anil Admin', '$2a$10$.kPUmZcFyacsFVlNPTU53uK8BMurLNAElXxErv28eNacawTGXow8K', 'admin');
INSERT INTO `roles` (`id`, `name`) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_USER');
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
    ('ec1a09c6-e834-4262-be22-93fbffb551ea', 1),
    ('9a8bb192-ccd0-480e-808e-fae8ae47cf67', 2),
    ('cc365ccb-43a4-411a-a399-f974e5d276d7', 2);
INSERT INTO `team` (`id`, `created_at`, `updated_at`, `available_cash`, `country`, `name`, `user_id`) VALUES
    ('2796c7d1-90f9-443f-9e5f-372ff9b8e8d5', '2022-08-20 11:56:39', '2022-08-20 11:56:39', 100000000, 'Indonesia', 'Team 1', 'ec1a09c6-e834-4262-be22-93fbffb551ea'),
    ('921e2f34-7335-405e-a05e-2d4ac1bcc02d', '2022-08-20 11:56:39', '2022-08-20 11:57:02', 1990000, 'Turkey', 'Team2', 'cc365ccb-43a4-411a-a399-f974e5d276d7'),
    ('ed667468-ebf2-4170-85ad-32511aed970f', '2022-08-20 11:56:39', '2022-08-20 11:56:39', 1000000, 'Turkey', 'Team1', '9a8bb192-ccd0-480e-808e-fae8ae47cf67');
INSERT INTO `player` (`id`, `created_at`, `updated_at`, `age`, `country`, `first_name`, `last_name`, `market_value`, `position`, `team_id`) VALUES
    ('1343897b-1bf2-4270-bfae-e4ce9ae05c5a', NULL, '2022-08-20 11:57:02', 27, 'USA7', 'John1-7', 'Doe1-7', 1000000, 'Defender', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('18786fec-802f-4687-885c-063b736b2fad', NULL, '2022-08-20 11:56:39', 37, 'USA17', 'John2-17', 'Doe2-17', 1000000, 'Forward', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('1b7f5867-b677-4e12-83bb-a70dc872420e', NULL, '2022-08-20 11:56:39', 23, 'USA3', 'John2-3', 'Doe2-3', 1000000, 'Defender', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('1d2c4c90-ed76-413e-80ce-f8ae2a5a5850', NULL, '2022-08-20 11:56:39', 27, 'USA7', 'John2-7', 'Doe2-7', 1000000, 'Defender', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('1f200c96-76db-466c-9162-badae53ce0a5', NULL, '2022-08-20 11:56:39', 24, 'USA4', 'John2-4', 'Doe2-4', 1000000, 'Defender', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('2751da67-2f39-4d7e-9a14-93ddf8a3084d', NULL, '2022-08-20 11:56:39', 21, 'USA1', 'John2-1', 'Doe2-1', 1000000, 'GoalKeeper', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('285ca3f6-8434-4b44-9e13-b05e65c5224d', NULL, '2022-08-20 11:56:39', 29, 'USA9', 'John2-9', 'Doe2-9', 1000000, 'Midfielder', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('2a5dba56-969b-4917-90ce-c3a530d7c2e9', NULL, '2022-08-20 11:56:39', 31, 'USA11', 'John1-11', 'Doe1-11', 1000000, 'Midfielder', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('488a45dd-f303-4553-a51d-0e37b61a6377', NULL, '2022-08-20 11:56:39', 33, 'USA13', 'John2-13', 'Doe2-13', 1000000, 'Midfielder', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('4a62afb0-8d45-48e8-840a-233f97a5ca85', NULL, '2022-08-20 11:56:39', 28, 'USA8', 'John1-8', 'Doe1-8', 1000000, 'Defender', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('4c4bbe48-289f-4455-b213-05e69fbe2d33', NULL, '2022-08-20 11:56:39', 35, 'USA15', 'John2-15', 'Doe2-15', 1000000, 'Forward', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('545e0c73-68f0-4110-81fa-712ce3f2fe04', NULL, '2022-08-20 11:56:39', 33, 'USA13', 'John1-13', 'Doe1-13', 1000000, 'Midfielder', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('5b5627b1-de70-42b8-8789-d1c6e33afb88', NULL, '2022-08-20 11:56:39', 25, 'USA5', 'John1-5', 'Doe1-5', 1000000, 'Defender', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('61fa8cba-b492-4e8e-af63-2f70b4869775', NULL, '2022-08-20 11:56:39', 21, 'USA1', 'John1-1', 'Doe1-1', 1000000, 'GoalKeeper', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('6c8dcd90-4435-4e6d-8f62-b2e9476ad885', NULL, '2022-08-20 11:56:39', 39, 'USA19', 'John2-19', 'Doe2-19', 1000000, 'Forward', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('6ea259a4-7fee-4d87-a063-bd075525b6dc', NULL, '2022-08-20 11:56:39', 30, 'USA10', 'John1-10', 'Doe1-10', 1000000, 'Midfielder', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('6f5592e7-9bfb-4faa-a7e6-041bec892b68', NULL, '2022-08-20 11:56:39', 36, 'USA16', 'John1-16', 'Doe1-16', 1000000, 'Forward', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('71b6a260-4e4f-4372-b108-15e2f9f5982f', NULL, '2022-08-20 11:56:39', 29, 'USA9', 'John1-9', 'Doe1-9', 1000000, 'Midfielder', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('7678a55f-34ed-4a70-8c78-62e3b0bce835', NULL, '2022-08-20 11:56:39', 24, 'USA4', 'John1-4', 'Doe1-4', 1000000, 'Defender', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('78304c4d-7fc2-470c-8756-2a4c0e65ee2b', NULL, '2022-08-20 11:56:39', 34, 'USA14', 'John2-14', 'Doe2-14', 1000000, 'Midfielder', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('8210a1e7-6c06-426f-8991-cd3c47742e1e', NULL, '2022-08-20 11:56:39', 38, 'USA18', 'John2-18', 'Doe2-18', 1000000, 'Forward', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('826df420-5704-40ab-9a6d-1847eb26b104', NULL, '2022-08-20 11:56:39', 20, 'USA0', 'John2-0', 'Doe2-0', 1000000, 'GoalKeeper', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('842e7629-7fc4-435e-b93a-f020d9f44d19', NULL, '2022-08-20 11:56:39', 22, 'USA2', 'John1-2', 'Doe1-2', 1000000, 'GoalKeeper', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('85cc2989-bc22-40b7-af2a-66def9bde6e8', NULL, '2022-08-20 11:56:39', 32, 'USA12', 'John2-12', 'Doe2-12', 1000000, 'Midfielder', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('939a317a-3ba4-47f0-bf37-5ee4872c9b40', NULL, '2022-08-20 11:56:39', 20, 'USA0', 'John1-0', 'Doe1-0', 1000000, 'GoalKeeper', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('994613de-3398-48b3-8927-84364b2a108f', NULL, '2022-08-20 11:56:39', 30, 'USA10', 'John2-10', 'Doe2-10', 1000000, 'Midfielder', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('9d0545e8-1d1b-4fe7-877e-3507ce93d22c', NULL, '2022-08-20 11:56:39', 26, 'USA6', 'John2-6', 'Doe2-6', 1000000, 'Defender', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('a79ed870-6eaf-4374-a970-fdeed6431dcd', NULL, '2022-08-20 11:56:39', 23, 'USA3', 'John1-3', 'Doe1-3', 1000000, 'Defender', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('ad77384e-0f3f-4396-8116-d5fc6dc69596', NULL, '2022-08-20 11:56:39', 38, 'USA18', 'John1-18', 'Doe1-18', 1000000, 'Forward', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('b8d98282-3c15-4173-8a18-5c371522e857', NULL, '2022-08-20 11:56:39', 28, 'USA8', 'John2-8', 'Doe2-8', 1000000, 'Defender', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('bd15b58a-caf2-4537-9e80-7214620350ed', NULL, '2022-08-20 11:56:39', 25, 'USA5', 'John2-5', 'Doe2-5', 1000000, 'Defender', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('cd1e4573-e4c8-4c8f-b892-38fafb19c633', NULL, '2022-08-20 11:56:39', 22, 'USA2', 'John2-2', 'Doe2-2', 1000000, 'GoalKeeper', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('d1d147e4-eaf8-4e6d-ba03-d03f0e6836d4', NULL, '2022-08-20 11:56:39', 31, 'USA11', 'John2-11', 'Doe2-11', 1000000, 'Midfielder', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('d2428a52-3a53-4a92-b2e8-a9ba953263d8', NULL, '2022-08-20 11:56:39', 36, 'USA16', 'John2-16', 'Doe2-16', 1000000, 'Forward', '921e2f34-7335-405e-a05e-2d4ac1bcc02d'),
    ('d351beaa-08f0-43aa-ae62-c1cf7ca917ea', NULL, '2022-08-20 11:56:39', 34, 'USA14', 'John1-14', 'Doe1-14', 1000000, 'Midfielder', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('d47954f7-9afc-42e0-8484-a0ec33fba92e', NULL, '2022-08-20 11:56:39', 37, 'USA17', 'John1-17', 'Doe1-17', 1000000, 'Forward', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('e2a3ed65-b176-40e4-95c4-d938a21c6ce5', NULL, '2022-08-20 11:56:39', 32, 'USA12', 'John1-12', 'Doe1-12', 1000000, 'Midfielder', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('e44be276-aa05-489f-8db6-2b101104d904', NULL, '2022-08-20 11:56:39', 39, 'USA19', 'John1-19', 'Doe1-19', 1000000, 'Forward', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('e4eae701-4ec7-4deb-b9d6-0446caabd177', NULL, '2022-08-20 11:56:39', 35, 'USA15', 'John1-15', 'Doe1-15', 1000000, 'Forward', 'ed667468-ebf2-4170-85ad-32511aed970f'),
    ('f063b061-2700-4062-8094-ff485cbb0556', NULL, '2022-08-20 11:56:39', 26, 'USA6', 'John1-6', 'Doe1-6', 1000000, 'Defender', 'ed667468-ebf2-4170-85ad-32511aed970f');
INSERT INTO `transfer` (`id`, `created_at`, `updated_at`, `asked_price`, `market_value`, `transferred`, `player_id`, `transferred_from_id`, `transferred_to_id`) VALUES
    ('b6dad2de-a0ff-4148-b2a4-ea5cc97ff3be', '2022-08-20 11:56:54', '2022-08-20 11:49:21', 20000, 380000, b'0', '18786fec-802f-4687-885c-063b736b2fad', NULL, NULL),
    ('b6dad2de-a0ff-4148-b2a4-ea5cc97ff4be', '2022-08-20 11:56:54', '2022-08-20 11:59:29', 20000, 380000, b'1', '1343897b-1bf2-4270-bfae-e4ce9ae05c5a', 'ed667468-ebf2-4170-85ad-32511aed970f', '921e2f34-7335-405e-a05e-2d4ac1bcc02d');
