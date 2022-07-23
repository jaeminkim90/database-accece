package hello.jdbc.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *  트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV1 memberRepositoryV1;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection(); // Connection을 받아온다

        try {
            con.setAutoCommit(false); // 트랜잭션 시작. AutoCommit false 명령을 DB에 날려준다

            // 비즈니스 로직 수행한다
            bizLogic(con, fromId, toId, money);

            // 여기까지 정상적으로 로직이 수행되면 commit을 해준다
            con.commit(); // 성공시 커밋

        } catch(Exception e) {
            // 만약, 예외가 발생할 경우 트랜잭션 rollback한다.
            con.rollback(); // 실패시 롤백
            throw new IllegalStateException(e); // 예외 던진다

        } finally {
            if (con != null) {
                release(con); // Connecion을 release(해제) 한다 -> 생성한 connection을 풀어준다
            }

        }
    }

    /**
     * 비즈니스 로직: member의 money update
     **/
    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        // 비즈니스 로직
        //
        Member fromMember = memberRepositoryV1.findById(con, fromId); // 보내는 멤버
        Member toMember = memberRepositoryV1.findById(con,toId);

        memberRepositoryV1.update(con, fromId, fromMember.getMoney() - money); // fromMember의 money 출금 처리
        // 예외 발생 상황 만들기
        validation(toMember);
        memberRepositoryV1.update(con, toId, toMember.getMoney() + money); // fromMember의 money 출금 처리
    }

    private void release(Connection con) {
        try {
            con.setAutoCommit(true); // 오토 커밋 모드로 다시 변경 후 close를 통해 반환한다
            con.close(); // connection을 pool에 반환한다

        } catch (Exception e) {
            log.info("error", e); // 에러 로그 남긴다
        }
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }

}
