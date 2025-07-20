# WIL - 1주차 (TDD & 테스트 가능한 구조)

## 🧠 이번 주에 새로 배운 것 
- 테스트의 단계를 도메인 -> 통합테스트 -> E2E 순으로 작성한다면 순서데로 커버리지가 높아지면서 서비스로직의 안정성이 높아지게 되는것을 알게 되었다 
- 팀 리뷰를 하면서 Null을 핸들링할 수 있는 Optional을 사용한다면 코드를 간략하게 핸들링 할 수 있는 방법을 알게 되었다
- TLD(Test Last Development), TFD(Test First Development)가 무엇인지 각 장점에 대해 알게 되었다
- 병렬적으로 빠르게 협업 개발을 하기위해 API에 고정된 응답을 먼저 개발하는 방법론을 알게 되었다  
- Dummy, Mock, Spy, Stub, Fake의 구분과 어떨때 사용하는지 알게 되었다

<table>
<tr>
    <th>역할</th>
    <th>목적</th>
    <th>사용방식</th>
    <th>장점</th>
    <th>단점</th>
</tr>
<tr>
    <td>Dummy</td>
    <td>자리만 채우고 아무역할을 하지 않음, Null 방지</td>
    <td>생성자에서 전달</td>
    <td>테스트 대상과 무관한 의존성 제거</td>
    <td>의미없는 코드가 있을 수 있음</td>
</tr>
<tr>
    <td>Mock</td>
    <td>메소드 호출여부/횟수 검증</td>
    <td>Mockito등으로 모의 객체 생성</td>
    <td>행위기반 테스트 가능</td>
    <td>구현이 바뀌면 테스트도 깨질 수 있음</td>
</tr>
<tr>
    <td>Spy</td>
    <td>진짜 객체를 감싸서 추적하며 잘 사용되고있는지 확인</td>
    <td>일부는 실제 메서드 호출, 일부는 오버라이드로 감시</td>
    <td>실제 객체의 동작과 검증을 함</td>
    <td>구현이 복잡, 의도치 않은 결과 발생</td>
</tr>
<tr>
    <td>Stub</td>
    <td>고정된 응답하여 테스트 조건 설정</td>
    <td>메서드를 오버라이드 해서 특정 입력에 대해 예상된 값 반환</td>
    <td>테스트 조건을 쉽게 제어 가능</td>
    <td>복잡한 로직에대한 커버리지가 약함</td>
</tr>
<tr>
    <td>Fake</td>
    <td>실제처럼 동작하는 가짜 구현체</td>
    <td>직접 클래스를 구현</td>
    <td>테스트중 진짜처럼 작동해서 현실성 있음</td>
    <td>직접 클래스를 모두 구현해야하기에 개발해야할 양이 상대적으로 많음</td>
</tr>
</table>

1. Dummy: 사용되지 않지만, Null 아닌 객체 전달
```
class DummyUser implements User {
  // 구현 내용 없음
}

service.createAccount("name", new DummyUser()); // user는 내부에서 사용되지 않음
```

2.Stub: 고정된 값을 반환하여 테스트 조건을 설정
```
class StubUserRepository implements UserRepository {
    public Optional<User> findById(Long id) {
        return Optional.of(new User("stubUser"));
    }
}
```
3.Fake: 실제 동작을 대체하지만, 간단한 로직으로 작동하는 구현체
```
class InMemoryUserRepository implements UserRepository {
    private Map<Long, User> store = new HashMap<>();
    public void save(User user) { store.put(user.getId(), user); }
    public Optional<User> findById(Long id) { return Optional.ofNullable(store.get(id)); }
}
```
4.Spy: 실제 객체처럼 동작하면서 호출 여부, 인자 등을 검증
```
UserService realService = new UserService();
UserService spyService = Mockito.spy(realService);

spyService.doSomething();
verify(spyService).doSomething();
```
5.Mock: 상호작용(메서드 호출 여부, 횟수 등) 자체를 검증
```
UserRepository mockRepo = mock(UserRepository.class);
when(mockRepo.findById(1L)).thenReturn(Optional.of(new User("mock")));

verify(mockRepo, times(1)).findById(1L);
```

## 💭 이런 고민이 있었어요
- JPA기반 프로젝트를 처음 접하니 사용하는데 약간의 어려움이 있었다.
- 도메인, 통합, E2E테스트 코드라는것을 여태까지 말로만 들었지 직접해보니 어떻게 작성해야할지 막막했지만<br>
샘플코드를 참고하여 작성해보니 테스트 코드에 대한 어느정도 감을 잡을 수 있었다.
- 내가 작성한 코드가 과연 다른사람이 볼때 보기 좋은 코드일까? 라는 의문점이 있었다.

## 💡 앞으로 실무에 써먹을 수 있을 것 같은 포인트
- X-USER-ID 헤더값을 체크할때 AOP내 커스텀 애노테이션을 정의하여 사용했는데 추후 자주 사용하는 validation, 공통함수등을 활용할때 좋을거 같다
- 처음부터 잘 설계해서 한번에 개발을 진행하기 보다 작은 단위부터 검증 로직, 예외사항 정리, 개발기에 올리기전 테스트를 하여 API의 검증을 할 수 있을것 같다
- 호출량이 많은 주요 API들에 대해 선 반영한다면 좀 더 안정적인 개발을 할 수 있을것 같다 
- 여태까지 B2C서비스 개발/운영 하면서 API 변경/개발을 하면서 예상치 못한 연관 API의 영향도로 인해 에러가 발생한적이 있었던적이 있었는데 테스트코드를 통해 운영 배포전 검증할 수 있게 될것 같다 
- 다양한 리팩토링 기법에 대한 내용을 알게 되었는데 적절한 리팩토링 기법을 실무에 적용하여 유지보수하기 좋은 구조로 만들어봐야할거 같다 
  >https://refactoring.guru/ko/design-patterns

## 🤔 아쉬웠던 점 & 다음 주에 해보고 싶은 것
- 요구사항에 정리를 하여 코드를 한번에 작성하기보다 먼저 테스트 케이스를 정리하고, 그에 맞는 구현을 한다면 좀 더 빠르게 개발을 할 수 있을것 같다
- 다음 주 진행할 API를 설계할 때부터는 테스트 구조를 고려해 도메인, 서비스레이어의 설계와 리팩토링할 수 있도록 구조설계를 해보고싶다. 