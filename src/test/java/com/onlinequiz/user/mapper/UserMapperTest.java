package com.onlinequiz.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.onlinequiz.user.dto.UserProfileResponse;
import com.onlinequiz.user.dto.UserSummaryResponse;
import com.onlinequiz.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toProfileResponseMapsFields() {
        User user = User.createAdmin("Ada Lovelace", "ada@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", 7L);

        UserProfileResponse response = mapper.toProfileResponse(user);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.fullName()).isEqualTo("Ada Lovelace");
        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void toSummaryResponseMapsFields() {
        User user = User.createRegularUser("Ada Lovelace", "ada@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", 8L);

        UserSummaryResponse response = mapper.toSummaryResponse(user);

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.fullName()).isEqualTo("Ada Lovelace");
        assertThat(response.email()).isEqualTo("ada@example.com");
    }
}
