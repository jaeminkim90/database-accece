package hello.jdbc.repository.ex;

import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * 예외 누수 문제 해결
 * 체크 예외를 런타임 예외로 변경
 * MemberRepository 인터페이스 사용
 * throws SQLException 제거
 */
public class MyDbException extends RuntimeException {

    public MyDbException() {
    }

    public MyDbException(String message) {
        super(message);
    }

    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) { // 원인 예외를 파라미터로 받는 생성자
        super(cause);
    }
}
