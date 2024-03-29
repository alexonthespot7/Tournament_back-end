package com.myproject.tournamentapp.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Long id;

	@NotBlank(message = "Username is mandatory")
    @Size(max = 15, message = "Username must be less than or equal to 15 characters")
    @Pattern(regexp = "\\S+", message = "Username cannot contain whitespace")
	@Column(name = "username", nullable = false, unique = true)
	private String username;

	@NotBlank(message = "Password is mandatory")
	@Column(name = "password", nullable = false)
	private String passwordHash;

    @NotBlank(message = "Role is mandatory")
	@Column(name = "role", nullable = false)
	private String role;

	@Column(name = "isOut")
	private boolean isOut;

	@Column(name = "isCompetitor", nullable = false)
	private boolean isCompetitor;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is mandatory")
	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "verification_code", length = 64)
	private String verificationCode;

	private boolean accountVerified;
	
	@ManyToOne
	@JoinColumn(name = "stageid")
	private Stage stage;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user1")
	private List<Round> rounds1;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user2")
	private List<Round> rounds2;

	public User() {}
	
	//constructor for sign-up
	public User(String username, String passwordHash, String role, boolean isOut,
			boolean isCompetitor, Stage stage, String email, String verificationCode) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.role = role;
		this.isOut = isOut;
		this.isCompetitor = isCompetitor;
		this.stage = stage;
		this.email = email;
		this.accountVerified = false;
		this.verificationCode = verificationCode;
	}

	public User(String username, String passwordHash, String role, boolean isOut,
			boolean isCompetitor, Stage stage, String email, boolean accountVerified, String verificationCode) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.role = role;
		this.isOut = isOut;
		this.isCompetitor = isCompetitor;
		this.stage = stage;
		this.email = email;
		this.accountVerified = accountVerified;
		this.verificationCode = verificationCode;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean getIsOut() {
		return isOut;
	}

	public void setIsOut(boolean isOut) {
		this.isOut = isOut;
	}

	public boolean getIsCompetitor() {
		return isCompetitor;
	}

	public void setIsCompetitor(boolean isCompetitor) {
		this.isCompetitor = isCompetitor;
	}

	public List<Round> getRounds1() {
		return rounds1;
	}

	public void setRounds1(List<Round> rounds1) {
		this.rounds1 = rounds1;
	}

	public List<Round> getRounds2() {
		return rounds2;
	}

	public void setRounds2(List<Round> rounds2) {
		this.rounds2 = rounds2;
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public boolean isAccountVerified() {
		return accountVerified;
	}

	public void setAccountVerified(boolean accountVerified) {
		this.accountVerified = accountVerified;
	}

}
