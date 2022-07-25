package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckedTest {

    /**
     * Exception을 상속받은 예외는 체크 예외가 된다.
     */
    // Exception을 상속 받으면 Check 예외가 된다.
    static class MyCheckedException extends Exception {

        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * Checked 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘 중 하나를 필수로 선택해야 한다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                // call() 내부에서 예외를 던지기 때문에 call()을 호출하는 메서드에서도 예외를 던지거나 잡아야한다.
                repository.call();
            } catch (MyCheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message = {} ", e.getMessage(), e);
            }

        }

    }

    static class Repository {
        public void call() throws MyCheckedException {
            // 예외를 잡아 처리하지 않으면, 반드시 던져야 한다(throw). 컴파일러가 체크해주기 때문에 체크예외라고 한다.
            throw new MyCheckedException("ex");
        }

    }
}
