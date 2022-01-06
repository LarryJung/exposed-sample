jetbrains 에서 운영하는 kotlin orm

kotlin orm 3대장 (start) _ 2022.01.05

- exposed (5.8k)
- sqldelight (4.4k) - 안드진영에서 인기
- ktorm (1.2k)

## Exposed
> Just like our mascot, *Exposed can mimic a variety of database engines* and help you build database applications without hard dependencies on any specific database engine, and switch between them *with very little or no changes in your code.*

~~하지만 db engine 은 한번 결정되면 바꿀일이 없는..~~
### document site 는 따로 없고, github wiki 만 존재함

[Home · JetBrains/Exposed Wiki](https://github.com/JetBrains/Exposed/wiki)

## 특징

- lightweight orm
    - 그렇다고 sql mapper 수준은 아님.
    - cache layer도 있다.
- dsl (like QueryDSL)
    - QClass 같은 것을 직접 선언해야 함
- dao (like jpa)
    - lazy loading, eager loading 지원
- single thread 기반 (like jpa)
    - nonblocking framework 와 사용할때 io 부분은 block 됨
    - coroutine 도 가능하긴 하다.
- native query 지원
- batch update, batch insert 지원
    - jpa가 지원못해주는 이유는 mysql + identity + context 때문이므로..
- spring transaction 에서 사용 가능
- transaction 에 isolation 지정 가능
- propagation 은 따로 없음. nestedTransaction enable flag가 존재하여 exception 시 nestedTx 만 rollback 하게 할 수 는 있다.
- multiple datasource 가능

## 장점

- 스프링 이외의 서버 프레임워크와 이용 가능 (ktor, http4k 등..)
- 코틀린으로 만들어져서 null 타입 호환이 잘 됨
- batch insert 등을 편하게 쓸 수 있다.
- jdbc template + jpa 느낌
- 설정이 간편하다.
- 젯브레인에 대한 기대감..
- coroutine transaction 을 지원함

## 단점

- 레퍼런스가 적다 (막혔을 때 막막함)
- 쿼리 코드량이 queryDSL 에 비해 적지 않음
- best practice 를 찾지 못했음
- 버전이 낮음 (0.32)
- r2dbc X

## 정리

- 토이 프로젝트에서 사용해보는 것을 권장
- batch insert 등이 필요한 경우에 부분적으로 사용해보는 것도 좋음 (정산팀에서는 쓰고 있음)