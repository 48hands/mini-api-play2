# --- !Ups
CREATE TABLE IF NOT EXISTS dev_db.companies (
   `id` INTEGER NOT NULL
   ,`name` VARCHAR(20) NOT NULL
   ,PRIMARY KEY(`id`)
);

INSERT INTO dev_db.companies VALUES (1, 'NTTDATA');
INSERT INTO dev_db.companies VALUES (2, 'NTTDOCOMO');

CREATE TABLE IF NOT EXISTS dev_db.users (
   `id` INTEGER(20) AUTO_INCREMENT
  ,`name` VARCHAR(20) NOT NULL
  ,`company_id` INTEGER NOT NULL
  ,PRIMARY KEY (`id`)
  ,FOREIGN KEY (`company_id`) REFERENCES dev_db.companies (`id`)
);

INSERT INTO dev_db.users(`name`, `company_id`) VALUES ('Taro Yamada', 1);
INSERT INTO dev_db.users(`name`, `company_id`) VALUES ('Hanako Sato', 2);

# --- !Downs
DROP TABLE IF EXISTS dev_db.users;
DROP TABLE IF EXISTS dev_db.companies;