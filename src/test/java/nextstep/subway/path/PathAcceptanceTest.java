package nextstep.subway.path;

import static nextstep.subway.auth.infrastructure.AuthorizationExtractor.*;
import static nextstep.subway.line.acceptance.LineAcceptanceTest.*;
import static nextstep.subway.line.acceptance.LineSectionAcceptanceTest.*;
import static nextstep.subway.member.MemberAcceptanceTest.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenRequest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {
	private LineResponse 신분당선;
	private LineResponse 이호선;
	private LineResponse 삼호선;
	private LineResponse 일호선;
	private StationResponse 주안역;
	private StationResponse 부천역;
	private StationResponse 강남역;
	private StationResponse 양재역;
	private StationResponse 교대역;
	private StationResponse 남부터미널역;
	private String token;

	/**
	 * 교대역    --- *2호선* ---   강남역
	 * |                        |
	 * *3호선*                   *신분당선*
	 * |                        |
	 * 남부터미널역  --- *3호선* ---   양재
	 */
	@BeforeEach
	public void setUp() {
		super.setUp();

		강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
		양재역 = StationAcceptanceTest.지하철역_등록되어_있음("양재역").as(StationResponse.class);
		교대역 = StationAcceptanceTest.지하철역_등록되어_있음("교대역").as(StationResponse.class);
		남부터미널역 = StationAcceptanceTest.지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);
		주안역 = StationAcceptanceTest.지하철역_등록되어_있음("주안역").as(StationResponse.class);
		부천역 = StationAcceptanceTest.지하철역_등록되어_있음("부천역").as(StationResponse.class);

		일호선 = 지하철_노선_등록되어_있음("일호선", "bg-blue-600", 주안역, 부천역, 5);
		신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재역, 10);
		이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역, 강남역, 10);
		삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역, 양재역, 5);

		지하철_노선에_지하철역_등록되어_있음(삼호선, 교대역, 남부터미널역, 3);

		회원_생성을_요청(EMAIL, PASSWORD, AGE);
		TokenResponse response = 로그인_요청(new TokenRequest(EMAIL, PASSWORD)).as(TokenResponse.class);
		token = response.getAccessToken();
	}

	@DisplayName("최단 경로를 조회한다.")
	@Test
	void 최단_경로_조회_성공() {
		// when
		ExtractableResponse<Response> response = 최단_경로를_조회한다(강남역.getId(), 남부터미널역.getId());

		// then
		최단_경로를_반환한다(response);
		최단_거리를_반환한다(response);
		지하철_이용_요금을_반환한다(response);
	}

	@DisplayName("출발역과 도착역이 같은 경우, 조회에 실패한다.")
	@Test
	void 최단_경로_조회_실패_출발_도착_같은_경우() {
		// when
		ExtractableResponse<Response> response = 최단_경로를_조회한다(강남역.getId(), 강남역.getId());

		// then
		최단_경로를_조회_실패(response);
	}

	@DisplayName("출발역과 도착역이 연결되어 있지 않는 경우, 조회에 실패한다.")
	@Test
	void 최단_경로_조회_실패_출발_도착이_연결되어_있지_않은_경우() {
		// when
		ExtractableResponse<Response> response = 최단_경로를_조회한다(주안역.getId(), 강남역.getId());

		// then
		최단_경로를_조회_실패(response);
	}

	@DisplayName("출발역이 존재하지 않는 경우, 조회에 실패한다.")
	@Test
	void 최단_경로_조회_실패_출발이_존재하지_않는_경우() {
		// given
		StationResponse 송내역 = StationAcceptanceTest.지하철역_등록되어_있음("송내역").as(StationResponse.class);

		// when
		ExtractableResponse<Response> response = 최단_경로를_조회한다(송내역.getId(), 강남역.getId());

		// then
		최단_경로를_조회_실패(response);
	}

	@DisplayName("도착역이 존재하지 않는 경우, 조회에 실패한다.")
	@Test
	void 최단_경로_조회_실패_도착이_존재하지_않는_경우() {
		// given
		StationResponse 송내역 = StationAcceptanceTest.지하철역_등록되어_있음("송내역").as(StationResponse.class);

		// when
		ExtractableResponse<Response> response = 최단_경로를_조회한다(강남역.getId(), 송내역.getId());

		// then
		최단_경로를_조회_실패(response);
	}

	private void 최단_경로를_반환한다(ExtractableResponse<Response> response) {
		Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		PathResponse res = response.as(PathResponse.class);
		Assertions.assertThat(res.getStationsResponse()).isNotNull();
	}

	private void 최단_거리를_반환한다(ExtractableResponse<Response> response) {
		Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		PathResponse res = response.as(PathResponse.class);
		Assertions.assertThat(res.getDistance()).isNotNull();
	}

	private void 지하철_이용_요금을_반환한다(ExtractableResponse<Response> response) {
		Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		PathResponse res = response.as(PathResponse.class);
		Assertions.assertThat(res.getFare()).isNotNull();
	}

	private void 최단_경로를_조회_실패(ExtractableResponse<Response> response) {
		Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	private ExtractableResponse<Response> 최단_경로를_조회한다(long source, long target) {
		return RestAssured
			.given().log().all()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.header(HttpHeaders.AUTHORIZATION, makeAccessToken(token))
			.param("source", source)
			.param("target", target)
			.when().get("/paths")
			.then().log().all().extract();
	}

	private LineResponse 지하철_노선_등록되어_있음(
		String name,
		String color,
		StationResponse upStation,
		StationResponse downStation,
		int distance) {
		return 지하철_노선_생성_요청(new LineRequest(name, color, upStation.getId(), downStation.getId(), distance)).as(LineResponse.class);
	}

	private void 지하철_노선에_지하철역_등록되어_있음(
		LineResponse line,
		StationResponse upStation,
		StationResponse downStation,
		int distance) {
		지하철_노선에_지하철역_등록_요청(line, upStation, downStation, distance);
	}

	private static String makeAccessToken(String token) {
		return BEARER_TYPE + " " + token;
	}
}
