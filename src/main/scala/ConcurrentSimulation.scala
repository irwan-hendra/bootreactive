import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class ConcurrentSimulation extends Simulation {


  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("text/html,application/json,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn1 = scenario("Tomcat: 10 threads + Spring WebMVC + 5s delay webcall")
    .exec(
      http("request_x")
        .get("/studentsresttemplate")
        .requestTimeout(FiniteDuration.apply(30000, TimeUnit.MILLISECONDS))
    )

  val scn2 = scenario("Tomcat: 10 threads + Blocking Spring WebFlux + 5s delay webcall")
    .exec(
      http("request_x")
        .get("/studentsreactiveblocking")
        .requestTimeout(FiniteDuration.apply(30000, TimeUnit.MILLISECONDS))
    )

  val scn3 = scenario("Tomcat: 10 threads + Non Blocking Spring WebFlux + 5s delay webcall")
    .exec(
      http("request_x")
        .get("/studentsreactive")
        .requestTimeout(FiniteDuration.apply(30000, TimeUnit.MILLISECONDS))
    )

  val scn4 = scenario("Tomcat: 10 threads + Spring WebMVC + cpu-intenstive pi calculation")
    .exec(
      http("request_x")
        .get("/calcpi")
        .queryParam("iterations", 1000000)
        .requestTimeout(FiniteDuration.apply(30000, TimeUnit.MILLISECONDS))
    )

  val scn5 = scenario("Tomcat: 10 threads + Spring WebFlux + cpu-intensive pi calculation")
    .exec(
      http("request_x")
        .get("/calcpireactive")
        .queryParam("iterations", 1000000)
        .requestTimeout(FiniteDuration.apply(30000, TimeUnit.MILLISECONDS))
    )

  val scn6 = scenario("Tomcat: 10 threads + Spring WebFlux Parallel of 4 + cpu-intensive pi calculation")
    .exec(
      http("request_x")
        .get("/calcpireactiveparl2")
        .queryParam("iterations", 1000000)
        .requestTimeout(FiniteDuration.apply(30000, TimeUnit.MILLISECONDS))
    )

  val scn7 = scenario("Tomcat: 10 threads + Spring WebFlux Parallel of 2 + cpu-intensive pi calculation")
    .exec(
      http("request_x")
        .get("/calcpireactiveparl4")
        .queryParam("iterations", 1000000)
        .requestTimeout(FiniteDuration.apply(30000, TimeUnit.MILLISECONDS))
    )

  setUp(
    //scn1.inject(atOnceUsers(30)),
    // scn2.inject(atOnceUsers(30)),
    // scn3.inject(atOnceUsers(30)),
    // scn4.inject(atOnceUsers(30)),
    // scn5.inject(atOnceUsers(30))
    //scn6.inject(atOnceUsers(30))
    scn7.inject(atOnceUsers(30))
  ).protocols(httpProtocol)
}
