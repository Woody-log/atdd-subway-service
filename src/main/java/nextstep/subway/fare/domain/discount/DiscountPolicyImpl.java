package nextstep.subway.fare.domain.discount;

import nextstep.subway.auth.domain.LoginMember;
import nextstep.subway.member.domain.AgeGrade;
import org.springframework.stereotype.Component;

@Component
public class DiscountPolicyImpl implements DiscountPolicy {

    private static final double YOUTH_DISCOUNT_RATE = 0.7;
    private static final double CHILDREN_DISCOUNT_RATE = 0.5;
    private static final long DEDUCTIBLE_AMOUNT = 350;

    @Override
    public long calculateDiscountFare(LoginMember loginMember, long fare) {
        if (AgeGrade.isYouth(loginMember.getAge())) {
            return (long) ((fare - DEDUCTIBLE_AMOUNT) * YOUTH_DISCOUNT_RATE);
        }
        if (AgeGrade.isChildren(loginMember.getAge())) {
            return (long) ((fare - DEDUCTIBLE_AMOUNT) * CHILDREN_DISCOUNT_RATE);
        }

        return fare;
    }
}
