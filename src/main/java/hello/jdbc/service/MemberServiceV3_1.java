package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *  트랜잭션 - 트랜잭션 매니저
 *  Service에서 Spring이 제공하는 TransactionManager를 연결하는 코드 실습
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

    // 기존 dataSource 대신 PlatfromTransactionManager를 사용한다
    //private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager; // jdbc를 사용하는 DataSourceTransactionManager 객체 의존성 주입받음
    private final MemberRepositoryV3 memberRepository; // DB 접근을 위해 repository 주입 받음

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 1. 트랜잭션 실행을 위해서는 반드시 커넥션이 필요하다. 트랜잭션 매니저는 데이터 소스를 사용해 커넥션을 생성한다.
        // 2. 트랜잭션 시작(트랜잭션 매니저를 통해 트랜잭션을 얻는다)
        // 3. 생성된 커넥션의 setAutoCommit을 false로 변경한다.
        // 4. 커넥션은 <트랜잭션 동기화 매니저>에 보관한다. 멀티 쓰레드 환경에서 안전하게 커넥션을 보관할 수 있다.
        TransactionStatus status = transactionManager.getTransaction(
            new DefaultTransactionDefinition());// 트랜잭션 속성을 넣는 파라미터에는 기본 속성 객체를 넣는다.

        try {
            // 비즈니스 로직 수행한다
            // 비즈니스 로직 수행중 커넥션이 필요할 경우 트랜잭션 동기화 매니저를 통해 커넥션을 가져온다.
            // 파라미터로 커넥션을 주입할 필요가 없다
            bizLogic(fromId, toId, money);
            transactionManager.commit(status);

        } catch (Exception e) {
            // 만약, 예외가 발생할 경우 트랜잭션 rollback한다.
            transactionManager.rollback(status);
            throw new IllegalStateException(e); // 예외 던진다
        }
        // <트랜잭션 매니저>를 사용하면 release는 더 이상 필요하지 않다.
        // commit 또는 rollback 시점에 transactionManager가 자동으로 리소스를 정리한다.
        // 커넥션의 setAutoCommit을 true로 변경하고, close 처리한다. 커넥션 풀 사용하는 경우 close하면 커넥션 풀에 반환된다.

    }

    /**
     * 비즈니스 로직: member의 money update
     **/
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        // 비즈니스 로직
        Member fromMember = memberRepository.findById(fromId); // 보내는 멤버
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money); // fromMember의 money 출금 처리
        // 예외 발생 상황 만들기
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money); // fromMember의 money 출금 처리
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
