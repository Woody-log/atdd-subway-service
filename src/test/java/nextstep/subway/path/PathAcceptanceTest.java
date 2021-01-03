package nextstep.subway.path;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.acceptance.LineAcceptanceTest;
import nextstep.subway.line.acceptance.LineSectionAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {
    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;

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

        신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재역, 10);
        이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역, 강남역, 10);
        삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역, 양재역, 5);

        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 3);
    }

    @DisplayName("지하철 구간의 최단 경로를 찾는다.")
    @Test
    void findShortestPath() {
        // when
        ExtractableResponse<Response> response = 지하철_경로_조회_요청(강남역.getId(), 남부터미널역.getId());

        // then
        지하철_최단경로_응답됨(response, 강남역.getId(), 양재역.getId(), 남부터미널역.getId());
    }

    private LineResponse 지하철_노선_등록되어_있음(final String name, final String color, final StationResponse upStation,
                                        final StationResponse downStation, final int distance) {
        LineRequest lineRequest = new LineRequest(name, color, upStation.getId(), downStation.getId(), distance);
        return LineAcceptanceTest.지하철_노선_등록되어_있음(lineRequest).as(LineResponse.class);
    }

    private ExtractableResponse<Response> 지하철_경로_조회_요청(final Long source, final Long target) {
        String path = String.format("/paths?source=%d&target=%d", source, target);
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .log().all()
                .extract();
        return response;
    }

    private void 지하철_최단경로_응답됨(final ExtractableResponse<Response> response, final Long... expectedStationIds) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> actualStationIds = response.jsonPath()
                .getList("stations", PathResponse.StationResponse.class).stream()
                .map(PathResponse.StationResponse::getId)
                .collect(Collectors.toList());
        assertThat(actualStationIds).containsExactly(expectedStationIds);
    }
}
