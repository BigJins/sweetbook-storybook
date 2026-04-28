CREATE TABLE story (
  id                 CHAR(36)     PRIMARY KEY,
  title              VARCHAR(120) NOT NULL DEFAULT '',
  child_name         VARCHAR(20)  NOT NULL,
  status             VARCHAR(32)  NOT NULL,
  error_message      VARCHAR(500) NULL,
  drawing_url        VARCHAR(255) NULL,
  style_descriptor   JSON         NULL,
  imagination_prompt TEXT         NOT NULL,
  created_at         DATETIME(3)  NOT NULL,
  updated_at         DATETIME(3)  NOT NULL,
  INDEX idx_story_created_at (created_at DESC)
);

CREATE TABLE page (
  id                  CHAR(36)    PRIMARY KEY,
  story_id            CHAR(36)    NOT NULL,
  page_number         INT         NOT NULL,
  layout              VARCHAR(16) NOT NULL,
  body_text           TEXT        NULL,
  illustration_prompt TEXT        NULL,
  illustration_url    VARCHAR(255) NULL,
  CONSTRAINT fk_page_story FOREIGN KEY (story_id) REFERENCES story(id) ON DELETE CASCADE,
  UNIQUE KEY uk_page_story_number (story_id, page_number)
);

CREATE TABLE orders (
  id              CHAR(36)    PRIMARY KEY,
  story_id        CHAR(36)    NOT NULL,
  recipient_name  VARCHAR(30) NOT NULL,
  address_memo    TEXT        NULL,
  status          VARCHAR(16) NOT NULL,
  status_history  JSON        NOT NULL,
  created_at      DATETIME(3) NOT NULL,
  updated_at      DATETIME(3) NOT NULL,
  CONSTRAINT fk_order_story FOREIGN KEY (story_id) REFERENCES story(id),
  INDEX idx_order_status (status),
  INDEX idx_order_created_at (created_at DESC)
);

CREATE TABLE order_item (
  id          CHAR(36)   PRIMARY KEY,
  order_id    CHAR(36)   NOT NULL,
  book_size   VARCHAR(8) NOT NULL,
  cover_type  VARCHAR(8) NOT NULL,
  copies      INT        NOT NULL,
  CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
