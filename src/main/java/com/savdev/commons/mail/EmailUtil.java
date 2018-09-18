package com.savdev.commons.mail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EmailUtil {
  private static InternetAddress[] EMPTY = {};


  public static Session sslAuthentication(
    final String smtpHostServer,
    final String smtpPort,
    final String sslPort,  //Default port for SSL: 465
    final String smtpUser,
    final String smtpUserPassword) {
    Properties props = new Properties();
    props.put("mail.smtp.host", smtpHostServer); //SMTP Host
    props.put("mail.smtp.socketFactory.port", sslPort); //SSL Port
    props.put("mail.smtp.socketFactory.class",
      "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
    props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
    props.put("mail.smtp.port", smtpPort); //SMTP Port

    return Session.getInstance(props, new Authenticator() {
      //override the getPasswordAuthentication method
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(smtpUser, smtpUserPassword);
      }
    });
  }

  /**
   * Outgoing Mail (SMTP) Server with required TLS authentication
   *
   * @param smtpHostServer
   * @param smtpPort
   * @param smtpUser
   * @param smtpUserPassword
   * @return
   */
  public static Session tlsAuthentication(
    final String smtpHostServer,
    final String smtpPort,
    final String smtpUser,
    final String smtpUserPassword) {

    Properties props = System.getProperties();
    props.put("mail.smtp.host", smtpHostServer); //SMTP Host
    props.put("mail.smtp.port", smtpPort); //TLS Port
    props.put("mail.smtp.auth", "true"); //enable authentication
    props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

    return Session.getInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(smtpUser, smtpUserPassword);
      }
    });
  }

  public static Session withoutAuthentication(
    final String smtpHostServer) {
    Properties props = System.getProperties();
    props.put("mail.smtp.host", smtpHostServer);
    return Session.getInstance(props, null);
  }

  /**
   * Send email, simplified method for a single sender/receiver
   *
   * @param session
   * @param toEmail
   * @param fromEmail
   * @param subject
   * @param body
   */
  public static void sendEmail(
    final Session session,
    final String toEmail,
    final String fromEmail,
    final String subject,
    final String body) {
    sendEmail(
      session,
      MimeMessageHeaders.defaultMimeHeaders().toMap(),
      StandardCharsets.UTF_8,
      Collections.singletonList(
        InternetAddressBuilder.builder().email(toEmail).build()),
      Collections.emptyList(),
      InternetAddressBuilder.builder().email(fromEmail).build(),
      Collections.emptyList(),
      subject,
      body);
  }

  /**
   * Utility method to send simple HTML email
   *
   * @param session
   * @param extraHeaders
   * @param encoding
   * @param toList
   * @param ccList
   * @param from
   * @param replyList
   * @param subject
   * @param body
   */
  public static void sendEmail(
    final Session session,
    final Map<String, String> extraHeaders,
    final Charset encoding,
    final List<InternetAddress> toList,
    final List<InternetAddress> ccList,
    final InternetAddress from,
    final List<InternetAddress> replyList,
    final String subject,
    final String body) {

    try {
      MimeMessage msg = initMimeMessage(
        session,
        extraHeaders,
        encoding,
        toList,
        ccList,
        from,
        replyList,
        subject);

      msg.setText(body, encoding.toString());

      Transport.send(msg);
    } catch (MessagingException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Utility method to send HTML email with attachments
   *
   * @param session
   * @param extraHeaders
   * @param encoding
   * @param toList
   * @param ccList
   * @param from
   * @param replyList
   * @param subject
   * @param body
   */
  public static void sendEmailWithAttachments(
    final Session session,
    final Map<String, String> extraHeaders,
    final Charset encoding,
    final List<InternetAddress> toList,
    final List<InternetAddress> ccList,
    final InternetAddress from,
    final List<InternetAddress> replyList,
    final String subject,
    final String body,
    final List<String> attachmentFiles) {
    try {
      MimeMessage msg = initMimeMessage(
        session,
        extraHeaders,
        encoding,
        toList,
        ccList,
        from,
        replyList,
        subject); // Create a multipart message for attachment

      MimeMultipart multipart = mimeMultipart4Attachments(body, attachmentFiles);
      msg.setContent(multipart);

      Transport.send(msg);
    } catch (MessagingException e) {
      throw new IllegalStateException(e);
    }
  }


  /**
   * Sends email with attachments, allows html text in the body refer to attachments
   * <p>
   * We can create HTML body message,
   * if the image file is located at some server location
   * we can use img element to show them in the message.
   * <p>
   * But sometimes we want to attach the image in the email itself
   * and then use it in the email body itself.
   * <p>
   * The trick is to attach the image file like any other attachment
   * and then set the Content-ID header for image file:
   * and then use the same content id in the email message body
   * with <img src='cid:image_id'>.
   *
   * @param session
   * @param extraHeaders
   * @param encoding
   * @param toList
   * @param ccList
   * @param from
   * @param replyList
   * @param subject
   * @param body
   */
  public static void sendEmailWithAttachmentsAndImages(
    final Session session,
    final Map<String, String> extraHeaders,
    final Charset encoding,
    final List<InternetAddress> toList,
    final List<InternetAddress> ccList,
    final InternetAddress from,
    final List<InternetAddress> replyList,
    final String subject,
    final String body,
    final List<String> attachmentFiles,
    final Map<String, String> imageId2file) {

    try {

      MimeMessage msg = initMimeMessage(
        session,
        extraHeaders,
        encoding,
        toList,
        ccList,
        from,
        replyList,
        subject); // Create a multipart message for attachment

      MimeMultipart multipart = mimeMultipart4Attachments(body, attachmentFiles);

      // Attachments
      imageId2file.entrySet().stream().forEach(entry -> {
        BodyPart attachmentBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(entry.getValue());
        try {
          attachmentBodyPart.setDataHandler(new DataHandler(source));
          attachmentBodyPart.setFileName(entry.getValue());
          //Trick is to add the content-id header here
          attachmentBodyPart.setHeader("Content-ID", entry.getKey());
          multipart.addBodyPart(attachmentBodyPart);
        } catch (MessagingException e) {
          throw new IllegalStateException(e);
        }
      });

      msg.setContent(multipart);

      Transport.send(msg);
    } catch (MessagingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static MimeMultipart mimeMultipart4Attachments(
    final String body,
    final List<String> attachmentFiles) {

    // Create a multipart message for attachment
    MimeMultipart multipart = new MimeMultipart();

    // Create the message body part
    BodyPart textBodyPart = new MimeBodyPart();

    try {
      // Fill the message
      textBodyPart.setText(body);
      // Set text message part
      multipart.addBodyPart(textBodyPart);
    } catch (MessagingException e) {
      throw new IllegalStateException(e);
    }

    // Attachments
    attachmentFiles.forEach(f -> {
      BodyPart attachmentBodyPart = new MimeBodyPart();
      DataSource source = new FileDataSource(f);
      try {
        attachmentBodyPart.setDataHandler(new DataHandler(source));
        attachmentBodyPart.setFileName(f);
        multipart.addBodyPart(attachmentBodyPart);
      } catch (MessagingException e) {
        throw new IllegalStateException(e);
      }
    });

    return multipart;
  }

  private static MimeMessage initMimeMessage(
    final Session session,
    final Map<String, String> extraHeaders,
    final Charset encoding,
    final List<InternetAddress> toList,
    final List<InternetAddress> ccList,
    final InternetAddress from,
    final List<InternetAddress> replyList,
    final String subject) {
    MimeMessage msg = new MimeMessage(session);

    //set message headers
    extraHeaders.forEach((k, v) -> {
      try {
        msg.addHeader(k, v);
      } catch (MessagingException e) {
        throw new IllegalStateException(e);
      }
    });

    try {
      msg.setRecipients(Message.RecipientType.TO, toList.toArray(EMPTY));

      msg.setRecipients(Message.RecipientType.CC, ccList.toArray(EMPTY));

      msg.setFrom(from);

      msg.setReplyTo(replyList.toArray(EMPTY));

      msg.setSubject(subject, encoding.toString());

      msg.setSentDate(new Date());

      return msg;
    } catch (MessagingException e) {
      throw new IllegalStateException(e);
    }
  }
}
