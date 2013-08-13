
CREATE TABLE User (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE Plan (
    id INT PRIMARY KEY,
    summary VARCHAR(255) NOT NULL,
    details VARCHAR(255) NULL,
    creator_id INT NOT NULL,
    FOREIGN KEY(creator_id) REFERENCES User(id) ON DELETE CASCADE
);
