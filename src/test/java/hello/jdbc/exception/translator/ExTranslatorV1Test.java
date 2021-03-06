package hello.jdbc.exception.translator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import hello.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ExTranslatorV1Test { // 예외를 전환하는 V1 Test

    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myid");
        service.create("myid"); // 같은 ID 저장 시도. 중복 id 에러 메시지를 발생시킨다.
    }

    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 0));
                log.info("saveId = {}", memberId);
            } catch (MyDuplicateKeyException e) {
                log.info("키 중복, 복구 시도");
                // 키를 생성하는 메서드 호출
                String retryId = generateNewId(memberId);
                log.info("retryId={}", retryId);
                repository.save(new Member(retryId, 0));

            } catch (MyDbException e) { // catch는 중복해서 잡을 수 있다
                log.info("데이터 접근 계층 예외", e);
                throw e;
            }
        }

        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }

    }

    @RequiredArgsConstructor
    static class Repository {
        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?,?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;

            } catch (SQLException e) {
                // h2 DB
                if (e.getErrorCode() == 23505) {
                    // 키 중복 예외 발생 시, 예외를 변경한다
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDbException(e); //  키 중복 이외의 모든 예외는 MyDbException으로 전환하여 던진다

            } finally {
                JdbcUtils.closeStatement(pstmt); // PreparedStatement 반납
                JdbcUtils.closeConnection(con); // Connection 반납
            }
        }
    }
}
