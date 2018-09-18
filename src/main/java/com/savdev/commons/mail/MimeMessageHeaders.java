package com.savdev.commons.mail;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class MimeMessageHeaders {
  public final String contentType;
  public final String format;
  public final String transferEncoding;

  private MimeMessageHeaders(
    final String contentType,
    final String format,
    final String transferEncoding) {
    this.contentType = contentType;
    this.format = format;
    this.transferEncoding = transferEncoding;
  }

  public Map<String, String> toMap(){
    return ImmutableMap.of(
      "Content-type", this.contentType,
      "format", this.format,
      "Content-Transfer-Encoding", this.transferEncoding);
  }

  public static MimeMessageHeaders defaultMimeHeaders(){
    return MimeMessageHeaders.builder()
      .contentType("text/HTML; charset=UTF-8")
      .format("flowed")
      .transferEncoding("8bit")
      .build();
  }

  public static MimeMessageHeadersBuilder builder() {
    return new MimeMessageHeadersBuilder();
  }


  public static class MimeMessageHeadersBuilder {
    private String contentType;
    private String format;
    private String transferEncoding;

    public MimeMessageHeadersBuilder contentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    public MimeMessageHeadersBuilder format(String format) {
      this.format = format;
      return this;
    }

    public MimeMessageHeadersBuilder transferEncoding(String transferEncoding) {
      this.transferEncoding = transferEncoding;
      return this;
    }

    public MimeMessageHeaders build() {
      return new MimeMessageHeaders(contentType, format, transferEncoding);
    }
  }
}
