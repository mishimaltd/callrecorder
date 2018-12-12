package com.mishima.callrecorder.accountservice.utils;

import java.util.Optional;
import java.util.regex.Pattern;

public enum CardType {

  UNKNOWN,
  VISA("Visa","^4[0-9]{12}(?:[0-9]{3}){0,2}$"),
  MASTERCARD("Mastercard","^(?:5[1-5]|2(?!2([01]|20)|7(2[1-9]|3))[2-7])\\d{14}$"),
  AMERICAN_EXPRESS("American Express","^3[47][0-9]{13}$"),
  DINERS_CLUB("Diners Club","^3(?:[0-5]\\d|095|6\\d{0,2}|[89]\\d{2})\\d{12,15}$"),
  DISCOVER("Discover","^6(?:011|[45][0-9]{2})[0-9]{12}$"),
  JCB("JCB", "^(?:2131|1800|35\\d{3})\\d{11}$"),
  CHINA_UNION_PAY("China Union Pay","^62[0-9]{14,17}$");

  private Pattern pattern;

  private String name;

  CardType() {
    this.pattern = null;
    this.name = null;
  }

  CardType(String name, String pattern) {
    this.name = name;
    this.pattern = Pattern.compile(pattern);
  }

  public static CardType detect(String cardNumber) {
    for (CardType cardType : CardType.values()) {
      if (null == cardType.pattern) continue;
      if (cardType.pattern.matcher(cardNumber).matches()) return cardType;
    }
    return UNKNOWN;
  }

  public static String getName(String cardType) {
    return CardType.valueOf(cardType).getName();
  }

  public String getName() {
    return Optional.ofNullable(name).orElse("Unknown");
  }


}
