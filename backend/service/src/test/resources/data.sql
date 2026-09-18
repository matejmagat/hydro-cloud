-- ROLES
INSERT INTO role
    (ROLE_ID, CREATED_AT, UPDATED_AT, ROLE_NAME, DESCRIPTION)
VALUES (1, NOW(), NULL, 'ADMIN', 'Full administrative access to the system'),
       (2, NOW(), NULL, 'DATA_MANAGER', 'Manage hydrological data and imports'),
       (3, NOW(), NULL, 'USER_MANAGER', 'Manage users, roles, and access rights'),
       (4, NOW(), NULL, 'USER', 'Standard authenticated user with read access');


-- PRIVILEGES
INSERT INTO privilege
    (PRIVILEGE_ID, PRIVILEGE_NAME, CREATED_AT, UPDATED_AT, DESCRIPTION)
VALUES (1, 'DATA_READ', NOW(), NULL, 'Read hydrological data'),
       (2, 'DATA_CREATE', NOW(), NULL, 'Create new hydrological records'),
       (3, 'DATA_UPDATE', NOW(), NULL, 'Update existing hydrological records'),
       (4, 'DATA_DELETE', NOW(), NULL, 'Delete hydrological records'),
       (5, 'DATA_BULK_IMPORT', NOW(), NULL, 'Bulk import hydrological datasets'),
       (6, 'DATA_EXPORT', NOW(), NULL, 'Export hydrological data'),
       (7, 'USER_READ', NOW(), NULL, 'Read user information'),
       (8, 'USER_CREATE', NOW(), NULL, 'Create new users'),
       (9, 'USER_UPDATE', NOW(), NULL, 'Update user data'),
       (10, 'USER_DELETE', NOW(), NULL, 'Delete users'),
       (11, 'USER_ASSIGN_ROLE', NOW(), NULL, 'Assign or revoke user roles'),
       (12, 'ALL_USER_READ', NOW(), NULL, 'Read all users information');


-- ADMIN ROLE PRIVILEGE MAPPING
INSERT INTO role_privilege (role_privilege_id, ROLE_ID, PRIVILEGE_ID, created_at)
SELECT PRIVILEGE_ID, 1, PRIVILEGE_ID, NOW()
FROM privilege;

-- DATA MANAGER PRIVILEGE MAPPING
INSERT INTO role_privilege (ROLE_ID, PRIVILEGE_ID, created_at, role_privilege_id)
VALUES (2, 1, NOW(), 13),
       (2, 2, NOW(), 14),
       (2, 3, NOW(), 15),
       (2, 4, NOW(), 16),
       (2, 5, NOW(), 17),
       (2, 6, NOW(), 18);

-- USER MANAGER PRIVILEGE MAPPING
INSERT INTO role_privilege (ROLE_ID, PRIVILEGE_ID, created_at, role_privilege_id)
VALUES (3, 7, now(), 19),
       (3, 8, now(), 20),
       (3, 9, now(), 21),
       (3, 10, now(), 22),
       (3, 11, now(), 23),
       (3, 12, now(), 24);

-- USER PRIVILEGE MAPPING
INSERT INTO role_privilege (ROLE_ID, PRIVILEGE_ID, created_at, role_privilege_id)
VALUES (4, 1, NOW(), 25),
       (4, 6, NOW(), 26);


-- USERS
INSERT INTO user_entity
(first_name, last_name, username, email, password, blocked, created_at, updated_at, is_2fa_enabled)
VALUES ('System', 'Admin', 'admin', 'admin@system.local',
        '$2a$10$HWbo953zqtwMxbstSsXAqOiZ9WVoqLn3JAQn4moJGztqBXm/KkUsm',
        false, NOW(), NULL, false),
       ('User', 'Manager', 'user_manager', 'user.manager@system.local',
        '$2a$10$37LtX2ROJWo4SOjAf/qBN.w0A1orS4RiGZXZvVRrw9gvohwAp.6d6',
        false, NOW(), NULL, false),
       ('Data', 'Manager', 'data_manager', 'data.manager@system.local',
        '$2a$10$e/aYHtq2g2yjHLAVF7cn4u/2gmGKLW2ZopBThzLwxblFl3dM.vzLW',
        false, NOW(), NULL, false);


-- Admin
INSERT INTO user_role (user_id, role_id, user_role_id, created_at)
SELECT u.user_id, 1, 1, NOW()
FROM user_entity u
WHERE u.username = 'admin';

-- User Manager
INSERT INTO user_role (user_id, role_id, user_role_id, created_at)
SELECT u.user_id, 3, 2, NOW()
FROM user_entity u
WHERE u.username = 'user_manager';

-- Data Manager
INSERT INTO user_role (user_id, role_id, user_role_id, created_at)
SELECT u.user_id, 2, 3, NOW()
FROM user_entity u
WHERE u.username = 'data_manager';


SELECT setval('user_entity_user_id_seq', (SELECT MAX(user_id) FROM user_entity));
SELECT setval('user_role_seq', (SELECT MAX(user_role_id) FROM user_role));

COMMIT;
