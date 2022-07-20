package hello.jdbc.service;

import java.sql.SQLException;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepositoryV1;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV1.findById(fromId); // 보내는 멤버
        Member toMember = memberRepositoryV1.findById(toId);

        memberRepositoryV1.update(fromId, fromMember.getMoney() - money); // fromMember의 money 출금 처리
        
        // 예외 발생 상황 만들기
        validation(toMember);


        memberRepositoryV1.update(toId, toMember.getMoney() + money); // fromMember의 money 출금 처리

    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }

}
