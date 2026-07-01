-- 動作確認用のサンプルデータ。
-- Flyway の管理対象外（db/migration/ ではなく db/ 直下）なので、アプリ起動や jOOQ codegen には影響しない。
-- 投入方法・DB リセットは README「クイックスタート」を参照。
-- 採番 ID（GENERATED ALWAYS）に依存しないよう、中間テーブルは name/title で突き合わせて紐づける。

INSERT INTO authors (name, birth_date) VALUES
  ('村上 春樹', '1949-01-12'),
  ('東野 圭吾', '1958-02-04'),
  ('宮部 みゆき', '1960-12-23'),
  ('伊坂 幸太郎', '1971-05-25'),
  ('谷崎 潤一郎', '1886-07-24');

-- 書籍。UNPUBLISHED / PUBLISHED の両方と、価格0（境界値）を含める。
INSERT INTO books (title, price, publication_status) VALUES
  ('ノルウェイの森',     1200, 'PUBLISHED'),
  ('海辺のカフカ',       1800, 'PUBLISHED'),
  ('容疑者Xの献身',      1500, 'PUBLISHED'),
  ('白夜行',            1600, 'PUBLISHED'),
  ('火車',              1400, 'PUBLISHED'),
  ('ゴールデンスランバー', 1700, 'UNPUBLISHED'),
  ('三人共著サンプル',    2000, 'UNPUBLISHED'),
  ('無料サンプル',          0, 'UNPUBLISHED');

-- 著者と書籍の紐づけ（多対多）。
-- 「三人共著サンプル」は3著者、「無料サンプル」は2著者を持ち、複数著者を確認できる。
INSERT INTO author_books (author_id, book_id)
SELECT a.id, b.id
FROM authors a, books b
WHERE (a.name = '村上 春樹'   AND b.title = 'ノルウェイの森')
   OR (a.name = '村上 春樹'   AND b.title = '海辺のカフカ')
   OR (a.name = '東野 圭吾'   AND b.title = '容疑者Xの献身')
   OR (a.name = '東野 圭吾'   AND b.title = '白夜行')
   OR (a.name = '宮部 みゆき' AND b.title = '火車')
   OR (a.name = '伊坂 幸太郎' AND b.title = 'ゴールデンスランバー')
   OR (a.name = '村上 春樹'   AND b.title = '三人共著サンプル')
   OR (a.name = '東野 圭吾'   AND b.title = '三人共著サンプル')
   OR (a.name = '宮部 みゆき' AND b.title = '三人共著サンプル')
   OR (a.name = '伊坂 幸太郎' AND b.title = '無料サンプル')
   OR (a.name = '宮部 みゆき' AND b.title = '無料サンプル');
