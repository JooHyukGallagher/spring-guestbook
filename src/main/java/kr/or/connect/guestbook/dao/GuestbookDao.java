package kr.or.connect.guestbook.dao;

import kr.or.connect.guestbook.dto.Guestbook;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

import static kr.or.connect.guestbook.dao.GuestbookDaoSqls.*;

@Repository
public class GuestbookDao {
    private NamedParameterJdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertAction;
    private RowMapper<Guestbook> rowMapper = BeanPropertyRowMapper.newInstance(Guestbook.class);

    public GuestbookDao(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.insertAction = new SimpleJdbcInsert(dataSource)
                .withTableName("guestbook")
                // id 자동 입력
                .usingGeneratedKeyColumns("id");
    }

    public List<Guestbook> selectAll(Integer start, Integer limit) {
        Map<String, Integer> params = new HashMap<>();
        params.put("start", start);
        params.put("limit", limit);
        return jdbcTemplate.query(SELECT_PAGING, params, rowMapper);
    }

    public Long insert(Guestbook guestbook){
        SqlParameterSource params = new BeanPropertySqlParameterSource(guestbook);
        // insert문을 내부적으로 생성해서 실행하고 자동으로 생성된 id 값을 리턴
        return insertAction.executeAndReturnKey(params).longValue();
    }

    public int deleteById(Long id){
        Map<String, ?> params = Collections.singletonMap("id", id);
        return jdbcTemplate.update(DELETE_BY_ID, params);
    }

    public int selectCount() {
        return jdbcTemplate.queryForObject(SELECT_COUNT, Collections.<String, Object>emptyMap(), Integer.class);
    }
}
