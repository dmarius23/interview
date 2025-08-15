-- =========================================================
-- Clients (linked to the above company)
-- =========================================================
INSERT INTO client (full_name, email, created_date, updated_date)
VALUES ('Marius D', 'marius@marius.ro', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- =========================================================
-- Companies
-- =========================================================
INSERT INTO rental_company (name,  created_date, updated_date, deleted, deleted_at, version)
VALUES ('Hertz Rentals', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0);

-- =========================================================
-- Locations (linked to the above company)
-- =========================================================
INSERT INTO rental_location (company_id, name, city, country, created_date, updated_date, deleted, deleted_at, version)
VALUES
  (1, 'Downtown Station', 'Bucharest', 'Romania',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP ,FALSE, NULL, 0),
  (1, 'Airport Hub', 'Bucharest', 'Romania', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 'Central Plaza', 'Cluj-Napoca', 'Romania',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 'Cluj Airport', 'Cluj-Napoca', 'Romania',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0);

-- =========================================================
-- Car Models
-- =========================================================
INSERT INTO car_model (make, model, vehicle_class, seats, created_date, updated_date)
VALUES
  ('Toyota', 'Corolla', 'Compact', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Volkswagen', 'Golf', 'Compact', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('BMW', '3 Series', 'Sedan', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Mercedes', 'E-Class', 'Luxury', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =========================================================
-- Cars (2 per model per location)
-- =========================================================
INSERT INTO car (company_id, model_id, current_location_id, vin, plate_number, mileage_km, daily_price_in_cents, status, created_date, updated_date, deleted, deleted_at, version)
VALUES
  (1, 1, 1, 'VIN-TC-001', 'B-100-TC', 25000, 15000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 1, 1, 'VIN-TC-002', 'B-101-TC', 30000, 15000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),

  (1, 1, 2, 'VIN-TC-003', 'B-200-TC', 18000, 15000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 1, 2, 'VIN-TC-004', 'B-201-TC', 22000, 15000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),

  (1, 2, 1, 'VIN-VG-001', 'B-102-VG', 15000, 16000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 2, 1, 'VIN-VG-002', 'B-103-VG', 14000, 16000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),

  (1, 2, 2, 'VIN-VG-003', 'B-202-VG', 17000, 16000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 2, 2, 'VIN-VG-004', 'B-203-VG', 20000, 16000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),

  (1, 3, 1, 'VIN-B3-001', 'B-104-B3', 35000, 20000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 3, 1, 'VIN-B3-002', 'B-105-B3', 37000, 20000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),

  (1, 3, 2, 'VIN-B3-003', 'B-204-B3', 40000, 20000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 3, 2, 'VIN-B3-004', 'B-205-B3', 42000, 20000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),

  (1, 4, 3, 'VIN-ME-001', 'CJ-100-ME', 15000, 30000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0),
  (1, 4, 3, 'VIN-ME-002', 'CJ-101-ME', 18000, 30000, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, NULL, 0);


