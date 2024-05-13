CREATE CACHED TABLE FEED_LINK(
    ID BIGINT NOT NULL PRIMARY KEY,
    TITLE VARCHAR(255),
    URL VARCHAR(255)
);

CREATE CACHED TABLE POST(
    ID VARCHAR(255) NOT NULL PRIMARY KEY,
    LINK VARCHAR(255),
    READ BOOLEAN DEFAULT FALSE,
    TITLE VARCHAR(255),
    FEED_ID BIGINT,
    FOREIGN KEY(FEED_ID) REFERENCES FEED_LINK(ID) ON DELETE CASCADE
);

CREATE INDEX POST_IDX_FEED_ID ON POST(FEED_ID);

CREATE SEQUENCE IF NOT EXISTS HIBERNATE_SEQUENCE;