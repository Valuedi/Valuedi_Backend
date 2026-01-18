package org.umc.valuedi.domain.terms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.entity.BaseEntity;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_terms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "terms_id"})
)
public class MemberTerms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    @Column(name = "agreed_version", nullable = false, length = 20)
    private String agreedVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getMemberTermsList().remove(this);
        }
        this.member = member;
        if (member != null && !member.getMemberTermsList().contains(this)) {
            member.getMemberTermsList().add(this);
        }
    }

    public void setTerms(Terms terms) {
        if (this.terms != null) {
            this.terms.getMemberTermsList().remove(this);
        }
        this.terms = terms;
        if (terms != null && !terms.getMemberTermsList().contains(this)) {
            terms.getMemberTermsList().add(this);
        }
    }

    public static MemberTerms create(Member member, Terms terms, boolean isAgreed, String agreedVersion) {
        MemberTerms mt = MemberTerms.builder()
                .isAgreed(isAgreed)
                .agreedVersion(agreedVersion)
                .build();

        mt.setMember(member);
        mt.setTerms(terms);
        return mt;
    }
}
