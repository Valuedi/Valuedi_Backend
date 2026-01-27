INSERT INTO category (code, name, sort_order, is_active, created_at, updated_at) VALUES
('FOOD', '식비', 1, true, now(), now()),
('CAFE', '카페/간식', 2, true, now(), now()),
('TRANSPORT', '교통', 3, true, now(), now()),
('SHOPPING', '쇼핑', 4, true, now(), now()),
('CULTURE', '문화/여가', 5, true, now(), now()),
('LIVING', '생활', 6, true, now(), now()),
('HOUSING', '주거/통신', 7, true, now(), now()),
('FINANCE', '금융/보험', 8, true, now(), now()),
('HEALTH', '의료/건강', 9, true, now(), now()),
('EDUCATION', '교육/학습', 10, true, now(), now()),
('CHILD', '자녀/육아', 11, true, now(), now()),
('PET', '반려동물', 12, true, now(), now()),
('GIFT', '경조/선물', 13, true, now(), now()),
('INCOME', '수입', 90, true, now(), now()),
('TRANSFER', '이체', 91, true, now(), now()),
('WITHDRAWAL', '출금', 92, true, now(), now()),
('ETC', '기타', 99, true, now(), now())
ON CONFLICT (code) DO NOTHING;
