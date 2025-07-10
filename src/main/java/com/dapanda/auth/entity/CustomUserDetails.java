package com.dapanda.auth.entity;

import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

	@Getter
	private final Long id;
	private final String email;
	private final String password;
	private final String provider;
	private final MemberRole role;

	public CustomUserDetails(Long id, String email, String password, String provider,
			MemberRole role) {

		this.id = id;
		this.email = email;
		this.password = password;
		this.provider = provider;
		this.role = role;
	}

	public static CustomUserDetails from(Member member) {

		return new CustomUserDetails(
				member.getId(),
				member.getEmail(),
				member.getPassword(),
				member.getProvider().name(),
				member.getRole()
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return List.of(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getUsername() {

		return email;
	}

	@Override
	public String getPassword() {

		return password;
	}

	@Override
	public boolean isAccountNonExpired() {

		return true;
	}

	@Override
	public boolean isAccountNonLocked() {

		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {

		return true;
	}

	@Override
	public boolean isEnabled() {

		return true;
	}
}
