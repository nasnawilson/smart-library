CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(20) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  dept VARCHAR(50),
  `class` VARCHAR(50),
  violation_count INT DEFAULT 0,
  forbid_time TIMESTAMP,
  role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- 老版本演示库升级：已有 sys_user 表时 CREATE TABLE IF NOT EXISTS 不会加列，这里补上
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE TABLE IF NOT EXISTS book_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  book_name VARCHAR(100) NOT NULL,
  author VARCHAR(50),
  isbn VARCHAR(20) UNIQUE,
  publisher VARCHAR(50),
  category VARCHAR(30),
  cover VARCHAR(255),
  intro CLOB,
  location VARCHAR(50) NOT NULL,
  total_num INT NOT NULL DEFAULT 0,
  borrow_num INT NOT NULL DEFAULT 0,
  in_library TINYINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS seat_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  floor INT NOT NULL,
  seat_num VARCHAR(10) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  UNIQUE (floor, seat_num)
);

CREATE TABLE IF NOT EXISTS borrow_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  borrow_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  due_time TIMESTAMP,
  renew_flag TINYINT DEFAULT 0,
  return_flag TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reserve_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  seat_id BIGINT NOT NULL,
  reserve_date DATE NOT NULL,
  time_period VARCHAR(20) NOT NULL,
  sign_time TIMESTAMP,
  leave_time TIMESTAMP,
  status TINYINT NOT NULL DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book_list (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  list_name VARCHAR(100) NOT NULL UNIQUE,
  `desc` CLOB,
  book_ids VARCHAR(255)
);

INSERT INTO sys_user (student_id, password, dept, `class`, role)
SELECT 'admin', 'admin', '系统管理', '管理员', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE student_id = 'admin');

INSERT INTO seat_info (floor, seat_num, status)
SELECT 1, CONCAT('1-', LPAD(N, 2, '0')), 0
FROM SYSTEM_RANGE(1, 50) AS T(N)
WHERE NOT EXISTS (
  SELECT 1 FROM seat_info WHERE floor = 1 AND seat_num = CONCAT('1-', LPAD(N, 2, '0'))
);

INSERT INTO seat_info (floor, seat_num, status)
SELECT 2, CONCAT('2-', LPAD(N, 2, '0')), 0
FROM SYSTEM_RANGE(1, 50) AS T(N)
WHERE NOT EXISTS (
  SELECT 1 FROM seat_info WHERE floor = 2 AND seat_num = CONCAT('2-', LPAD(N, 2, '0'))
);

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT 'Python编程：从入门到实践', '埃里克·马瑟斯', '9787115428028', '人民邮电出版社', '热门', '', 'Python入门经典教材', '1楼A区01架', 5, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787115428028');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '数据结构与算法分析（Java版）', '马克·艾伦·维斯', '9787111640127', '机械工业出版社', '学科', '', '数据结构经典教材', '1楼B区05架', 6, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787111640127');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '考研英语真题大全解', '张剑', '9787519306326', '群言出版社', '考研', '', '考研英语真题解析', '2楼C区03架', 10, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787519306326');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '百年孤独', '加西亚·马尔克斯', '9787544291167', '南海出版公司', '文学', '', '魔幻现实主义经典', '2楼D区08架', 4, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787544291167');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '经济学原理（微观分册）', '曼昆', '9787301326261', '北京大学出版社', '学科', '', '经济学入门教材', '1楼E区02架', 6, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787301326261');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT 'JavaScript高级程序设计', '马特·弗里斯比', '9787115545381', '人民邮电出版社', '热门', '', 'JS进阶经典', '1楼A区08架', 2, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787115545381');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '红楼梦', '曹雪芹', '9787020108834', '人民文学出版社', '文学', '', '四大名著之一', '2楼D区01架', 8, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787020108834');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '金融市场学', '张亦春', '9787040509347', '高等教育出版社', '学科', '', '金融专业核心教材', '2楼E区06架', 4, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787040509347');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '算法图解', '巴尔加瓦', '9787115447630', '人民邮电出版社', '热门', '', '算法入门图解', '1楼B区09架', 5, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787115447630');

INSERT INTO book_info (book_name, author, isbn, publisher, category, cover, intro, location, total_num, borrow_num, in_library)
SELECT '平凡的世界', '路遥', '9787506369206', '作家出版社', '文学', '', '当代文学经典', '2楼D区05架', 6, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM book_info WHERE isbn = '9787506369206');

INSERT INTO book_list (list_name, `desc`, book_ids)
SELECT 'Python入门必备', '零基础入门 Python 的经典书单', '1,6,9'
WHERE NOT EXISTS (SELECT 1 FROM book_list WHERE list_name = 'Python入门必备');

INSERT INTO book_list (list_name, `desc`, book_ids)
SELECT '计算机专业核心教材', '计科专业必读书目', '1,2,6,9'
WHERE NOT EXISTS (SELECT 1 FROM book_list WHERE list_name = '计算机专业核心教材');

INSERT INTO book_list (list_name, `desc`, book_ids)
SELECT '文学经典必读', '中外文学经典作品', '4,7,10'
WHERE NOT EXISTS (SELECT 1 FROM book_list WHERE list_name = '文学经典必读');
