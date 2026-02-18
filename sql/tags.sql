-- 1. 태그 데이터 입력 (중복 방지)
INSERT INTO tag (tag_name) VALUES ('#미국대표') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#반도체') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#2차전지') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#배당주') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#헬스케어') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#AI/로봇') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#채권') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#금/은') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#레버리지') ON CONFLICT (tag_name) DO NOTHING;
INSERT INTO tag (tag_name) VALUES ('#인버스') ON CONFLICT (tag_name) DO NOTHING;

-- 2. ETF-태그 매핑 데이터 입력 (예시)
-- 실제 종목코드를 확인하여 매핑해야 합니다.
-- 아래는 예시 데이터입니다.

-- #미국대표 (S&P500, 나스닥100)
-- TIGER 미국S&P500 (360750)
INSERT INTO etf_tag (srtn_cd, tag_id)
SELECT '360750', tag_id FROM tag WHERE tag_name = '#미국대표'
ON CONFLICT DO NOTHING;

-- KODEX 미국나스닥100TR (379810)
INSERT INTO etf_tag (srtn_cd, tag_id)
SELECT '379810', tag_id FROM tag WHERE tag_name = '#미국대표'
ON CONFLICT DO NOTHING;

-- #반도체
-- TIGER 미국필라델피아반도체나스닥 (381180)
INSERT INTO etf_tag (srtn_cd, tag_id)
SELECT '381180', tag_id FROM tag WHERE tag_name = '#반도체'
ON CONFLICT DO NOTHING;

-- KODEX 반도체 (091160)
INSERT INTO etf_tag (srtn_cd, tag_id)
SELECT '091160', tag_id FROM tag WHERE tag_name = '#반도체'
ON CONFLICT DO NOTHING;

-- #2차전지
-- TIGER 2차전지테마 (305540)
INSERT INTO etf_tag (srtn_cd, tag_id)
SELECT '305540', tag_id FROM tag WHERE tag_name = '#2차전지'
ON CONFLICT DO NOTHING;

-- #배당주
-- TIGER 미국배당다우존스 (458730)
INSERT INTO etf_tag (srtn_cd, tag_id)
SELECT '458730', tag_id FROM tag WHERE tag_name = '#배당주'
ON CONFLICT DO NOTHING;
