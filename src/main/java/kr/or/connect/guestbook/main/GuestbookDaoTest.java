package kr.or.connect.guestbook.main;

import kr.or.connect.guestbook.config.ApplicationConfig;
import kr.or.connect.guestbook.dao.LogDao;
import kr.or.connect.guestbook.dto.Guestbook;
import kr.or.connect.guestbook.dto.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Date;

public class GuestbookDaoTest {
    public static void main(String[] args) {
        ApplicationContext ac = new AnnotationConfigApplicationContext(ApplicationConfig.class);
//        GuestbookDao guestbookDao = ac.getBean(GuestbookDao.class);
//
//        Guestbook guestbook = new Guestbook();
//        guestbook.setName("김주혁");
//        guestbook.setContent("안녕하세요. 엘지트윈스 팬 입니다.");
//        guestbook.setRegdate(new Date());
//        Long id = guestbookDao.insert(guestbook);
//        System.out.println("id : " + id);

        LogDao logDao = ac.getBean(LogDao.class);
        Log log = new Log();
        log.setIp("127.0.0.1");
        log.setMethod("insert");
        log.setRegdate(new Date());
        logDao.insert(log);
    }
}
