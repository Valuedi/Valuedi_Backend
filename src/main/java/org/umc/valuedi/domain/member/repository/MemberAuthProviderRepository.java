package org.umc.valuedi.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.member.entity.MemberAuthProvider;
import org.umc.valuedi.domain.member.enums.Provider;

import java.util.Optional;

public interface MemberAuthProviderRepository extends JpaRepository<MemberAuthProvider, Long> {
    Optional<MemberAuthProvider> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
