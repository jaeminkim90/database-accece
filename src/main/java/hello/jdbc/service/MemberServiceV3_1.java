package hello.jdbc.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
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
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepositoryV2;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(
            new DefaultTransactionDefinition());// 트랜잭션 속성을 넣는 파라미터에는 기본 속성 객체를 넣는다.

        try {
            // 비즈니스 로직 수행한다
            bizLogic(fromId, toId, money);
            transactionManager.commit(status);

        } catch (Exception e) {
            // 만약, 예외가 발생할 경우 트랜잭션 rollback한다.
            transactionManager.rollback(status);
            throw new IllegalStateException(e); // 예외 던진다
        }
        // release는 더 이상 필요하지 않다. commit 또는 rollback 시점에 transactionManager가 자동으로 close 처리한다.
    }

    /**
     * 비즈니스 로직: member의 money update
     **/
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        // 비즈니스 로직
        Member fromMember = memberRepositoryV2.findById(fromId); // 보내는 멤버
        Member toMember = memberRepositoryV2.findById(toId);

        memberRepositoryV2.update(fromId, fromMember.getMoney() - money); // fromMember의 money 출금 처리
        // 예외 발생 상황 만들기
        validation(toMember);
        memberRepositoryV2.update(toId, toMember.getMoney() + money); // fromMember의 money 출금 처리
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
