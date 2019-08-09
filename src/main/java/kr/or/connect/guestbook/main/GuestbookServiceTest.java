package kr.or.connect.guestbook.main;

import kr.or.connect.guestbook.config.ApplicationConfig;
import kr.or.connect.guestbook.dto.Guestbook;
import kr.or.connect.guestbook.service.GuestbookService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Date;

public class GuestbookServiceTest {
    public static void main(String[] args) {
        ApplicationContext ac = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        GuestbookService guestbookService = ac.getBean(GuestbookService.class);

        Guestbook guestbook = new Guestbook();
        guestbook.setName("kang kyungmi22");
        guestbook.setContent("반갑습니다. 여러분.");
        guestbook.setRegdate(new Date());
        Guestbook result = guestbookService.addGuestbook(guestbook, "127.0.0.1");
        System.out.println(result);
    }
}
