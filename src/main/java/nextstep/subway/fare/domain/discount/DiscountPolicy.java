package nextstep.subway.fare.domain.discount;

import nextstep.subway.auth.domain.LoginMember;

public interface DiscountPolicy {

    long calculateDiscountFare(LoginMember loginMember, long fare);
}
