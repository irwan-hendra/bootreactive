package com.example.demoreactive;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StudentController {

  private final RestTemplate restTemplate;
  private final Scheduler schedulerParlOfTwo = Schedulers.newParallel("np", 2); // 2 CPUs

  private final WebClient client = WebClient.create("http://localhost:9090");
  private final Scheduler schedulerParlOfFour = Schedulers.newParallel("np", 4); // 4 CPUs
  private AtomicInteger txnIdGen = new AtomicInteger(0); // unique id just for logging purpose

  @GetMapping(path = "studentsresttemplate", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result getStudentsRestTemplate() throws Exception {

    Result result = createResultObject();
    result.setStudents(Arrays.asList(restTemplate.getForObject("http://localhost:9090/customerinfo", Student[].class)));
    result.setEndDtm(LocalDateTime.now());
    log.info("txnId: " + result.getTxnId() +
        ", StartDtm: " + result.getStartDtm() +
        ", EndDtm: " + result.getEndDtm() +
        ", Duration: " + ChronoUnit.MILLIS.between(result.getStartDtm(), result.getEndDtm()));
    return result;
  }

  @GetMapping(path = "studentsreactiveblocking", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result getStudentsWebClientBlocking() throws Exception {

    Result result = createResultObject();
    Result block = client.get().uri("/customerinfo")
        .retrieve()
        .bodyToMono(Student[].class)
        .map(p -> {
          result.setStudents(Arrays.asList(p));
          result.setEndDtm(LocalDateTime.now());
          return result;
        }).block();
    result.setStudents(block.getStudents());
    result.setEndDtm(LocalDateTime.now());

    log.info("txnId: " + result.getTxnId() +
        ", StartDtm: " + result.getStartDtm() +
        ", EndDtm: " + result.getEndDtm() +
        ", Duration: " + ChronoUnit.MILLIS.between(result.getStartDtm(), result.getEndDtm()));

    return result;
  }

  @GetMapping(path = "studentsreactive", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Result> getStudentsReactive() throws Exception {
    Result result = createResultObject();
    return client.get().uri("/customerinfo")
        .retrieve()
        .bodyToMono(Student[].class)
        .map(p -> {
          result.setStudents(Arrays.asList(p));
          result.setEndDtm(LocalDateTime.now());
          return result;
        })
        .doOnNext(p -> log.info("txnId: " + result.getTxnId() +
            ", StartDtm: " + result.getStartDtm() +
            ", EndDtm: " + result.getEndDtm() +
            ", Duration: " + ChronoUnit.MILLIS.between(result.getStartDtm(), result.getEndDtm())));
  }

  @GetMapping(path = "calcpi", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result getCalcPi(@RequestParam Long iterations) throws Exception {
    Result result = createResultObject();
    result.setPiResult(calcPi(iterations));
    result.setEndDtm(LocalDateTime.now());

    log.info("Pi txnId: " + result.getTxnId() +
        ", StartDtm: " + result.getStartDtm() +
        ", EndDtm: " + result.getEndDtm() +
        ", Duration: " + ChronoUnit.MILLIS.between(result.getStartDtm(), result.getEndDtm()));

    return result;
  }

  @GetMapping(path = "calcpireactive", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Result> calcPiReactive(@RequestParam Long iterations) throws Exception {
    Result result = createResultObject();
    return Mono.just(iterations).map(i -> {
          result.setPiResult(calcPi(i));
          return result;
        })
        .doOnNext(p -> {
              result.setEndDtm(LocalDateTime.now());
              log.info("Pi txnId: " + result.getTxnId() +
                  ", StartDtm: " + result.getStartDtm() +
                  ", EndDtm: " + result.getEndDtm() +
                  ", Duration: " + ChronoUnit.MILLIS.between(result.getStartDtm(), result.getEndDtm()));
            }
        );
  }

  @GetMapping(path = "calcpireactiveparl2", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Result> calcPiReactiveParlOfTwo(@RequestParam Long iterations) throws Exception {
    Result result = createResultObject();
    return Mono.just(iterations).map(i -> {
          result.setPiResult(calcPi(i));
          return result;
        })
        .subscribeOn(schedulerParlOfTwo)
        .doOnNext(p -> {
              result.setEndDtm(LocalDateTime.now());
              log.info("Pi txnId: " + result.getTxnId() +
                  ", StartDtm: " + result.getStartDtm() +
                  ", EndDtm: " + result.getEndDtm() +
                  ", Duration: " + ChronoUnit.MILLIS.between(result.getStartDtm(), result.getEndDtm()));
            }
        );
  }

  @GetMapping(path = "calcpireactiveparl4", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Result> calcPiReactiveParlOfFour(@RequestParam Long iterations) throws Exception {
    Result result = createResultObject();
    return Mono.just(iterations).map(i -> {
          result.setPiResult(calcPi(i));
          return result;
        })
        .subscribeOn(schedulerParlOfFour)
        .doOnNext(p -> {
              result.setEndDtm(LocalDateTime.now());
              log.info("Pi txnId: " + result.getTxnId() +
                  ", StartDtm: " + result.getStartDtm() +
                  ", EndDtm: " + result.getEndDtm() +
                  ", Duration: " + ChronoUnit.MILLIS.between(result.getStartDtm(), result.getEndDtm()));
            }
        );
  }

  private Double calcPi(final Long iterations) {
    double x;
    double y;
    long successCount = 0;
    for (long i = 0; i <= iterations; i++) {
      x = Math.random();
      y = Math.random();
      if ((Math.pow(x, 2) + Math.pow(y, 2)) <= 1) {
        successCount++;
      }
    }
    return Double.valueOf((double) 4 * successCount) / iterations;
  }

//  @GetMapping(path = "callall", produces = MediaType.APPLICATION_JSON_VALUE)
//  public Result getAll(@RequestParam Long itr) throws Exception {
//    Result result = studentService.getStudentsWebClient(createResultObject());
//    return studentService.calculatePi(itr, result);
//  }
//
//  @GetMapping(path = "getallreactives", produces = MediaType.APPLICATION_JSON_VALUE)
//  public Mono<Result> getCorrectReactives(@RequestParam Long itr) throws Exception {
//    return studentService.getPersonsReactive(createResultObject())
//        .flatMap(p -> studentService.calculatePiReactive(itr, p)
//            .subscribeOn(Schedulers.newParallel("test-", 5)));
//  }

  private Result createResultObject() {
    return Result.builder()
        .txnId("txn-" + txnIdGen.incrementAndGet())
        .startDtm(LocalDateTime.now())
        .build();
  }
}
