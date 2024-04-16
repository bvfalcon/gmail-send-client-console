package name.bychkov.gmail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class ConsoleSender {

	public static void main(String[] args) throws IOException, AddressException, MessagingException {
		// read console parameters
		String applicationName = System.getProperty("application.name");
		String delegate = System.getProperty("delegate");
		File serviceAccountKeyFile = new File(System.getProperty("service.account.key"));
		String emailSubject = System.getProperty("email.subject");
		String emailBody = System.getProperty("email.body");
		String emailRecipient = System.getProperty("email.recipient");

		// construct JavaMail message
		MimeMessage javaMailMessage = getJavaMailMessage(emailRecipient, emailSubject, emailBody);

		// connect to GMail
		Gmail client = getGMailClient(applicationName, serviceAccountKeyFile, delegate);
		
		// send message
		Message gmailMessage = getGMailMessage(javaMailMessage);
		client.users().messages().send(delegate, gmailMessage).execute();
	}

	private static Message getGMailMessage(MimeMessage javaMailMessage) throws IOException, MessagingException {
		Message message = new Message();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		javaMailMessage.writeTo(buffer);
		message.encodeRaw(buffer.toByteArray());
		return message;
	}

	private static Gmail getGMailClient(String applicationName, File serviceAccountKeyFile, String delegate)
			throws IOException {
		HttpTransport transport = Utils.getDefaultTransport();
		JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
		GoogleCredential credentials = GoogleCredential
				.fromStream(new FileInputStream(serviceAccountKeyFile), transport, jsonFactory)
				.createScoped(Arrays.asList(GmailScopes.GMAIL_SEND)).createDelegated(delegate);
		credentials.refreshToken();
		Gmail client = new Gmail.Builder(transport, jsonFactory, credentials)
				.setApplicationName(applicationName).build();
		return client;
	}

	private static MimeMessage getJavaMailMessage(String recipient, String subject, String body)
			throws AddressException, MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage email = new MimeMessage(session);
		email.addRecipient(RecipientType.TO, new InternetAddress(recipient));
		email.setSubject(subject);
		email.setText(body);
		return email;
	}
}
