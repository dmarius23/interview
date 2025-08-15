-- =========================================================
-- Clean up (drop children first due to FK dependencies)
-- =========================================================
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS rental_location;
DROP TABLE IF EXISTS car_model;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS rental_company;

-- =========================================================
-- Parent tables
-- =========================================================

-- Companies
CREATE TABLE rental_company (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(120) NOT NULL,
  created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    VARCHAR(100),
  updated_by    VARCHAR(100),
  deleted    BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at TIMESTAMP NULL,
  version    BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_company_name ON rental_company (name);

-- Clients (minimal; extend as needed)
CREATE TABLE client (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  email       VARCHAR(160) NOT NULL,
  full_name   VARCHAR(160),
  created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    VARCHAR(100),
  updated_by    VARCHAR(100)
);

ALTER TABLE client ADD CONSTRAINT uq_client_email UNIQUE (email);

-- Car catalog
CREATE TABLE car_model (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  make          VARCHAR(80)  NOT NULL,
  model         VARCHAR(80)  NOT NULL,
  vehicle_class VARCHAR(40),
  seats         INT,
  created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    VARCHAR(100),
  updated_by    VARCHAR(100)
);

ALTER TABLE car_model ADD CONSTRAINT uq_carmodel_make_model UNIQUE (make, model);
CREATE INDEX idx_carmodel_make ON car_model (make);

-- Locations
CREATE TABLE rental_location (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id  BIGINT NOT NULL,
  name        VARCHAR(120) NOT NULL,
  city        VARCHAR(120) NOT NULL,
  country     VARCHAR(120),
  created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    VARCHAR(100),
  updated_by    VARCHAR(100),
  deleted     BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at  TIMESTAMP NULL,
  version     BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_location_company FOREIGN KEY (company_id) REFERENCES rental_company(id)
);

ALTER TABLE rental_location ADD CONSTRAINT uq_location_company_name UNIQUE (company_id, name);
CREATE INDEX idx_location_city ON rental_location (city);

-- =========================================================
-- Inventory and transactions
-- =========================================================

-- Cars
CREATE TABLE car (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id          BIGINT NOT NULL,
  model_id            BIGINT NOT NULL,
  current_location_id BIGINT,
  vin                 VARCHAR(64)  NOT NULL,
  plate_number        VARCHAR(32)  NOT NULL,
  mileage_km          INT DEFAULT 0,
  daily_price_in_cents       INT NOT NULL,          -- cents
  status              VARCHAR(32) NOT NULL,  -- e.g., AVAILABLE, RESERVED, RENTED, MAINTENANCE
  created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    VARCHAR(100),
  updated_by    VARCHAR(100),
  deleted             BOOLEAN NOT NULL DEFAULT FALSE,
  deleted_at          TIMESTAMP NULL,
  version             BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_car_company   FOREIGN KEY (company_id)          REFERENCES rental_company(id),
  CONSTRAINT fk_car_model     FOREIGN KEY (model_id)            REFERENCES car_model(id),
  CONSTRAINT fk_car_location  FOREIGN KEY (current_location_id) REFERENCES rental_location(id)
);

ALTER TABLE car ADD CONSTRAINT uq_car_vin UNIQUE (vin);
ALTER TABLE car ADD CONSTRAINT uq_car_plate UNIQUE (plate_number);
CREATE INDEX idx_car_company       ON car (company_id);
CREATE INDEX idx_car_model         ON car (model_id);
CREATE INDEX idx_car_location      ON car (current_location_id);
CREATE INDEX idx_car_status        ON car (status);

-- Bookings
CREATE TABLE booking (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  client_id          BIGINT NOT NULL,
  car_id             BIGINT NOT NULL,
  pickup_location_id BIGINT NOT NULL,
  return_location_id BIGINT NOT NULL,
  pickup_time        TIMESTAMP NOT NULL,
  return_time        TIMESTAMP NOT NULL,
  status             VARCHAR(32) NOT NULL,  -- e.g., CREATED, CONFIRMED, CANCELED, COMPLETED
  total_price_cents  INT NOT NULL,
  version            BIGINT NOT NULL DEFAULT 0,
  created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    VARCHAR(100),
  updated_by    VARCHAR(100),
  CONSTRAINT fk_booking_client  FOREIGN KEY (client_id)          REFERENCES client(id),
  CONSTRAINT fk_booking_car     FOREIGN KEY (car_id)             REFERENCES car(id),
  CONSTRAINT fk_booking_pickup  FOREIGN KEY (pickup_location_id) REFERENCES rental_location(id),
  CONSTRAINT fk_booking_return  FOREIGN KEY (return_location_id) REFERENCES rental_location(id)
);

CREATE INDEX idx_booking_car          ON booking (car_id);
CREATE INDEX idx_booking_status       ON booking (status);
CREATE INDEX idx_booking_time_window  ON booking (pickup_time, return_time);

-- Payments
CREATE TABLE payment (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id    BIGINT NOT NULL,
  amount_cents  INT NOT NULL,
  status        VARCHAR(32) NOT NULL, -- e.g., AUTHORIZED, FAILED, PENDING
  provider_reference VARCHAR(100),
  created_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    VARCHAR(100),
  updated_by    VARCHAR(100),
  CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking(id)
);

CREATE INDEX idx_payment_booking ON payment (booking_id);
CREATE INDEX idx_payment_status  ON payment (status);

CREATE TABLE outbox_events (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  aggregate_id   VARCHAR(100) NOT NULL,
  aggregate_type VARCHAR(50)  NOT NULL,
  event_type     VARCHAR(50)  NOT NULL,
  event_data     TEXT,
  status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  processed_at   TIMESTAMP NULL,
  retry_count    INT DEFAULT 0,
  error_message  TEXT,
  created_date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by     VARCHAR(100) DEFAULT 'SYSTEM',
  updated_by     VARCHAR(100) DEFAULT 'SYSTEM'
);
