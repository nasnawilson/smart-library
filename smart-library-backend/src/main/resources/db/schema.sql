CREATE DATABASE IF NOT EXISTS smart_library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_library;

DROP TABLE IF EXISTS `reserve_record`;
DROP TABLE IF EXISTS `borrow_record`;
DROP TABLE IF EXISTS `book_list`;
DROP TABLE IF EXISTS `seat_info`;
DROP TABLE IF EXISTS `book_info`;
DROP TABLE IF EXISTS `sys_user`;

-- 1. 用户表
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` VARCHAR(20) NOT NULL COMMENT '账号（读者=11位学号，管理员=admin）',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 哈希，长度60）',
  `dept` VARCHAR(50) COMMENT '院系',
  `class` VARCHAR(50) COMMENT '班级',
  `violation_count` INT DEFAULT 0 COMMENT '违规次数',
  `forbid_time` DATETIME COMMENT '预约权限禁用截止时间',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色（ADMIN=管理员/USER=读者）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 图书表
CREATE TABLE `book_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `book_name` VARCHAR(100) NOT NULL COMMENT '书名',
  `author` VARCHAR(50) COMMENT '作者',
  `isbn` VARCHAR(20) COMMENT 'ISBN',
  `publisher` VARCHAR(50) COMMENT '出版社',
  `category` VARCHAR(30) COMMENT '分类（学科/热门/新书）',
  `cover` VARCHAR(255) COMMENT '封面URL（模拟）',
  `intro` TEXT COMMENT '简介',
  `location` VARCHAR(50) NOT NULL COMMENT '书架位置',
  `total_num` INT NOT NULL DEFAULT 0 COMMENT '总数量',
  `borrow_num` INT NOT NULL DEFAULT 0 COMMENT '已借数量',
  `in_library` TINYINT(1) DEFAULT 1 COMMENT '是否有可借图书（1=是/0=否）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书信息表';

-- 3. 座位表
CREATE TABLE `seat_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `floor` INT NOT NULL COMMENT '楼层（1/2）',
  `seat_num` VARCHAR(10) NOT NULL COMMENT '座位号',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0=空闲/1=占用/2=即将释放）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_floor_seat` (`floor`,`seat_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位信息表';

-- 4. 借阅记录表
CREATE TABLE `borrow_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `borrow_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '借阅时间',
  `due_time` DATETIME COMMENT '到期时间',
  `renew_flag` TINYINT(1) DEFAULT 0 COMMENT '是否续借（1=是/0=否）',
  `return_flag` TINYINT(1) DEFAULT 0 COMMENT '是否归还（1=是/0=否）',
  PRIMARY KEY (`id`),
  KEY `idx_user_book` (`user_id`,`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书借阅记录表';

-- 5. 预约记录表
CREATE TABLE `reserve_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `seat_id` BIGINT NOT NULL COMMENT '座位ID',
  `reserve_date` DATE NOT NULL COMMENT '预约日期',
  `time_period` VARCHAR(20) NOT NULL COMMENT '时段（上午/下午/晚上）',
  `sign_time` DATETIME COMMENT '签到时间',
  `leave_time` DATETIME COMMENT '临时离开开始时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0=待签到/1=已使用/2=超时未签/3=临时离开）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，用于判断预约后15分钟未签到',
  PRIMARY KEY (`id`),
  KEY `idx_user_seat` (`user_id`,`seat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位预约记录表';

-- 6. 书单表
CREATE TABLE `book_list` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `list_name` VARCHAR(100) NOT NULL COMMENT '书单名称',
  `desc` TEXT COMMENT '书单描述',
  `book_ids` VARCHAR(255) COMMENT '图书ID集合（逗号分隔）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书单信息表';

-- 管理员账号：admin / admin
-- 前端注册接口已禁止注册 admin，这个账号只能由数据库脚本或启动迁移创建；
-- 后端启动时会自动把这里的明文密码升级为 BCrypt 哈希。
INSERT INTO `sys_user` (`student_id`, `password`, `dept`, `class`, `role`) VALUES
('admin', 'admin', '系统管理', '管理员', 'ADMIN');

INSERT INTO `seat_info` (`floor`, `seat_num`, `status`)
SELECT 1, CONCAT('1-', LPAD(n, 2, '0')), 0 FROM (
SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL
SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30 UNION ALL
SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40 UNION ALL
SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50) t;

INSERT INTO `seat_info` (`floor`, `seat_num`, `status`)
SELECT 2, CONCAT('2-', LPAD(n, 2, '0')), 0 FROM (
SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL
SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30 UNION ALL
SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40 UNION ALL
SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50) t;

INSERT INTO `book_info` (`book_name`, `author`, `isbn`, `publisher`, `category`, `cover`, `intro`, `location`, `total_num`, `borrow_num`, `in_library`) VALUES
('Python编程：从入门到实践','埃里克·马瑟斯','9787115428028','人民邮电出版社','热门','https://img.example.com/python.jpg','Python入门经典教材','1楼A区01架',5,2,1),
('数据结构与算法分析（Java版）','马克·艾伦·维斯','9787111640127','机械工业出版社','学科','https://img.example.com/ds.jpg','数据结构经典教材','1楼B区05架',3,1,1),
('考研英语真题大全解','张剑','9787519306326','群言出版社','考研','https://img.example.com/kyyy.jpg','考研英语真题解析','2楼C区03架',10,5,1),
('百年孤独','加西亚·马尔克斯','9787544291167','南海出版公司','文学','https://img.example.com/bngd.jpg','魔幻现实主义经典','2楼D区08架',4,4,0),
('经济学原理（微观分册）','曼昆','9787301326261','北京大学出版社','学科','https://img.example.com/jjx.jpg','经济学入门教材','1楼E区02架',6,3,1),
('JavaScript高级程序设计','马特·弗里斯比','9787115545381','人民邮电出版社','热门','https://img.example.com/js.jpg','JS进阶经典','1楼A区08架',2,0,1),
('红楼梦','曹雪芹','9787020108834','人民文学出版社','文学','https://img.example.com/hlm.jpg','四大名著之一','2楼D区01架',8,3,1),
('金融市场学','张亦春','9787040509347','高等教育出版社','学科','https://img.example.com/jrsc.jpg','金融专业核心教材','2楼E区06架',4,2,1),
('算法图解','巴尔加瓦','9787115447630','人民邮电出版社','热门','https://img.example.com/sf.jpg','算法入门图解','1楼B区09架',5,3,1),
('平凡的世界','路遥','9787506369206','作家出版社','文学','https://img.example.com/pfdsj.jpg','当代文学经典','2楼D区05架',6,0,1),
('Java编程思想','Bruce Eckel','9787111213826','机械工业出版社','学科','https://img.example.com/java.jpg','Java语言经典著作','1楼A区02架',6,2,1),
('深入理解计算机系统','Randal E. Bryant','9787111544937','机械工业出版社','学科','https://img.example.com/csapp.jpg','计算机系统经典教材','1楼A区03架',5,1,1),
('计算机网络','谢希仁','9787121411748','电子工业出版社','学科','https://img.example.com/net.jpg','网络原理教材','1楼A区04架',7,2,1),
('操作系统概念','Abraham Silberschatz','9787111604362','机械工业出版社','学科','https://img.example.com/os.jpg','操作系统经典教材','1楼A区05架',4,1,1),
('数据库系统概念','Abraham Silberschatz','9787111375293','机械工业出版社','学科','https://img.example.com/db.jpg','数据库理论教材','1楼A区06架',6,1,1),
('机器学习','周志华','9787302423287','清华大学出版社','热门','https://img.example.com/ml.jpg','机器学习入门经典','1楼A区07架',5,4,1),
('深度学习','Ian Goodfellow','9787115461476','人民邮电出版社','热门','https://img.example.com/dl.jpg','深度学习权威教材','1楼A区09架',3,2,1),
('软件工程导论','张海藩','9787302330981','清华大学出版社','学科','https://img.example.com/se.jpg','软件工程基础教材','1楼A区10架',8,0,1),
('C程序设计','谭浩强','9787302224464','清华大学出版社','学科','https://img.example.com/c.jpg','C语言基础教材','1楼A区11架',10,3,1),
('Vue.js设计与实现','霍春阳','9787115583864','人民邮电出版社','新书','https://img.example.com/vue.jpg','Vue源码与设计思想','1楼A区12架',4,1,1),
('高等数学','同济大学数学系','9787040396633','高等教育出版社','学科','https://img.example.com/math.jpg','大学数学基础教材','2楼C区01架',12,6,1),
('线性代数','同济大学数学系','9787040396619','高等教育出版社','学科','https://img.example.com/linear.jpg','线性代数教材','2楼C区02架',10,2,1),
('概率论与数理统计','浙江大学','9787040516604','高等教育出版社','学科','https://img.example.com/prob.jpg','概率统计教材','2楼C区04架',9,3,1),
('数学分析','华东师范大学数学系','9787040313067','高等教育出版社','学科','https://img.example.com/analysis.jpg','数学专业基础课程','2楼C区05架',5,1,1),
('离散数学','屈婉玲','9787040419080','高等教育出版社','学科','https://img.example.com/discrete.jpg','计算机数学基础','2楼C区06架',7,2,1),
('大学英语综合教程','李荫华','9787544648329','上海外语教育出版社','英语','https://img.example.com/english.jpg','大学英语教材','2楼C区07架',15,6,1),
('新概念英语3','亚历山大','9787560013480','外语教学与研究出版社','英语','https://img.example.com/nce3.jpg','英语学习经典','2楼C区08架',9,3,1),
('雅思词汇真经','刘洪波','9787519301697','中国人民大学出版社','英语','https://img.example.com/ielts.jpg','雅思备考词汇','2楼C区09架',6,4,1),
('考研词汇闪过','张剑','9787519307774','群言出版社','考研','https://img.example.com/words.jpg','考研英语词汇','2楼C区10架',12,7,1),
('英语语法新思维','张满胜','9787513595498','外语教学与研究出版社','英语','https://img.example.com/grammar.jpg','英语语法系统讲解','2楼C区11架',5,2,1),
('三体','刘慈欣','9787536692930','重庆出版社','文学','https://img.example.com/threebody.jpg','中国科幻代表作','2楼D区02架',9,8,1),
('活着','余华','9787506365437','作家出版社','文学','https://img.example.com/huozhe.jpg','当代文学经典','2楼D区03架',8,5,1),
('围城','钱钟书','9787020098095','人民文学出版社','文学','https://img.example.com/weicheng.jpg','现代文学经典','2楼D区04架',6,1,1),
('月亮与六便士','毛姆','9787532749003','上海译文出版社','文学','https://img.example.com/moon.jpg','外国文学经典','2楼D区06架',5,2,1),
('局外人','阿尔贝·加缪','9787532761760','上海译文出版社','文学','https://img.example.com/outsider.jpg','存在主义文学','2楼D区07架',4,1,1),
('人类简史','尤瓦尔·赫拉利','9787508647357','中信出版社','历史','https://img.example.com/sapiens.jpg','人类文明发展史','2楼D区09架',7,4,1),
('中国近代史','蒋廷黻','9787543899951','湖南人民出版社','历史','https://img.example.com/history.jpg','中国近代史入门','2楼D区10架',6,2,1),
('明朝那些事儿','当年明月','9787801656087','中国海关出版社','历史','https://img.example.com/ming.jpg','通俗历史读物','2楼D区11架',10,6,1),
('全球通史','斯塔夫里阿诺斯','9787301109489','北京大学出版社','历史','https://img.example.com/global.jpg','世界历史经典','2楼D区12架',5,1,1),
('史记','司马迁','9787101003048','中华书局','历史','https://img.example.com/shiji.jpg','纪传体通史经典','2楼D区13架',8,3,1),
('管理学','斯蒂芬·罗宾斯','9787300234604','中国人民大学出版社','学科','https://img.example.com/manage.jpg','管理学基础教材','1楼E区03架',7,2,1),
('市场营销学','菲利普·科特勒','9787300168664','中国人民大学出版社','学科','https://img.example.com/marketing.jpg','营销管理经典','1楼E区04架',6,2,1),
('会计学原理','葛家澍','9787300275669','中国人民大学出版社','学科','https://img.example.com/accounting.jpg','会计基础教材','1楼E区05架',8,4,1),
('财务管理','荆新','9787300279551','中国人民大学出版社','学科','https://img.example.com/finance.jpg','财务管理教材','1楼E区06架',6,3,1),
('宏观经济学','曼昆','9787301256889','北京大学出版社','学科','https://img.example.com/macro.jpg','宏观经济学教材','1楼E区07架',5,1,1),
('考研数学复习全书','李永乐','9787568246538','北京理工大学出版社','考研','https://img.example.com/kysx.jpg','考研数学复习资料','2楼C区12架',10,8,1),
('考研政治核心考案','徐涛','9787040587499','高等教育出版社','考研','https://img.example.com/kyzz.jpg','考研政治复习资料','2楼C区13架',12,9,1),
('设计模式','Erich Gamma','9787111075752','机械工业出版社','学科','https://img.example.com/pattern.jpg','面向对象设计模式','1楼A区13架',4,1,1),
('代码整洁之道','Robert C. Martin','9787115216878','人民邮电出版社','热门','https://img.example.com/clean.jpg','软件开发实践经典','1楼A区14架',5,2,1),
('人工智能：一种现代方法','Stuart Russell','9787115598119','人民邮电出版社','新书','https://img.example.com/ai.jpg','人工智能综合教材','1楼A区15架',3,1,1);

-- 借阅/预约记录不再预置测试数据，从空库开始更贴近真实使用

INSERT INTO `book_list` (`list_name`, `desc`, `book_ids`) VALUES
('Python入门必备','零基础入门Python的经典书单','1,6,9,20'),
('考研英语高分攻略','考研英语复习核心书单','3,28,29,30'),
('计算机专业核心教材','计科专业必读书目','2,11,12,13,14,15'),
('文学经典必读','中外文学经典作品','4,7,10,31,32,33,34,35'),
('金融专业入门','金融新手必学书单','8,5,43,44,45'),
('算法与数据结构','算法能力提升书单','2,9,48'),
('人工智能入门','AI与机器学习入门路线','16,17,50'),
('Web前端进阶','前端工程师进阶书单','6,20,49'),
('数学基础强化','大学数学和考研数学基础','21,22,23,24,25,46'),
('英语能力提升','四六级、雅思、考研英语通用','26,27,28,29,30'),
('历史通识阅读','历史兴趣与通识提升','36,37,38,39,40'),
('经管专业核心','经管类专业基础书单','5,41,42,43,44,45'),
('软件工程实践','软件工程与代码质量','18,48,49'),
('考研公共课资料','考研公共课常用资料','3,29,46,47'),
('新书推荐','近期推荐新书','20,50');
