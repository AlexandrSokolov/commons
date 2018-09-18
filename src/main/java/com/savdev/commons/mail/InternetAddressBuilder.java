package com.savdev.commons.mail;

import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.InternetAddress;

public class InternetAddressBuilder {

  private String email;
  private String person;

  public static InternetAddressBuilder builder(){
    return new InternetAddressBuilder();
  }

  public InternetAddressBuilder email(
    final String email){
    if (StringUtils.isEmpty(email)){
      throw new IllegalArgumentException("Email cannot be empty");
    }
    this.email = email;
    return this;
  }

  public InternetAddressBuilder person(
    final String person){
    if (StringUtils.isEmpty(person)){
      throw new IllegalArgumentException("Person cannot be empty");
    }
    this.person = person;
    return this;
  }

  public InternetAddress build(){
    try {
      return StringUtils.isEmpty(this.person) ?
        new InternetAddress(email) :
        new InternetAddress(email, person);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
