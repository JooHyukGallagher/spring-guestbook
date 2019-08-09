package kr.or.connect.guestbook.service;

import kr.or.connect.guestbook.dto.Guestbook;

import java.util.List;

public interface GuestbookService {
    public static final int LIMIT = 5;
    public List<Guestbook> getGuestBooks(int start);
    public int deleteGuestbook(Long id, String ip);
    public Guestbook addGuestbook(Guestbook guestbook, String ip);
    public int getCount();
}
