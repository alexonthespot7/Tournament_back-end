package com.myproject.tournamentapp.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.myproject.tournamentapp.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	private static final String FRONT_END_URL = "http://localhost:3000";

	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${spring.mail.username}")
	private String springMailUsername;
	
	// Email sending method for verification
	public void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = springMailUsername;
		String senderName = "No reply";
		String subject = "Please verify your registration";
		String content = "Dear [[name]],<br>"
				+ "Please click the link below to verify your registration on the tournament app:<br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you,<br>" + "AXOS inc.";
		String endpoint = "/verify?code=";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());
		String mainURL = FRONT_END_URL + endpoint + user.getVerificationCode();

		content = content.replace("[[URL]]", mainURL);

		helper.setText(content, true);

		mailSender.send(message);
	}

	// Email sending method for password reset
	public void sendPasswordEmail(User user, String password) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = springMailUsername;
		String senderName = "No reply";
		String subject = "Reset password";
		String content = "Dear [[name]],<br>" + "Here is your new TEMPORARY password for tournament app:<br><br>"
				+ "<h3>[[PASSWORD]]</h3>" + "Please change this password once you logged in<br><br>Thank you,<br>"
				+ "AXOS inc.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		content = content.replace("[[name]]", user.getUsername());

		content = content.replace("[[PASSWORD]]", password);

		helper.setText(content, true);

		mailSender.send(message);

	}
}
