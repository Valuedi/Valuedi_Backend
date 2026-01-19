INSERT INTO terms (code, title, is_required, version, content_text, effective_from, is_active)
VALUES
    ('AGE_14', '만 14세 이상입니다.', true,  '1.0', '만 14세 이상만 가입 가능합니다.', NOW(), true),
    ('SERVICE', '서비스 이용약관 동의', true,  '1.0', '서비스 이용약관 전문...', NOW(), true),
    ('PRIVACY', '개인정보 수집 및 이용 동의', true, '1.0', '개인정보 처리방침 전문...', NOW(), true),
    ('MARKETING', '마케팅 목적의 개인정보 수집 및 이용 동의', false, '1.0', '마케팅 동의 전문...', NOW(), true),
    ('SECURITY', '전자금융거래 이용약관 동의', true,  '1.0', '전자금융거래 약관 전문...', NOW(), true)
    ON DUPLICATE KEY UPDATE
                         title = VALUES(title),
                         is_required = VALUES(is_required),
                         version = VALUES(version),
                         content_text = VALUES(content_text),
                         effective_from = VALUES(effective_from),
                         is_active = VALUES(is_active);
