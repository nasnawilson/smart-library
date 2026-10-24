package com.smartlibrary.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlibrary.entity.Book;
import com.smartlibrary.service.BookService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 图书基础数据修正器。
 * 用于确保 50 本图书都有可用于搜索的业务分类和楼层位置。
 */
@Component
public class BookDataInitializer {

    @Autowired
    private BookService bookService;

    @PostConstruct
    public void initBooks() {
        books().forEach(this::upsertBook);
    }

    private void upsertBook(Book seed) {
        Book existing = bookService.getOne(new QueryWrapper<Book>().eq("isbn", seed.getIsbn()).last("LIMIT 1"), false);
        if (existing == null) {
            bookService.save(seed);
            return;
        }
        Book update = new Book();
        update.setId(existing.getId());
        update.setTitle(seed.getTitle());
        update.setAuthor(seed.getAuthor());
        update.setPublisher(seed.getPublisher());
        update.setCategory(seed.getCategory());
        update.setCoverImage(seed.getCoverImage());
        update.setDescription(seed.getDescription());
        update.setLocation(seed.getLocation());
        update.setTotalCount(seed.getTotalCount());
        Integer borrowed = existing.getBorrowNum() == null ? seed.getBorrowNum() : existing.getBorrowNum();
        update.setBorrowNum(borrowed);
        update.setInLibrary(borrowed < seed.getTotalCount() ? 1 : 0);
        bookService.updateById(update);
    }

    private Book book(String title, String author, String isbn, String publisher, String category, String intro, String location, int total, int borrowed) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublisher(publisher);
        book.setCategory(category);
        book.setCoverImage("");
        book.setDescription(intro);
        book.setLocation(location);
        book.setTotalCount(total);
        book.setBorrowNum(borrowed);
        book.setInLibrary(borrowed < total ? 1 : 0);
        return book;
    }

    private List<Book> books() {
        return Arrays.asList(
                book("Python编程：从入门到实践", "埃里克·马瑟斯", "9787115428028", "人民邮电出版社", "计算机", "Python入门经典教材", "1楼A区01架", 5, 2),
                book("数据结构与算法分析（Java版）", "马克·艾伦·维斯", "9787111640127", "机械工业出版社", "计算机", "数据结构经典教材", "1楼A区02架", 3, 1),
                book("考研英语真题大全解", "张剑", "9787519306326", "群言出版社", "考研", "考研英语真题解析", "2楼C区01架", 10, 5),
                book("百年孤独", "加西亚·马尔克斯", "9787544291167", "南海出版公司", "文学", "魔幻现实主义经典", "2楼D区01架", 4, 4),
                book("经济学原理（微观分册）", "曼昆", "9787301326261", "北京大学出版社", "经管", "经济学入门教材", "1楼E区01架", 6, 3),
                book("JavaScript高级程序设计", "马特·弗里斯比", "9787115545381", "人民邮电出版社", "计算机", "JS进阶经典", "1楼A区03架", 2, 0),
                book("红楼梦", "曹雪芹", "9787020108834", "人民文学出版社", "文学", "四大名著之一", "2楼D区02架", 8, 3),
                book("金融市场学", "张亦春", "9787040509347", "高等教育出版社", "经管", "金融专业核心教材", "1楼E区02架", 4, 2),
                book("算法图解", "巴尔加瓦", "9787115447630", "人民邮电出版社", "计算机", "算法入门图解", "1楼A区04架", 5, 3),
                book("平凡的世界", "路遥", "9787506369206", "作家出版社", "文学", "当代文学经典", "2楼D区03架", 6, 0),
                book("Java编程思想", "Bruce Eckel", "9787111213826", "机械工业出版社", "计算机", "Java语言经典著作", "1楼A区05架", 6, 2),
                book("深入理解计算机系统", "Randal E. Bryant", "9787111544937", "机械工业出版社", "计算机", "计算机系统经典教材", "1楼A区06架", 5, 1),
                book("计算机网络", "谢希仁", "9787121411748", "电子工业出版社", "计算机", "网络原理教材", "1楼A区07架", 7, 2),
                book("操作系统概念", "Abraham Silberschatz", "9787111604362", "机械工业出版社", "计算机", "操作系统经典教材", "1楼A区08架", 4, 1),
                book("数据库系统概念", "Abraham Silberschatz", "9787111375293", "机械工业出版社", "计算机", "数据库理论教材", "1楼A区09架", 6, 1),
                book("机器学习", "周志华", "9787302423287", "清华大学出版社", "计算机", "机器学习入门经典", "1楼A区10架", 5, 4),
                book("深度学习", "Ian Goodfellow", "9787115461476", "人民邮电出版社", "计算机", "深度学习权威教材", "1楼A区11架", 3, 2),
                book("软件工程导论", "张海藩", "9787302330981", "清华大学出版社", "计算机", "软件工程基础教材", "1楼A区12架", 8, 0),
                book("C程序设计", "谭浩强", "9787302224464", "清华大学出版社", "计算机", "C语言基础教材", "1楼A区13架", 10, 3),
                book("Vue.js设计与实现", "霍春阳", "9787115583864", "人民邮电出版社", "计算机", "Vue源码与设计思想", "1楼A区14架", 4, 1),
                book("高等数学", "同济大学数学系", "9787040396633", "高等教育出版社", "数学", "大学数学基础教材", "2楼C区02架", 12, 6),
                book("线性代数", "同济大学数学系", "9787040396619", "高等教育出版社", "数学", "线性代数教材", "2楼C区03架", 10, 2),
                book("概率论与数理统计", "浙江大学", "9787040516604", "高等教育出版社", "数学", "概率统计教材", "2楼C区04架", 9, 3),
                book("数学分析", "华东师范大学数学系", "9787040313067", "高等教育出版社", "数学", "数学专业基础课程", "2楼C区05架", 5, 1),
                book("离散数学", "屈婉玲", "9787040419080", "高等教育出版社", "计算机", "计算机数学基础", "1楼A区15架", 7, 2),
                book("大学英语综合教程", "李荫华", "9787544648329", "上海外语教育出版社", "英语", "大学英语教材", "2楼C区06架", 15, 6),
                book("新概念英语3", "亚历山大", "9787560013480", "外语教学与研究出版社", "英语", "英语学习经典", "2楼C区07架", 9, 3),
                book("雅思词汇真经", "刘洪波", "9787519301697", "中国人民大学出版社", "英语", "雅思备考词汇", "2楼C区08架", 6, 4),
                book("考研词汇闪过", "张剑", "9787519307774", "群言出版社", "考研", "考研英语词汇", "2楼C区09架", 12, 7),
                book("英语语法新思维", "张满胜", "9787513595498", "外语教学与研究出版社", "英语", "英语语法系统讲解", "2楼C区10架", 5, 2),
                book("三体", "刘慈欣", "9787536692930", "重庆出版社", "文学", "中国科幻代表作", "2楼D区04架", 9, 8),
                book("活着", "余华", "9787506365437", "作家出版社", "文学", "当代文学经典", "2楼D区05架", 8, 5),
                book("围城", "钱钟书", "9787020098095", "人民文学出版社", "文学", "现代文学经典", "2楼D区06架", 6, 1),
                book("月亮与六便士", "毛姆", "9787532749003", "上海译文出版社", "文学", "外国文学经典", "2楼D区07架", 5, 2),
                book("局外人", "阿尔贝·加缪", "9787532761760", "上海译文出版社", "文学", "存在主义文学", "2楼D区08架", 4, 1),
                book("人类简史", "尤瓦尔·赫拉利", "9787508647357", "中信出版社", "历史", "人类文明发展史", "2楼E区01架", 7, 4),
                book("中国近代史", "蒋廷黻", "9787543899951", "湖南人民出版社", "历史", "中国近代史入门", "2楼E区02架", 6, 2),
                book("明朝那些事儿", "当年明月", "9787801656087", "中国海关出版社", "历史", "通俗历史读物", "2楼E区03架", 10, 6),
                book("全球通史", "斯塔夫里阿诺斯", "9787301109489", "北京大学出版社", "历史", "世界历史经典", "2楼E区04架", 5, 1),
                book("史记", "司马迁", "9787101003048", "中华书局", "历史", "纪传体通史经典", "2楼E区05架", 8, 3),
                book("管理学", "斯蒂芬·罗宾斯", "9787300234604", "中国人民大学出版社", "经管", "管理学基础教材", "1楼E区03架", 7, 2),
                book("市场营销学", "菲利普·科特勒", "9787300168664", "中国人民大学出版社", "经管", "营销管理经典", "1楼E区04架", 6, 2),
                book("会计学原理", "葛家澍", "9787300275669", "中国人民大学出版社", "经管", "会计基础教材", "1楼E区05架", 8, 4),
                book("财务管理", "荆新", "9787300279551", "中国人民大学出版社", "经管", "财务管理教材", "1楼E区06架", 6, 3),
                book("宏观经济学", "曼昆", "9787301256889", "北京大学出版社", "经管", "宏观经济学教材", "1楼E区07架", 5, 1),
                book("考研数学复习全书", "李永乐", "9787568246538", "北京理工大学出版社", "考研", "考研数学复习资料", "2楼C区11架", 10, 8),
                book("考研政治核心考案", "徐涛", "9787040587499", "高等教育出版社", "考研", "考研政治复习资料", "2楼C区12架", 12, 9),
                book("设计模式", "Erich Gamma", "9787111075752", "机械工业出版社", "计算机", "面向对象设计模式", "1楼A区16架", 4, 1),
                book("代码整洁之道", "Robert C. Martin", "9787115216878", "人民邮电出版社", "计算机", "软件开发实践经典", "1楼A区17架", 5, 2),
                book("人工智能：一种现代方法", "Stuart Russell", "9787115598119", "人民邮电出版社", "计算机", "人工智能综合教材", "1楼A区18架", 3, 1)
        );
    }
}
