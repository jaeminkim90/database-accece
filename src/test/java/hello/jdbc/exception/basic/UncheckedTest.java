package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UncheckedTest {

    /**
     * RuntimeException을 상속받은 예외는 언체크 예외가 된다.
     */
    static class MyUncheckedException extends RuntimeException {
        public  MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * Unchecked 예외는 예외를 잡거나 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아서 처리하면 된다
         * Unchecked 예외는 별도로 예외를 처리하지 않으면 자동으로 던진다.
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                // 예외 로직 처리
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }

        }

    }

    static class Repository {
        public void call() { // Checked 예외는 반드시 Throw를 선언해야 하지만, Unchecked를 상속받은 경우 Throw 선언이 팔요없다
            throw new MyUncheckedException("ex");

        }
    }
}
